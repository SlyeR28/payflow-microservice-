# PayFlow — Merchant & Config Service: Comprehensive Research, Flows & Architecture

---

## 1. Executive Summary & Core Purpose

In PayFlow’s event-driven payment ecosystem, the **Merchant & Config Service** (`merchant-service`) serves as the **master business registry, compliance boundary, and gateway credential vault**.

### Why does this service exist?
1. **Decoupling Business Logic from Checkout**: Business onboarding, KYC verifications, bank account approvals, and document audits are slow, administrative workflows. If they lived inside the payment processing service, slow document uploads or third-party KYC timeouts would directly degrade customer checkout speed.
2. **The "Zero-Fund Holding" Regulatory Moat**:
   - In traditional payment gateways, the gateway collects customer money into an escrow account and disburses payouts to merchants. In India, this requires a strict, multi-year **RBI Payment Aggregator (PA) license** with heavy capital requirements.
   - PayFlow solves this by letting merchants configure their **own** Razorpay, Stripe, or PayPal API keys. When a customer pays, PayFlow routes the transaction straight into the merchant’s own gateway account. PayFlow orchestrates the payment but never holds the funds. **Merchant Service is the vault that secures and manages these credentials.**

---

## 2. Main Tasks & Core Responsibilities

```
                                  ┌────────────────────────────────────────┐
                                  │      MERCHANT & CONFIG SERVICE         │
                                  └───────────────────┬────────────────────┘
                                                      │
         ┌─────────────────────┬──────────────────────┼──────────────────────┬──────────────────────┐
         ▼                     ▼                      ▼                      ▼                      ▼
  1. Business Profile    2. KYC Compliance      3. Gateway Key Vault   4. gRPC Key Supplier   5. Kafka Event Bus
  - Legal business info  - State machine         - AES-256-GCM crypto   - <5ms binary RPC      - merchant.verified
  - PAN / GSTIN capture  - S3 Pre-signed URLs   - Live API handshake   - Redis cached         - kyc.verified
  - Links to auth userId - PII masking          - Razorpay/Stripe keys - Critical path only    - kyc.rejected
```

### Task 1: Merchant Business Onboarding
- Links the business profile to the authenticated user from `auth-service` via `userId`.
- Captures business metadata: Legal Business Name, Brand/Trade Name, Business Type (`INDIVIDUAL`, `PROPRIETORSHIP`, `PRIVATE_LIMITED`, `PUBLIC_LIMITED`), Business Category, Registered Address, Support Email, and Support Phone.

### Task 2: KYC & Compliance Verification State Machine
- Manages an explicit, auditable verification lifecycle:
  $$\text{DRAFT} \longrightarrow \text{SUBMITTED} \longrightarrow \text{IN\_REVIEW} \longrightarrow \text{VERIFIED} \text{ / } \text{REJECTED}$$
- Pluggable verification engine:
  - **Local/Dev**: Automatic mock verification.
  - **Production**: Integration with licensed KYC providers (Digio, Setu, Karza, Surepass).
- Supports re-submission upon rejection with explicit feedback reasons.

### Task 3: Zero-Heap Document Management (Pre-Signed S3 URLs)
- Merchants must upload business proofs (PAN Card, Certificate of Incorporation, Cancelled Cheque).
- Instead of streaming large image/PDF files through the Spring Boot application (which exhausts JVM heap memory), Merchant Service issues **time-limited S3/MinIO pre-signed upload URLs**.
- The client uploads directly to S3. The backend stores only the resulting S3 object key.

### Task 4: Gateway Key Vault & Live Pre-Flight Handshake
- Stores credentials for multi-gateway routing:
  - **Razorpay**: `key_id`, `key_secret`
  - **Stripe**: `publishable_key`, `secret_key`, `webhook_secret`
  - **PayPal**: `client_id`, `client_secret`
- **Envelope Encryption**: Keys are encrypted at rest in PostgreSQL using AES-256-GCM with dynamic Initialization Vectors (IV).
- **Live Sandbox Pre-Flight Handshake**: Before saving or activating a key, Merchant Service makes a live test API call to Razorpay/Stripe. If the gateway returns HTTP 401 (invalid key/typo), the update is rejected immediately.

### Task 5: Ultra-Low Latency gRPC Supplier for Payment Orchestrator
- During checkout, `payment-orchestrator` needs the merchant’s decrypted gateway key to charge the customer.
- Instead of slow REST calls (HTTP/1.1 + JSON), this dependency is served exclusively over **gRPC (HTTP/2 + binary Protobuf)** with local Redis caching, guaranteeing sub-5ms response times.

### Task 6: Kafka Domain Event Publishing
- Emits events to the platform message bus:
  - `merchant.verified`: Triggered when KYC passes; notifies Notification Service to send merchant welcome pack.
  - `kyc.submitted` / `kyc.rejected`: Updates admin dashboard and triggers alert emails.
  - `gateway.key_updated`: Invalidates stale cached credentials in `payment-orchestrator`.

---

## 3. End-to-End Request Flows

### Flow 1: Merchant Registration & Profile Creation
```mermaid
sequenceDiagram
    autonumber
    actor Merchant as Merchant Admin
    participant Gateway as API Gateway (:9000)
    participant MerchantSvc as Merchant Service (:8082)
    participant DB as PostgreSQL (merchants)

    Merchant->>Gateway: POST /api/v1/merchants (Bearer JWT)
    Gateway->>Gateway: Validate JWT & Extract X-User-Id, Role: MERCHANT
    Gateway->>MerchantSvc: Forward request with headers
    MerchantSvc->>MerchantSvc: Validate business details, PAN & GSTIN formats
    MerchantSvc->>DB: Check if userId already has a registered merchant
    alt Already exists
        MerchantSvc-->>Merchant: 409 Conflict ("Merchant profile already exists")
    else Fresh registration
        MerchantSvc->>DB: INSERT INTO merchants (status = 'DRAFT', pan_masked, pan_encrypted, ...)
        MerchantSvc-->>Merchant: 201 Created (MerchantProfileResponse, status = 'DRAFT')
    end
```

---

### Flow 2: Zero-Heap KYC Document Upload (Pre-Signed S3 URLs)
```mermaid
sequenceDiagram
    autonumber
    actor Merchant as Merchant Frontend
    participant Gateway as API Gateway
    participant MerchantSvc as Merchant Service
    participant S3 as AWS S3 / MinIO Object Storage
    participant DB as PostgreSQL (merchant_kyc)

    Note over Merchant,S3: Step A: Request Pre-signed Upload URL
    Merchant->>Gateway: POST /api/v1/merchants/kyc/documents/upload-url<br/>{docType: "PAN_CARD", fileName: "pan.pdf"}
    Gateway->>MerchantSvc: Forward request
    MerchantSvc->>S3: Generate Pre-Signed PUT URL (expires in 15 mins)
    MerchantSvc-->>Merchant: 200 OK {uploadUrl: "https://s3.aws.../raw/pan.pdf?signature=...", s3Key: "kyc/101/pan.pdf"}

    Note over Merchant,S3: Step B: Direct Binary Upload to S3 (Bypasses Backend Heap)
    Merchant->>S3: PUT binary bytes directly to uploadUrl
    S3-->>Merchant: 200 OK

    Note over Merchant,DB: Step C: Confirm Document Upload
    Merchant->>Gateway: POST /api/v1/merchants/kyc/documents/confirm<br/>{docType: "PAN_CARD", s3Key: "kyc/101/pan.pdf"}
    Gateway->>MerchantSvc: Forward confirmation
    MerchantSvc->>DB: INSERT INTO merchant_kyc (merchant_id, doc_type, s3_key, status='SUBMITTED')
    MerchantSvc-->>Merchant: 200 OK ("Document registered for verification")
```

---

### Flow 3: Gateway Key Onboarding with Live Pre-Flight Handshake
```mermaid
sequenceDiagram
    autonumber
    actor Merchant as Merchant
    participant MerchantSvc as Merchant Service
    participant GatewayAPI as External Gateway (Razorpay/Stripe)
    participant Crypto as AES-256-GCM Encryptor
    participant DB as PostgreSQL
    participant Kafka as Apache Kafka

    Merchant->>MerchantSvc: POST /api/v1/merchants/gateways<br/>{gateway: "RAZORPAY", keyId: "rzp_live_abc", keySecret: "sec_xyz"}
    
    Note over MerchantSvc,GatewayAPI: Pre-Flight Handshake (Validates before saving!)
    MerchantSvc->>GatewayAPI: GET /v1/payments?count=1 (Basic Auth: keyId:keySecret)
    alt Gateway returns 401 Unauthorized
        GatewayAPI-->>MerchantSvc: 401 Bad Credentials
        MerchantSvc-->>Merchant: 400 Bad Request ("Invalid Gateway Credentials. Live handshake failed.")
    else Gateway returns 200 OK
        GatewayAPI-->>MerchantSvc: 200 OK (Connection verified)
        MerchantSvc->>Crypto: Encrypt keyId, keySecret with random IV + AES-GCM
        Crypto-->>MerchantSvc: Ciphertexts + IVs
        MerchantSvc->>DB: INSERT INTO merchant_gateway_configs (is_active=true, is_verified=true)
        MerchantSvc->>Kafka: Publish event `gateway.key_updated` (merchantId=101, gateway="RAZORPAY")
        MerchantSvc-->>Merchant: 201 Created (Masked keys: "rzp_live_•••••••abc", verified=true)
    end
```

---

### Flow 4: Synchronous gRPC Key Retrieval during Checkout (The Critical Path)
```mermaid
sequenceDiagram
    autonumber
    actor Customer as Customer Checkout
    participant Orchestrator as Payment Orchestrator (:8083)
    participant Redis as Redis Cache
    participant MerchantSvc as Merchant Service (:8082)
    participant DB as PostgreSQL
    participant Crypto as AES-256-GCM Decryptor

    Customer->>Orchestrator: Initiate Payment (merchantId: 101, amount: 2500, gateway: "RAZORPAY")
    Note over Orchestrator,Redis: Check Fast In-Memory Cache first
    Orchestrator->>Redis: GET merchant:101:gateway:RAZORPAY
    alt Cache Hit (<1ms)
        Redis-->>Orchestrator: Cached Decrypted Credentials
    else Cache Miss
        Note over Orchestrator,MerchantSvc: High-Speed gRPC Call (<5ms over HTTP/2 Protobuf)
        Orchestrator->>MerchantSvc: gRPC GetGatewayCredentials(merchant_id=101, gateway="RAZORPAY")
        MerchantSvc->>DB: SELECT encrypted credentials WHERE merchant_id=101 AND gateway='RAZORPAY'
        DB-->>MerchantSvc: Ciphertext rows
        MerchantSvc->>Crypto: Decrypt using Master Key + IV
        Crypto-->>MerchantSvc: Plaintext key_id & key_secret
        MerchantSvc->>Redis: SETEX merchant:101:gateway:RAZORPAY 3600 (TTL 1 hour)
        MerchantSvc-->>Orchestrator: gRPC Response (apiKey, apiSecret, webhookSecret, isActive=true)
    end
    Note over Orchestrator: Proceed to charge customer via Razorpay SDK
```

---

## 4. Engineering Challenges & Solutions in Merchant Service

| # | Challenge | Impact if Ignored | How PayFlow Solves It |
| :--- | :--- | :--- | :--- |
| **1** | **Gateway Secrets Stored in Plaintext** | A DB leak exposes every merchant's live Stripe/Razorpay keys, leading to total financial compromise. | **JPA AttributeConverter + AES-256-GCM**: Column-level encryption. Every secret is encrypted with a unique random IV before SQL `INSERT`/`UPDATE` and decrypted on `SELECT`. Master key loaded from environment / AWS Secrets Manager. |
| **2** | **Critical Checkout Path Dependency** | Payment Orchestrator needs merchant keys on *every* payment. If Merchant Service is slow, checkout crashes. | **gRPC + Redis Caching**: Binary Protobuf serialization over multiplexed HTTP/2 (<5ms latency). Decrypted credentials cached in Redis with automatic eviction upon key rotation. |
| **3** | **JVM Heap Exhaustion from File Uploads** | Large 10MB KYC PDFs uploaded concurrently cause GC pauses and out-of-memory errors. | **S3 Pre-Signed URLs**: The backend never handles document byte streams. Browsers upload directly to S3 via pre-signed `PUT` URLs; the DB only stores the S3 URI. |
| **4** | **Typo in API Key Breaks Checkout** | Merchant pastes a broken key $\rightarrow$ 100% of customer transactions immediately fail. | **Live Pre-Flight Sandbox Handshake**: Before saving credentials, the service makes an authenticated ping to the live gateway provider. Rejects invalid keys immediately. |
| **5** | **Sensitive PII Compliance (PAN/Aadhaar/Bank)** | Storing raw Aadhaar or full bank account numbers violates RBI & UIDAI data protection rules. | **Masking & Tokenization**: Never store full Aadhaar (store verification token + last 4 digits). Bank account numbers are AES-encrypted, exposing only `last4` for UI display. |
| **6** | **Flaky Third-Party KYC Verification APIs** | Government and verification vendor APIs take 5–15 seconds or randomly timeout. | **Asynchronous KYC Processing**: Non-blocking state machine. Submission returns immediately with `status=SUBMITTED`; background processing transitions to `VERIFIED` and publishes Kafka events. |

---

## 5. PostgreSQL Database Schema Design

```sql
-- 1. Merchants Table
CREATE TABLE merchants (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE, -- Foreign reference to auth-service users.id
    business_name VARCHAR(150) NOT NULL,
    brand_name VARCHAR(100),
    business_type VARCHAR(50) NOT NULL, -- INDIVIDUAL, PROPRIETORSHIP, PRIVATE_LIMITED, etc.
    business_category VARCHAR(100) NOT NULL,
    pan_number_masked VARCHAR(20) NOT NULL,
    pan_number_encrypted TEXT NOT NULL,
    gstin VARCHAR(30),
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT', -- DRAFT, SUBMITTED, IN_REVIEW, VERIFIED, SUSPENDED
    support_email VARCHAR(255) NOT NULL,
    support_phone VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_merchants_user_id ON merchants(user_id);
CREATE INDEX idx_merchants_status ON merchants(status);

-- 2. Merchant KYC Documents
CREATE TABLE merchant_kyc (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    document_type VARCHAR(50) NOT NULL, -- PAN_CARD, GST_CERTIFICATE, CANCELLED_CHEQUE, AADHAAR_FRONT
    s3_object_key VARCHAR(500) NOT NULL,
    verification_reference_id VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, SUBMITTED, VERIFIED, REJECTED
    rejection_reason TEXT,
    verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_kyc_merchant_id ON merchant_kyc(merchant_id);

-- 3. Merchant Bank Accounts (For Settlement & Payouts)
CREATE TABLE merchant_bank_accounts (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    account_number_encrypted TEXT NOT NULL,
    account_number_last4 VARCHAR(4) NOT NULL,
    ifsc_code VARCHAR(20) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    beneficiary_name VARCHAR(150) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT TRUE,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bank_merchant_id ON merchant_bank_accounts(merchant_id);

-- 4. Merchant Gateway Configurations (The Encrypted Vault)
CREATE TABLE merchant_gateway_configs (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    gateway_type VARCHAR(30) NOT NULL, -- RAZORPAY, STRIPE, PAYPAL
    api_key_encrypted TEXT NOT NULL,
    api_secret_encrypted TEXT NOT NULL,
    webhook_secret_encrypted TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_test_mode BOOLEAN NOT NULL DEFAULT TRUE,
    verified_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_merchant_gateway UNIQUE(merchant_id, gateway_type)
);

CREATE INDEX idx_gateway_merchant_active ON merchant_gateway_configs(merchant_id, is_active);
```

---

## 6. Communication Protocols & API Specifications

### 6.1 REST Endpoints (For Merchant Portal & Dashboard)
All endpoints pass through **API Gateway (`:9000`)** with Bearer JWT:
- `POST /api/v1/merchants` $\rightarrow$ Create onboarding profile
- `GET  /api/v1/merchants/me` $\rightarrow$ Get my business profile & KYC status
- `PUT  /api/v1/merchants/me` $\rightarrow$ Update business contact details
- `POST /api/v1/merchants/kyc/documents/upload-url` $\rightarrow$ Request pre-signed S3 upload URL
- `POST /api/v1/merchants/kyc/documents/confirm` $\rightarrow$ Confirm uploaded S3 object key
- `POST /api/v1/merchants/kyc/submit` $\rightarrow$ Trigger verification state transition
- `POST /api/v1/merchants/gateways` $\rightarrow$ Add/update Razorpay/Stripe keys (with live pre-flight check)
- `GET  /api/v1/merchants/gateways` $\rightarrow$ List configured gateways (with masked keys)
- `POST /api/v1/merchants/bank-accounts` $\rightarrow$ Add settlement payout bank account

---

### 6.2 gRPC Contract (`merchant_service.proto`)
Used by `payment-orchestrator` during checkout:

```protobuf
syntax = "proto3";

package com.payflow.merchant.grpc;

option java_multiple_files = true;
option java_package = "com.payflow.merchant.grpc";

service MerchantGrpcService {
    // Fetches live decrypted gateway credentials for checkout
    rpc GetGatewayCredentials (GetGatewayCredentialsRequest) returns (GetGatewayCredentialsResponse);

    // Validates whether the merchant is KYC-approved and active
    rpc ValidateMerchantStatus (ValidateMerchantStatusRequest) returns (ValidateMerchantStatusResponse);
}

message GetGatewayCredentialsRequest {
    int64 merchant_id = 1;
    string gateway_type = 2; // "RAZORPAY", "STRIPE", "PAYPAL"
}

message GetGatewayCredentialsResponse {
    int64 merchant_id = 1;
    string gateway_type = 2;
    string api_key = 3;
    string api_secret = 4;
    string webhook_secret = 5;
    bool is_test_mode = 6;
    bool is_active = 7;
}

message ValidateMerchantStatusRequest {
    int64 merchant_id = 1;
}

message ValidateMerchantStatusResponse {
    int64 merchant_id = 1;
    bool is_verified = 2;
    string status = 3; // "VERIFIED", "PENDING_KYC", "SUSPENDED"
}
```

---

### 6.3 Kafka Topics & Event Schemas
- **`merchant.verified`**:
  ```json
  {
    "eventId": "evt_8921389",
    "eventType": "MERCHANT_VERIFIED",
    "merchantId": 101,
    "userId": 42,
    "businessName": "Acme Payments Pvt Ltd",
    "timestamp": "2026-10-05T14:30:00Z"
  }
  ```
- **`kyc.status_changed`**:
  ```json
  {
    "eventId": "evt_8921390",
    "merchantId": 101,
    "previousStatus": "SUBMITTED",
    "currentStatus": "VERIFIED",
    "reason": null,
    "timestamp": "2026-10-05T14:32:00Z"
  }
  ```
