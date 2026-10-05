# PayFlow — 18 Core Concepts Explained Simply

Here is the complete architectural breakdown of PayFlow in 18 simple, interview-ready points. Each point covers **What it is**, **Why it exists**, and **How it works**.

---

### 1. The Core Purpose (The "Elevator Pitch")
- **What it is:** PayFlow is an event-driven, 9-microservice payment processing platform modeled after real-world payment orchestrators like Stripe or Juspay.
- **Why it matters:** Instead of hardcoding a single payment gateway (like just Razorpay), PayFlow routes payments dynamically across Razorpay, Stripe, and PayPal, scores fraud in real time, and ensures financial balance via double-entry bookkeeping.

---

### 2. Microservices with Database-per-Service
- **What it is:** The system is split into 9 distinct services (Auth, Merchant, Payment Orchestrator, Risk, Ledger, Settlement, Reconciliation, Notification, Report).
- **Why it matters:** In monolithic architectures, a bug in email notifications or a slow reporting query can crash the checkout flow. In PayFlow, each service has its own dedicated database (PostgreSQL, MySQL, Redis, MongoDB), ensuring failures never cascade across service boundaries.

---

### 3. Service Discovery & Gateway Routing (Eureka + Spring Cloud Gateway)
- **What it is:** Client traffic enters through a single **Spring Cloud Gateway**, and services discover each other using a **Netflix Eureka Server**.
- **How it works:** Services do not hardcode IP addresses or ports. When `payment-orchestrator` needs to call `merchant-service`, it looks up the logical service name via Eureka. The API Gateway handles centralized rate-limiting, JWT authentication, and request routing.

---

### 4. Protocol Choice: Using the Right Tool for the Job
- **REST:** Used for external browser/mobile clients and payment gateway webhooks because HTTP/JSON is universal.
- **gRPC (HTTP/2 + Protobuf):** Used for internal synchronous calls between Payment and Risk/Merchant where ultra-low latency (<30ms) and strict binary contracts are mandatory.
- **Kafka:** Used for all asynchronous, non-blocking background workflows (ledger entries, email/SMS notifications, analytics).
- **GraphQL:** Used for merchant reporting dashboards so merchants can query nested transaction summaries without over-fetching or under-fetching data.

---

### 5. Multi-Gateway Routing with Resilience4j Circuit Breakers
- **The Problem:** Third-party payment gateways (Razorpay, Stripe, PayPal) experience intermittent downtimes and latency spikes. If Razorpay goes down, merchants lose money.
- **How PayFlow solves it:** PayFlow uses **Resilience4j Circuit Breakers** per gateway. If Razorpay starts failing (e.g., >50% failure rate over a 20-call sliding window), the circuit flips to `OPEN`. Subsequent checkout traffic instantly fails over to Stripe or PayPal automatically without waiting for timeouts.

---

### 6. Two-Layer Idempotency (Stopping Double Charges)
- **The Problem:** A user double-clicks "Pay Now", or a mobile network timeout causes an automatic retry. If unhandled, the user is charged twice for one order.
- **Layer 1 (Fast Filter):** When a request hits the system with an `idempotency_key`, we check an in-memory **Redis Bloom filter**. If the Bloom filter says the key hasn't been seen, we know with 100% certainty it is fresh and allow it through in $O(1)$ time.
- **Layer 2 (Absolute Guarantee):** Because Bloom filters can have a small false-positive rate, the database maintains a `UNIQUE` constraint on `idempotency_key`. If a duplicate sneakily bypasses the filter, the database rejects the second insert with a constraint violation.

---

### 7. The Transactional Outbox Pattern (Solving Dual-Write Bugs)
- **The Problem:** A payment succeeds, so you want to: (1) update the database, and (2) send an event to Kafka. If the database commits but Kafka is temporarily unreachable, your message is lost. If Kafka sends but the DB transaction rolls back, you emitted a phantom event.
- **How PayFlow solves it:** In one local database transaction, the service writes the payment status AND an event record into an `outbox` table. A background poller reads unprocessed outbox rows, publishes them to Kafka, and marks them `PROCESSED`. It is impossible for one to happen without the other.

---

### 8. Distributed Transactions via Saga (Refund Compensation)
- **The Problem:** You cannot use traditional ACID distributed transactions (2PC / Two-Phase Commit) across microservices because locking rows across networks destroys checkout throughput.
- **How PayFlow solves it:** We use an **Orchestrator-driven Saga**. When a card is charged, the Orchestrator emits `PaymentCaptured`. The Ledger service consumes this and writes accounting records. If the Ledger write fails (`LedgerWriteFailed`), the Orchestrator triggers an automatic compensating transaction: refunding the money via the payment gateway and marking the payment `FAILED`. Money and records always match.

---

### 9. Deep `@Transactional` Management in Spring Boot
- **Self-Invocation Trap:** Calling a `@Transactional` method from another method in the same class calls the raw object instead of the Spring CGLIB proxy, causing transactions to silently fail. We resolved this by extracting business methods to separate service beans.
- **Exception Rollbacks:** Checked exceptions don't trigger rollbacks by default. We explicitly configure `@Transactional(rollbackFor = Exception.class)`.
- **Propagation (`REQUIRES_NEW`):** Used for outbox messages and audit trails so logging entries commit to the database even if the parent business transaction encounters an error and rolls back.

---

### 10. Double-Entry, Hash-Chained Ledger
- **The Rule:** In financial systems, you never just "update balance = balance + 100". Every financial transaction must have at least one debit and one credit leg, and their sum must equal zero ($\text{Debits} + \text{Credits} = 0$).
- **Hash-Chaining:** Each ledger row contains a cryptographic hash of its own data combined with the hash of the preceding row (like a private mini-blockchain). If someone tries to tamper with a past ledger row directly in the database, the entire chain breaks.

---

### 11. Snowflake ID Generation
- **What it is:** Ledger rows and payments use 64-bit Twitter Snowflake IDs instead of auto-incrementing database integers (`1, 2, 3...`).
- **Why it matters:** Auto-increment IDs leak business metrics (e.g., an attacker can guess you have 500 orders a day) and cannot be generated independently across multi-node distributed database shards without lock collisions. Snowflake IDs are roughly time-ordered and generated in-memory without coordination.

---

### 12. Real-Time Risk & Fraud Engine (gRPC only)
- **What it is:** Every payment request passes through an internal Fraud Engine before reaching a payment gateway.
- **How it works:** It evaluates velocity checks (e.g., how many cards has this IP tried in the last 10 minutes?) using **Redis sliding-window sorted sets** and declarative business rules. It outputs `APPROVE`, `REVIEW`, or `BLOCK` in under 30ms over gRPC. It has no public route, making it completely invisible to outside attackers.

---

### 13. Settlement, Fees & Rolling Reserves
- **What it is:** Once payments are captured, merchants need to be paid out.
- **How it works:** The Settlement service calculates the Merchant Discount Rate (MDR) plus GST, deducts fees, nets out refunds and chargebacks, holds back a 5–10% **rolling reserve** for risky merchants, and batches payouts on a $T+1$ or $T+2$ banking schedule.

---

### 14. Nightly Merkle-Tree Reconciliation
- **The Problem:** At the end of the day, the internal Ledger records must match the settlement report provided by the bank or gateway (e.g., Stripe/Razorpay CSV). Comparing millions of rows one-by-one is computationally expensive.
- **How PayFlow solves it:** PayFlow constructs a **Merkle Tree** (hash tree) for internal entries and one for gateway entries. It compares the root hashes: if they match, the data is 100% identical. If they don't, it walks down only the mismatched branch to isolate the discrepant transaction in milliseconds.

---

### 15. Asynchronous Notification Fan-Out
- **What it is:** Email, SMS, and webhook notifications are decoupled entirely from the checkout flow.
- **How it works:** The Notification service simply listens to Kafka events (`payment.captured`, `payment.failed`). If a third-party SMS provider like Twilio is slow or down, the user's payment still completes in 1 second; the notification queue retries in the background with exponential backoff.

---

### 16. Secure Authentication & Session Lifecycle
- **Access Tokens:** Short-lived RS256/HS256 JWTs (15 minutes). The API Gateway validates tokens locally without querying the Auth service on every call.
- **Refresh Token Rotation (Family Detection):** When a refresh token is used, it is invalidated and a new one is issued. If an attacker steals an old refresh token and tries to use it, the system detects "token reuse", invalidates the entire family ID, and forces an immediate re-login.
- **Instant Logout:** Blacklists the JWT's unique ID (`jti`) in Redis with a TTL matching the token's remaining lifespan.

---

### 17. Collision-Free Unique Username Generation
- **What it is:** When users register via email or third-party identity, a clean, readable username is automatically generated (e.g., `rishabh.kumar`, `rishabh.kumar1`).
- **How it works:** The system cleans the email prefix, checks for collisions in PostgreSQL, and increments a deterministic suffix under optimistic concurrency control, ensuring zero username collisions during concurrent signups.

---

### 18. Merchant Key Encryption (Zero Fund Holding)
- **What it is:** Merchants input their own Razorpay, Stripe, or PayPal API keys.
- **Security:** Keys are encrypted at rest using AES-GCM (with encryption keys stored in AWS Secrets Manager). PayFlow validates the credentials against the live gateway before marking them verified.
- **Regulatory Benefit:** Because customer payments route directly into the merchant's gateway account, PayFlow never holds customer funds, avoiding the strict compliance burden of requiring an RBI Payment Aggregator license.
