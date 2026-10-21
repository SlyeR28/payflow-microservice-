# 💳 PayFlow — Enterprise Distributed Payment Orchestration & Financial Ledger

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2024.x-2563EB)](https://spring.io/projects/spring-cloud)
[![Resilience4j](https://img.shields.io/badge/Resilience4j-2.x-8A2BE2)](https://resilience4j.readme.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.x-47A248?logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![Redis](https://img.shields.io/badge/Redis-7.x-DC382D?logo=redis&logoColor=white)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)

> **PayFlow** is a cloud-native, distributed payment orchestration and immutable financial ledger platform engineered after modern fintech platforms like **Stripe**, **Juspay**, and **Adyen**. It dynamically routes transactions across multiple upstream payment acquirers (Razorpay, Stripe, PayPal), eliminates double charges with two-tier idempotency, protects accounting balances with a double-entry zero-sum engine, and enforces system-wide resilience via Resilience4j.

---

## 📚 Master Documentation Repository

Comprehensive engineering specifications, system design architectures, interview handbooks, and resilience blueprints are available in the [`docs/`](file:///d:/payflow-backend/docs) directory:

1. **[Recruiter & System Design Interview Master Handbook](file:///d:/payflow-backend/docs/PAYFLOW_RECRUITER_AND_INTERVIEW_HANDBOOK.md)**:
   - Executive pitch, core value proposition, and competitive advantage.
   - Deep technical analysis of **every single problem PayFlow solves** (Gateway outages, dual-charge race conditions, dual-write outbox patterns, distributed SAGA consistency, double-entry mathematical invariants, zero-buffer S3 KYC uploads, and token family replay detection).
   - Comprehensive **Recruiter & System Design Interview Question Bank** with battle-tested answers (System Design, Financial Engineering, Concurrency, Fault Tolerance, Security, and STAR-format war stories).

2. **[Complete High-Level Design (HLD) & Services Low-Level Design (LLD)](file:///d:/payflow-backend/docs/PAYFLOW_SYSTEM_HLD_AND_SERVICES_LLD.md)**:
   - End-to-end Enterprise System Architecture diagram (Mermaid).
   - Network boundaries, DMZ proxy, security topologies, and ingress routing.
   - Polyglot storage architecture (MySQL database-per-service, MongoDB polymorphic logs, Redis caches).
   - Distributed SAGA sequence diagram with backward compensation.
   - Service-by-service LLD for all 9 modules (`api-gateway`, `eureka-server`, `config-server`, `auth-service`, `merchant-service`, `payment-service`, `ledger-service`, `notification-service`, `common`).
   - Complete database schemas (DDL), domain entity models, class hierarchies, and REST API specifications.

3. **[Resilience4j Master Implementation Guide & Call-Site Audit](file:///d:/payflow-backend/docs/PAYFLOW_RESILIENCE4J_MASTER_IMPLEMENTATION_GUIDE.md)**:
   - Exhaustive audit marking **10 exact integration points / call sites** across the microservices mesh.
   - Failure mode analysis, pattern selection rationale (Circuit Breaker, Retry, Bulkhead, Rate Limiter, Time Limiter).
   - Production-ready `application.yaml` configurations and complete Java code snippets.
   - Fail-Closed vs Fail-Open policies and SAGA compensation hooks.
   - Spring Boot Actuator monitoring, Prometheus metric alerts, and Top 10 resilience anti-patterns checklist.

---

## 🏛️ Ecosystem Overview & Port Mapping

| Service Module | Port | Technology | Primary Responsibility |
| :--- | :--- | :--- | :--- |
| **`api-gateway`** | `9000` | Spring Cloud Gateway, Redis | Edge reverse proxy, JWT claim verification & header injection, CORS, route-level circuit breakers, Redis rate limiter. |
| **`eureka-server`** | `8761` | Netflix Eureka | Dynamic service registration, health check heartbeats, and client-side load balancing. |
| **`config-server`** | `8888` | Spring Cloud Config | Centralized configuration repository for externalized environment properties. |
| **`auth-service`** | `8082` | Spring Boot, MySQL (`security`) | User identity, RBAC, BCrypt hashing, JWT issuance, Refresh Token Rotation with Token Family Reuse Detection. |
| **`merchant-service`** | `8083` | Spring Boot, MySQL (`merchant`), S3/MinIO | Merchant onboarding, KYC verification with S3 pre-signed URLs, bank accounts, and AES-256-GCM credential vault. |
| **`payment-service`** | `8084` | Spring Boot, MySQL (`payment_db`) | Payment orchestration state machine, two-tier idempotency, multi-gateway strategy factory (Razorpay/Stripe), SAGA coordinator. |
| **`ledger-service`** | `8085` | Spring Boot, MySQL (`payflow_ledger`) | Double-entry zero-sum accounting ($\sum \text{Debits} = \sum \text{Credits}$), snapshot balances with optimistic locking (`@Version`), hash chaining. |
| **`notification-service`** | `8086` | Spring Boot, MongoDB (`payflow_notification`) | Asynchronous multi-channel dispatch (Email, SMS, Webhooks) with Thymeleaf templates and 90-day TTL indexes. |
| **`common`** | N/A | Java Library | Standardized API wrappers (`ApiResponse`), global exceptions, `JwtProvider`. |
| **`common-client`** | N/A | Java Library | Declarative OpenFeign client interfaces, fallback implementations, and security interceptors. |

---

## 🚀 Quickstart & Local Orchestration

### Prerequisites
- **Java 21 (LTS)**
- **Maven 3.9+**
- **Docker & Docker Compose**

### Running with Docker Compose
To launch the complete infrastructure (MySQL, MongoDB, Redis, MinIO, Eureka, Gateway, and Services):

```bash
# Clone the repository
git clone https://github.com/SlyeR28/payflow-microservice-.git
cd payflow-backend

# Launch all infrastructure containers
docker compose up -d
```

### Building the Project Locally
```bash
# Clean and compile all microservice modules
./mvnw clean install -DskipTests
```

---

## 🛡️ License
Copyright © 2026 PayFlow Core Platform Engineering. All Rights Reserved.
