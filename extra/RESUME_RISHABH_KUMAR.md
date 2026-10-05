# RISHABH KUMAR
**Java Backend Developer — Spring Boot — Microservices & Distributed Systems — Immediate Joiner**  
+91 7037908312 | [rishabhkumarjob28@gmail.com](mailto:rishabhkumarjob28@gmail.com) | [linkedin.com/in/rishabh-kumar-21458135a/](https://linkedin.com/in/rishabh-kumar-21458135a/) | [github.com/SlyeR28](https://github.com/SlyeR28)

---

### PROFILE SUMMARY
Software Engineer with 1+ year of professional experience building scalable backend and distributed microservices architectures using Java, Spring Boot, and Spring Cloud. Deep technical background in event-driven systems with Apache Kafka, gRPC, and REST, solving complex consistency and reliability challenges through the Transactional Outbox pattern, Saga-based compensations, two-layer idempotency (Redis Bloom filter + DB constraints), and fine-tuned `@Transactional` boundaries. Proficient in database-per-service patterns, Redis distributed caching, Resilience4j fault tolerance, Docker containerization, CI/CD automation, and AWS cloud infrastructure.

---

### TECHNICAL SKILLS
- **Programming Languages:** Java (8/11/17/21), SQL, Bash, Data Structures & Algorithms
- **Backend & Frameworks:** Spring Boot, Spring Cloud (Gateway, Eureka, OpenFeign), Spring Security, Spring Data JPA, Hibernate, gRPC, Protocol Buffers, RESTful APIs, Servlet, JSP
- **Distributed Systems & Architecture:** Apache Kafka, Saga Pattern (Orchestrator/Choreography), Transactional Outbox Pattern, Two-Layer Idempotency, CQRS, Database-per-Service
- **Resilience & Caching:** Resilience4j (Circuit Breakers, Retries, Timeouts, Fallbacks), Redis (Sliding-Window Velocity, Bloom Filters, Distributed Caching, Token Blacklisting)
- **Databases & Storage:** PostgreSQL, MySQL, Redis, AWS S3
- **Authentication & Security:** JWT (RS256 / HS256), Refresh Token Rotation (Family-Based Reuse Detection), RBAC, Spring Security Filter Chains
- **DevOps & Cloud:** Docker, Docker Compose, AWS (VPC, EC2, RDS, ALB, Auto Scaling, Secrets Manager, CloudFormation), Jenkins, LXD, Linux, Git, GitHub
- **Testing & Quality:** JUnit 5, Mockito, Postman, SonarQube, OWASP Dependency-Check, Trivy

---

### PROFESSIONAL EXPERIENCE

**Seasec Private Limited** &nbsp;|&nbsp; Mohali, Punjab  
*Software Engineer – Java Developer* &nbsp;|&nbsp; **Dec 2025 – July 2026**
- Developed and maintained Java Spring Boot microservices and enterprise RESTful APIs, implementing robust business workflows and reducing backend response latency.
- Integrated Razorpay payment gateway handling secure checkout workflows, HMAC-SHA256 callback signature verification, and real-time transaction processing.
- Optimized database operations across relational MySQL databases through schema refactoring, composite indexing, and transaction isolation management.
- Collaborated with cross-functional teams to containerize and deploy backend services on Linux environments, ensuring 99.9% uptime and zero-downtime releases.

**Coding Club India** &nbsp;|&nbsp; Remote  
*Software Developer Intern* &nbsp;|&nbsp; **Aug 2025 – Nov 2025**
- Developed 10+ scalable backend modules using Java and Spring MVC, enabling reliable feature delivery across web applications.
- Built and optimized 12+ REST APIs with tuned SQL queries, improving data retrieval speeds and backend response performance by 35%.
- Implemented client-side and server-side validation layers, reducing bad request payloads and API error rates.
- Executed A/B testing across 5+ UI workflows, increasing user engagement by 30% and reducing bounce rates by 25%.

---

### PROJECTS

**PayFlow — Event-Driven Distributed Payment Platform** &nbsp;|&nbsp; **Sep 2025 – Present**  
*Java, Spring Boot, Spring Cloud, Kafka, gRPC, Redis, PostgreSQL, Resilience4j, Docker*
- **Architected a 9-microservice payment platform** (Auth, Payment Orchestrator, Ledger, Risk, Settlement, Merchant, Reconcile, Notify, Report) using **Spring Cloud Gateway**, **Eureka Service Discovery**, and **OpenFeign**, adhering strictly to a database-per-service model.
- **Eliminated distributed dual-write inconsistencies** between PostgreSQL and Kafka by implementing the **Transactional Outbox Pattern** with local `@Transactional` boundary commits and a separate poller, guaranteeing at-least-once message delivery.
- **Engineered a Saga compensation workflow** between Payment Orchestrator and Ledger: on downstream ledger recording failures (`LedgerWriteFailed`), compensation triggers an automated gateway refund, keeping money and accounting records strictly consistent without blocking 2PC locks.
- **Prevented duplicate charges** across network timeouts and aggressive client retries using a **two-layer idempotency mechanism**: an in-memory **Redis Bloom filter** as a fast probabilistic pre-check backed by a database unique constraint on `idempotency_key`.
- **Built a fault-tolerant multi-gateway payment router** (Razorpay, Stripe, PayPal) with weighted routing and **Resilience4j Circuit Breakers**, timeouts, and fallback routing, automatically isolating failing gateways without manual intervention.
- **Developed an internal, low-latency Risk & Fraud Engine** exposed exclusively via **gRPC/Protobuf**, executing Redis sliding-window velocity checks and rule scoring inside the 30ms synchronous checkout path.
- **Hardened security & identity lifecycle**: implemented asymmetric/HMAC JWTs with family-based refresh token rotation (preventing token theft), Redis-backed instant token revocation/blacklisting, and collision-resistant automated username generation.

**Event Management System** &nbsp;|&nbsp; **Oct 2025 – Nov 2025**  
*Spring Boot, MySQL, Redis, Elasticsearch, JWT, Docker* &nbsp;|&nbsp; [GitHub](https://github.com/SlyeR28)
- Built a scalable backend system with 50+ REST APIs using Spring Boot, MySQL, Redis, Elasticsearch, and JWT authentication.
- Improved API response times by 40% through Redis multi-level caching, SQL query optimization, and efficient database indexing strategies.
- Implemented Elasticsearch-based search, improving event discovery speed, fuzzy search accuracy, and user search experience by 30%.
- Developed asynchronous notifications, dynamic pricing logic, and QR-based ticket generation for 1,000+ users.

**CI/CD Pipeline & AWS Infrastructure for Spring Boot Backend** &nbsp;|&nbsp; **Aug 2026**  
*Jenkins, LXD, Docker, SonarQube, OWASP, Trivy, AWS CloudFormation* &nbsp;|&nbsp; [GitHub](https://github.com/SlyeR28)
- Designed a multi-agent Jenkins CI/CD pipeline using isolated LXD containers (build, test, security, Docker agents), triggered automatically via GitHub webhooks.
- Integrated SonarQube, OWASP Dependency-Check, and Trivy for static analysis, dependency scanning, and container image scanning, blocking builds with CRITICAL/HIGH vulnerabilities.
- Automated Docker image build, tag, and push to Docker Hub, using stash/unstash artifact passing across agents to eliminate duplicate checkouts.
- Provisioned a secure two-tier AWS architecture using CloudFormation (IaC): VPC, subnets, NAT Gateway, Bastion Host, Auto Scaling Group, ALB, RDS MySQL, and Secrets Manager; deployed to EC2 via AWS Systems Manager (SSM) with runtime secret injection.

---

### EDUCATION

**DY Patil University** &nbsp;|&nbsp; Pune, India  
*Bachelor of Technology in Computer Science (CGPA: 7.89/10)* &nbsp;|&nbsp; **Dec 2021 – Jun 2025**

---

### ACHIEVEMENTS & CERTIFICATIONS
- Solved 500+ algorithmic problems across LeetCode, CodeChef, HackerRank, InterviewBit, and SPOJ.
- Earned a Java Developer Certification with strong focus on Spring Boot, Microservices, REST APIs, and backend system design.
