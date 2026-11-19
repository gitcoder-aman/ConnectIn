# Connect-In: Distributed Microservices Social Networking Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.2-blue.svg)](https://spring.io/projects/spring-cloud)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-GKE-326CE5.svg?logo=kubernetes)](https://kubernetes.io/)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-KRaft%20Mode-231F20.svg?logo=apachekafka)](https://kafka.apache.org/)
[![Neo4j](https://img.shields.io/badge/Neo4j-5.26.3-008CC1.svg?logo=neo4j)](https://neo4j.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-4169E1.svg?logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Jib%20Containerized-2496ED.svg?logo=docker)](https://github.com/GoogleContainerTools/jib)
[![Cloudinary](https://img.shields.io/badge/Cloudinary-Media%20CDN-3448C5.svg?logo=cloudinary)](https://cloudinary.com/)

> **Production Deployment Base URL:** `http://34.95.113.61/`  
> **Source Repository:** [gitcoder-aman/ConnectIn](https://github.com/gitcoder-aman/ConnectIn.git)  
> **Container Registry:** [Docker Hub: aman06383](https://hub.docker.com/u/aman06383)

---

## Table of Contents

1. [Project Overview](#1-project-overview)
   - [Domain & Value Proposition](#domain--value-proposition)
   - [Core System Features](#core-system-features)
   - [Architectural Paradigm](#architectural-paradigm)
   - [Inter-Service Communication Strategy](#inter-service-communication-strategy)
   - [Polyglot Persistence Model](#polyglot-persistence-model)
   - [Event-Driven Architecture](#event-driven-architecture)
2. [Technology Stack](#2-technology-stack)
3. [Microservices Architecture](#3-microservices-architecture)
   - [Microservices Catalog](#microservices-catalog)
   - [High-Level Architectural Workflow](#high-level-architectural-workflow)
   - [Mermaid System Architecture Diagram](#mermaid-system-architecture-diagram)
4. [Complete API Documentation](#4-complete-api-documentation)
   - [Authentication & JWT Security Model](#authentication--jwt-security-model)
   - [User Service APIs](#user-service-apis)
   - [Post Service APIs](#post-service-apis)
   - [Connections Service APIs](#connections-service-apis)
   - [Internal Microservice APIs (Uploader Service)](#internal-microservice-apis-uploader-service)
5. [Database Architecture & Persistence](#5-database-architecture--persistence)
   - [Relational Tier (PostgreSQL Instances)](#relational-tier-postgresql-instances)
   - [Graph Tier (Neo4j Social Graph)](#graph-tier-neo4j-social-graph)
   - [Kubernetes StatefulSets & Volume Storage (PVC)](#kubernetes-statefulsets--volume-storage-pvc)
   - [Database Entity-Relationship & Graph Model](#database-entity-relationship--graph-model)
6. [Local Development Setup](#6-local-development-setup)
   - [Prerequisites](#prerequisites)
   - [Step-by-Step Installation & Running Guide](#step-by-step-installation--running-guide)
7. [Docker & Containerization Implementation](#7-docker--containerization-implementation)
   - [Daemonless Image Creation with Google Jib](#daemonless-image-creation-with-google-jib)
   - [Build & Push Commands](#build--push-commands)
8. [Kubernetes (K8s) Deployment Architecture](#8-kubernetes-k8s-deployment-architecture)
   - [Workload Topologies: Deployments vs. StatefulSets](#workload-topologies-deployments-vs-statefulsets)
   - [Networking, DNS, & Ingress Routing](#networking-dns--ingress-routing)
   - [Manifest Registry & Inspection Commands](#manifest-registry--inspection-commands)
9. [Google Kubernetes Engine (GKE) Deployment Guide](#9-google-kubernetes-engine-gke-deployment-guide)
   - [Cloud Deployment Workflow](#cloud-deployment-workflow)
   - [Step-by-Step GKE Provisioning Commands](#step-by-step-gke-provisioning-commands)
10. [End-to-End API Testing with cURL](#10-end-to-end-api-testing-with-curl)
11. [Project Directory Structure](#11-project-directory-structure)
12. [Troubleshooting & Production Operations](#12-troubleshooting--production-operations)
13. [Future Architectural Roadmap](#13-future-architectural-roadmap)

---

## 1. Project Overview

### Domain & Value Proposition
**Connect-In** is an enterprise-grade, distributed microservices social networking application modeled after modern professional networks (such as LinkedIn). In traditional monolithic architectures, social network operations suffer from intense coupling: graph traversals (friend/connection networks) execute poorly on relational schemas; heavy binary image uploads saturate web servers; and fan-out notifications on user activities (such as posts and likes) cause high write lock contention on centralized relational tables.

Connect-In resolves these systemic problems by decomposing the platform into independently scalable, highly decoupled bounded contexts. Each microservice manages its own lifecycle, encapsulates its own domain model, and leverages the database engine best aligned with its storage access patterns (polyglot persistence).

### Core System Features
* **Stateless Authentication & Identity Management:** Secure user registration, credential hashing with BCrypt (`org.mindrot:jbcrypt`), and compact signed JSON Web Token (JJWT) issuance with centralized validation at the API Gateway.
* **Graph-Powered Social Connections:** Graph database modeling using Neo4j to manage user nodes, direct connections (`CONNECTED_TO`), connection requests (`REQUESTED_TO`), and instantaneous $O(1)$/$O(k)$ first-degree connection lookups.
* **Multipart Media & Post Management:** Multipart post publishing with automated image offloading to Cloudinary CDN via a dedicated stateless media processor, user-specific feed retrieval, and post inspection.
* **Real-Time Likes & Engagement:** Instant post liking and unliking with duplicate vote protection and atomic deletion.
* **Asynchronous Event-Driven Fan-Out:** Distributed event choreography using Apache Kafka in KRaft mode. When posts are published or liked, events fan out to Kafka topics, decoupling activity producers from notification consumers.
* **Asynchronous Notification Ingestion:** Autonomous notification persistence without slowing down the post publishing and like write-paths.
* **Centralized API Ingress & Gateway Security:** Single entry point via Spring Cloud Gateway WebMVC executing cross-cutting token inspection, header mutation (`X-User-Id`), path stripping, and CORS controls.
* **Cloud-Native Kubernetes Infrastructure:** Containerized with Google Jib, orchestrated on Google Kubernetes Engine (GKE) with StatefulSets for databases, Deployments for stateless services, and a Google Cloud HTTP(S) Load Balancer (GCE Ingress).

### Architectural Paradigm
```
[ Client ] 
    │ HTTP / JSON / Multipart
    ▼
[ Google Cloud Ingress Controller (External IP: 34.95.113.61) ]
    │
    ▼
[ API Gateway (Spring Cloud Gateway WebMVC - Port 8080) ]
    │  - JWT Bearer Authentication & Claims Extraction
    │  - CORS & Prefix Stripping (/api/v1/* -> /*)
    │  - Injects X-User-Id Downstream Header
    │
    ├──▶ User Service (Port 9020) ────────▶ PostgreSQL (userDB)
    │        │
    │        └─[Kafka: user_created_topic]──▶ Connection Service
    │
    ├──▶ Post Service (Port 9010) ────────▶ PostgreSQL (postsDB)
    │        │
    │        ├─(Feign Sync)──▶ Uploader Service (Port 9050) ──▶ Cloudinary CDN
    │        ├─(Feign Sync)──▶ Connection Service (Port 9030)
    │        │
    │        ├─[Kafka: post_created_topic]─┐
    │        └─[Kafka: post_liked_topic]───┼──▶ Notification Service (Port 9040)
    │                                      │        │
    │                                      │        ▼
    │                                      └──▶ PostgreSQL (notificationDB)
    │
    └──▶ Connection Service (Port 9030) ──▶ Neo4j Graph Database (connections-db)
```

### Inter-Service Communication Strategy
The application utilizes a hybrid communication architecture to balance low-latency retrieval with high-throughput decoupling:
1. **Synchronous Interservice Invocations (Spring Cloud OpenFeign):**
   - **Post Service ➔ Uploader Service:** Post creation requires binary image uploading. The `PostService` invokes `UploaderServiceClient` (`POST /upload/file`) passing the multipart payload synchronously to obtain the CDN secure URL before persisting the post record.
   - **Post Service ➔ Connections Service:** During post publication, `PostService` calls `ConnectionServiceClient` (`GET /connections/core/{userId}/first-degree`) to retrieve the author's first-degree network for targeted notification dispatch.
2. **Asynchronous Event-Driven Decoupling (Apache Kafka):**
   - Critical domain state changes are published to Kafka topics with partitioned keys (`userId` / `postId`).
   - `userService` fires `UserCreatedEvent` to `user_created_topic`. `connectionService` consumes this event to initialize graph nodes asynchronously without coupling user signup to Neo4j availability.
   - `postService` fires `PostCreated` to `post_created_topic` and `PostLiked` to `post_liked_topic`. `notification-service` consumes these streams and persists notification audit logs without adding latency to the client request.

### Polyglot Persistence Model
* **User Service:** PostgreSQL (`userDB`) for strict ACID consistency and unique email constraints.
* **Post Service:** PostgreSQL (`postsDB`) for structured tabular post records and relational like records.
* **Notification Service:** PostgreSQL (`notificationDB`) for high-volume append-only notification records.
* **Connection Service:** Neo4j Community (`connections-db` / `connectin-db`) for graph traversal efficiency, relationship pointer jumping, and native Cypher queries.

### Event-Driven Architecture
| Topic Name | Partitions | Replication Factor | Producer Service | Consumer Service | Payload Contract |
|---|---|---|---|---|---|
| `user_created_topic` | 3 | 1 | `userService` | `connectionService` | `UserCreatedEvent (userId, name)` |
| `post_created_topic` | 3 | 1 | `postService` | `notification-service` | `PostCreated (ownerUserId, postId, userId, content)` |
| `post_liked_topic` | 3 | 1 | `postService` | `notification-service` | `PostLiked (postId, ownerUserId, likedByUserId)` |

---

## 2. Technology Stack

Every technology listed below is verified from the build manifests (`pom.xml`), configurations (`application*.yml`, `docker-compose.yml`), and deployment manifests (`k8s/*.yml`):

### Core Runtimes & Frameworks
* **Java Development Kit (JDK 21):** LTS release utilizing modern language features, records, and pattern matching.
* **Spring Boot (4.1.0):** Modern application framework managing dependency injection, auto-configuration, and actuators.
* **Spring Cloud (2025.1.2):** Distributed system building blocks including Gateway, OpenFeign, and Netflix Eureka.

### Gateway, Discovery & Inter-Service Communication
* **Spring Cloud Gateway Server WebMVC (`spring-cloud-starter-gateway-server-webmvc`):** Servlet-based API Gateway handling edge routing, CORS enforcement, and JWT token interception.
* **Spring Cloud Netflix Eureka (`spring-cloud-starter-netflix-eureka-server` & `client`):** Service registry running on port `8761` for local development. Disabled in Kubernetes environments in favor of Kubernetes CoreDNS.
* **Spring Cloud OpenFeign (`spring-cloud-starter-openfeign`):** Declarative HTTP client enabling synchronous inter-service communication with automated context propagation (`FeignClientInterceptor`).

### Security & Cryptography
* **Java JWT (`io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson:0.12.6`):** Cryptographic token generation, signing, and parsing utilizing HMAC-SHA256 (`HS256`).
* **jBCrypt (`org.mindrot:jbcrypt:0.4`):** One-way cryptographic salting and hashing algorithm for user credentials.

### Data & Persistence
* **PostgreSQL (18-alpine / 18):** Enterprise object-relational database deployed as individual Kubernetes StatefulSets.
* **Spring Data JPA & Hibernate:** ORM layer with automated schema updates (`ddl-auto: update`) and PostgreSQL dialect configurations.
* **Neo4j Community Edition (5.26.3):** Native graph database executing property graph traversals over Bolt binary protocol (Port `7687`).
* **Spring Data Neo4j (SDN):** Graph object-mapping supporting custom Cypher query execution.

### Messaging & Event Streaming
* **Apache Kafka (4.3.1 local / Confluent Local 7.5.0 K8s):** Distributed event streaming platform operating in modern **KRaft mode** (ZooKeeper-less consensus).
* **Spring for Apache Kafka (`spring-kafka`):** Native template abstraction (`KafkaTemplate`), automated topic provisioning (`NewTopic`), and concurrent listeners (`@KafkaListener`).
* **Kafka UI (`provectuslabs/kafka-ui:latest`):** Web-based administrative console for Kafka broker management during local development.

### Cloud Storage & Content Delivery
* **Cloudinary SDK (`com.cloudinary:cloudinary-http5:2.0.0`):** Cloud-hosted media pipeline for image optimization, storage, and secure delivery.

### Containerization, Build & Cloud Infrastructure
* **Google Jib Maven Plugin (`com.google.cloud.tools:jib-maven-plugin:3.4.4`):** Fast, daemonless container image builder producing OCI/Docker container images directly from Maven.
* **Docker & Docker Hub (`docker.io`):** Container virtualization runtime and public repository hosting published images (`aman06383/*`).
* **Docker Compose:** Multi-container local development harness for infrastructure dependencies.
* **Google Kubernetes Engine (GKE):** Managed production Kubernetes cluster platform on Google Cloud.
* **Kubernetes Ingress (GCE Ingress Controller):** Google Cloud HTTP(S) Load Balancer directing external traffic to cluster IP backends.
* **Kubernetes Workloads:** Deployments, StatefulSets, ClusterIP & Headless Services, ConfigMaps, Secrets, and PersistentVolumeClaims (PVCs).

### Developer Tooling & Productivity
* **Project Lombok:** Annotation-based boilerplate reduction (`@Data`, `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`).
* **ModelMapper (3.1.1):** Intelligent object-to-object transformation layer bridging JPA entities and REST DTOs.
* **Apache Maven:** Project management and comprehension tool with included Maven Wrapper (`mvnw`).

---

## 3. Microservices Architecture

### Microservices Catalog

| Service Name | Source Directory | Responsibility | Primary Backends / DB | Communication Partners | K8s Service Name | Port (Local / Target) |
|---|---|---|---|---|---|---|
| **API Gateway** | `apigateway/` | Edge proxy, JWT authentication, CORS, path stripping (`StripPrefix=2`) | None (Stateless) | Routes to User, Post, and Connection services | `api-gateway` | `8080` (K8s: `80 -> 8080`) |
| **Discovery Server** | `discoveryServer/` | Netflix Eureka Service Registry (Local development profile) | In-Memory Registry | Eureka Clients | N/A (K8s uses CoreDNS) | `8761` |
| **User Service** | `userService/` | User registration, authentication, JWT generation, password hashing | PostgreSQL (`userDB`) | Kafka (`user_created_topic`) | `user-service` | `9020` (K8s: `80 -> 9020`) |
| **Post Service** | `postService/` | Post creation, image binding, user feed retrieval, like/unlike tracking | PostgreSQL (`postsDB`) | Uploader Service (Feign), Connection Service (Feign), Kafka (`post_created_topic`, `post_liked_topic`) | `posts-service` & `post-service` | `9010` (K8s: `80 -> 9010`) |
| **Connections Service** | `connectionService/` | Social graph traversal, connection requests (send, accept, reject), friend queries | Neo4j Graph DB (`connections-db`) | Kafka (`user_created_topic` consumer), Inbound Feign from Post Service | `connections-service` | `9030` (K8s: `80 -> 9030`) |
| **Notification Service** | `notification-service/` | Event consumer generating and persisting user notifications | PostgreSQL (`notificationDB`) | Kafka (`post_created_topic`, `post_liked_topic` consumer) | `notification-service` | `9040` (K8s: `80 -> 9040`) |
| **Uploader Service** | `uploader-service/` | Stateless Cloudinary image upload wrapper | Cloudinary CDN (Stateless) | Inbound Feign from Post Service | `uploader-service` | `9050` (K8s: `80 -> 9050`) |

### High-Level Architectural Workflow
1. **User Authentication:** Client calls `POST /api/v1/users/auth/signup` or `login`. Gateway recognizes these as public paths. `userService` creates the user in `userDB`, issues a signed JWT, and emits `UserCreatedEvent` to Kafka.
2. **Graph Synchronization:** `connectionService` consumes `UserCreatedEvent` and generates a corresponding `:Person` node in Neo4j.
3. **Authorized Requests:** For secured endpoints, client attaches `Authorization: Bearer <JWT>`. Gateway validates the signature, extracts the user ID subject, sets `X-User-Id`, strips `/api/v1/*`, and forwards to the target service.
4. **Post Publishing:** Client submits `multipart/form-data` with post metadata and image file to `/api/v1/posts/core`. `postService` forwards the image via OpenFeign to `uploader-service`, which streams it to Cloudinary. `postService` calls `connectionService` via OpenFeign to obtain first-degree friends, saves the post to `postsDB`, and broadcasts `PostCreated` events to Kafka.
5. **Notification Delivery:** `notification-service` listens to `post_created_topic` and `post_liked_topic`, generating notification records in `notificationDB` asynchronously.

### Mermaid System Architecture Diagram

```mermaid
flowchart TD
    %% External Traffic
    Client(["🌐 Client / Mobile App / Web"]) -->|HTTP Requests| Ingress["🚪 Kubernetes Ingress<br/>(GCE Load Balancer: 34.95.113.61)"]
    
    %% Ingress to Gateway
    Ingress -->|Forward :80| Gateway["🛡️ API Gateway<br/>(Spring Cloud Gateway :8080)<br/>• JWT Authentication Interceptor<br/>• CORS & Path Stripping (/api/v1)"]

    %% Gateway to Services
    Gateway -->|/api/v1/users/** -> /users/**| UserSvc["👤 User Service<br/>(Port :9020)"]
    Gateway -->|/api/v1/posts/** -> /posts/**| PostSvc["📝 Post Service<br/>(Port :9010)"]
    Gateway -->|/api/v1/connections/** -> /connections/**| ConnSvc["🤝 Connections Service<br/>(Port :9030)"]

    %% Microservices to Databases
    UserSvc -->|JDBC :5432| UserDB[("🗄️ User DB<br/>PostgreSQL 18<br/>StatefulSet :5432")]
    PostSvc -->|JDBC :5432| PostDB[("🗄️ Posts DB<br/>PostgreSQL 18<br/>StatefulSet :5432")]
    ConnSvc -->|Bolt :7687| NeoDB[("🌐 Connections DB<br/>Neo4j 5.26.3<br/>StatefulSet :7687")]
    NotifSvc["🔔 Notification Service<br/>(Port :9040)"] -->|JDBC :5432| NotifDB[("🗄️ Notification DB<br/>PostgreSQL 18<br/>StatefulSet :5432")]

    %% Synchronous Feign Calls
    PostSvc -.->|Feign: POST /upload/file| UploadSvc["☁️ Uploader Service<br/>(Port :9050)"]
    UploadSvc -.->|HTTPS API| Cloudinary[("☁️ Cloudinary CDN")]
    PostSvc -.->|Feign: GET /connections/core/{id}/first-degree| ConnSvc

    %% Asynchronous Kafka Event Streaming
    subgraph KafkaBroker ["📬 Apache Kafka Cluster (KRaft Mode - StatefulSet :9092)"]
        T1["Topic: user_created_topic"]
        T2["Topic: post_created_topic"]
        T3["Topic: post_liked_topic"]
    end

    UserSvc ==>>|Produces UserCreatedEvent| T1
    T1 ==>>|Consumes| ConnSvc

    PostSvc ==>>|Produces PostCreated| T2
    PostSvc ==>>|Produces PostLiked| T3

    T2 ==>>|Consumes| NotifSvc
    T3 ==>>|Consumes| NotifSvc

    %% Service Registry Note
    subgraph Discovery ["🔍 Local Environment Only"]
        Eureka["Eureka Discovery Server<br/>(Port :8761)"]
    end
```

---

## 4. Complete API Documentation

### Authentication & JWT Security Model

Connect-In employs a stateless, token-based security architecture enforced at the API Gateway:
* **Token Issuance:** Generated upon successful login or signup via `JwtService` using HMAC-SHA256 (`HS256`).
* **Token Expiration:** Configured for **100 minutes** (`System.currentTimeMillis() + 1000 * 60 * 100`).
* **Subject & Claims:** The JWT `subject` is populated with the user ID (`user.getId().toString()`), and contains the custom claim `email`.
* **Gateway Enforcement:** The `AuthenticationFilter` intercepts all incoming requests matching `/api/v1/**`.
  - **Public Whitelist:**
    - `POST /api/v1/users/auth/signup`
    - `POST /api/v1/users/auth/login`
  - **Protected Endpoints:** All other requests require the `Authorization` header formatted as:
    ```http
    Authorization: Bearer <JWT_ACCESS_TOKEN>
    ```
  - **Downstream Identity Propagation:** Upon verifying the token signature against `JWT_SECRET_KEY`, the gateway extracts the subject user ID and injects it downstream in the `X-User-Id` header so downstream services know the calling user's identity without re-parsing the token.

### Standard Error Response Schema
All services leverage a unified `GlobalExceptionHandler` returning an `ApiError` payload:
```json
{
  "timeStamp": "2026-11-20T02:15:30.123456",
  "error": "Error description message",
  "httpStatus": "BAD_REQUEST"
}
```

---

### User Service APIs

Base URL: `http://34.95.113.61/api/v1/users` (Controller Mapping: `/auth`, Context Path: `/users`)

| Method | Endpoint | Controller | Purpose | Auth Requirement |
|---|---|---|---|---|
| `POST` | `/api/v1/users/auth/signup` | `UserController` | Register a new user account | Public |
| `POST` | `/api/v1/users/auth/login` | `UserController` | Authenticate credentials & receive JWT | Public |

#### 1. Register User Account
* **URL:** `POST http://34.95.113.61/api/v1/users/auth/signup`
* **Headers:** `Content-Type: application/json`
* **Request Body (`SignupRequestDto`):**
  ```json
  {
    "name": "Jane Doe",
    "email": "jane.doe@example.com",
    "password": "SecurePassword123!"
  }
  ```
* **Success Response (`201 Created` - `UserDto`):**
  ```json
  {
    "id": 1,
    "name": "Jane Doe",
    "email": "jane.doe@example.com"
  }
  ```
* **Error Responses:**
  - `400 Bad Request` (User already exists):
    ```json
    {
      "timeStamp": "2026-11-20T02:15:30.123456",
      "error": "User already exists",
      "httpStatus": "BAD_REQUEST"
    }
    ```

#### 2. User Login
* **URL:** `POST http://34.95.113.61/api/v1/users/auth/login`
* **Headers:** `Content-Type: application/json`
* **Request Body (`LoginRequestDto`):**
  ```json
  {
    "email": "jane.doe@example.com",
    "password": "SecurePassword123!"
  }
  ```
* **Success Response (`200 OK` - `LoginResponse`):**
  ```json
  {
    "userEmail": "jane.doe@example.com",
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJqYW5lLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTczMjA2MzgzMCwiZXhwIjoxNzMyMDY5ODMwfQ.signature"
  }
  ```
* **Error Responses:**
  - `400 Bad Request` (Invalid credentials):
    ```json
    {
      "timeStamp": "2026-11-20T02:15:30.123456",
      "error": "Incorrect email or password",
      "httpStatus": "BAD_REQUEST"
    }
    ```

---

### Post Service APIs

Base URL: `http://34.95.113.61/api/v1/posts` (Controllers: `/core`, `/likes`, Context Path: `/posts`)

| Method | Endpoint | Controller | Purpose | Auth Requirement |
|---|---|---|---|---|
| `POST` | `/api/v1/posts/core` | `PostController` | Create post with text content and image file | Authenticated |
| `GET` | `/api/v1/posts/core/{postId}` | `PostController` | Retrieve a specific post by ID | Authenticated |
| `GET` | `/api/v1/posts/core/users/{userId}/all-posts` | `PostController` | Retrieve all posts authored by a user | Authenticated |
| `POST` | `/api/v1/posts/likes/{postId}` | `PostLikesController` | Like a specific post | Authenticated |
| `DELETE` | `/api/v1/posts/likes/{postId}` | `PostLikesController` | Remove a like from a post | Authenticated |

#### 3. Create Post
* **URL:** `POST http://34.95.113.61/api/v1/posts/core`
* **Headers:**
  - `Authorization: Bearer <JWT_TOKEN>`
  - `Content-Type: multipart/form-data`
* **Request Parts:**
  - `post`: JSON string of `PostCreateRequestDto` (`Content-Type: application/json`)
    ```json
    {
      "content": "Excited to share our new Kubernetes deployment architecture!"
    }
    ```
  - `file`: Binary image file (`image/png`, `image/jpeg`, etc.)
* **Success Response (`201 Created` - `PostDto`):**
  ```json
  {
    "id": 101,
    "content": "Excited to share our new Kubernetes deployment architecture!",
    "imageUrl": "https://res.cloudinary.com/drsrdkhn7/image/upload/v1732063830/architecture_diagram.png",
    "userId": 1,
    "createdAt": "2026-11-20T02:16:00.000"
  }
  ```
* **Error Responses:**
  - `401 Unauthorized`: Missing or invalid Bearer token.
  - `500 Internal Server Error`: Cloudinary upload failure or downstream Feign timeout.

#### 4. Get Post by ID
* **URL:** `GET http://34.95.113.61/api/v1/posts/core/{postId}`
* **Headers:** `Authorization: Bearer <JWT_TOKEN>`
* **Path Variables:** `postId` (Long) - Unique ID of the post.
* **Success Response (`200 OK` - `PostDto`):**
  ```json
  {
    "id": 101,
    "content": "Excited to share our new Kubernetes deployment architecture!",
    "imageUrl": "https://res.cloudinary.com/drsrdkhn7/image/upload/v1732063830/architecture_diagram.png",
    "userId": 1,
    "createdAt": "2026-11-20T02:16:00.000"
  }
  ```
* **Error Responses:**
  - `404 Not Found`: Post ID does not exist:
    ```json
    {
      "timeStamp": "2026-11-20T02:16:30.123456",
      "error": "Post not found with ID: 101",
      "httpStatus": "NOT_FOUND"
    }
    ```

#### 5. Get All Posts by User
* **URL:** `GET http://34.95.113.61/api/v1/posts/core/users/{userId}/all-posts`
* **Headers:** `Authorization: Bearer <JWT_TOKEN>`
* **Path Variables:** `userId` (Long) - Author user ID.
* **Success Response (`200 OK` - `List<PostDto>`):**
  ```json
  [
    {
      "id": 101,
      "content": "Excited to share our new Kubernetes deployment architecture!",
      "imageUrl": "https://res.cloudinary.com/drsrdkhn7/image/upload/v1732063830/architecture_diagram.png",
      "userId": 1,
      "createdAt": "2026-11-20T02:16:00.000"
    }
  ]
  ```

#### 6. Like Post
* **URL:** `POST http://34.95.113.61/api/v1/posts/likes/{postId}`
* **Headers:** `Authorization: Bearer <JWT_TOKEN>`
* **Path Variables:** `postId` (Long) - ID of the post to like.
* **Success Response (`204 No Content`):** Empty body.
* **Error Responses:**
  - `400 Bad Request` (Attempting to like an already liked post):
    ```json
    {
      "timeStamp": "2026-11-20T02:17:00.123456",
      "error": "You can not like the Post again",
      "httpStatus": "BAD_REQUEST"
    }
    ```
  - `404 Not Found` (Post does not exist):
    ```json
    {
      "timeStamp": "2026-11-20T02:17:00.123456",
      "error": "Post not found with ID: 999",
      "httpStatus": "NOT_FOUND"
    }
    ```

#### 7. Unlike Post
* **URL:** `DELETE http://34.95.113.61/api/v1/posts/likes/{postId}`
* **Headers:** `Authorization: Bearer <JWT_TOKEN>`
* **Path Variables:** `postId` (Long) - ID of the post to unlike.
* **Success Response (`204 No Content`):** Empty body.
* **Error Responses:**
  - `400 Bad Request` (Attempting to unlike a post that was never liked):
    ```json
    {
      "timeStamp": "2026-11-20T02:17:30.123456",
      "error": "You can not unlike the Post that you have not liked.",
      "httpStatus": "BAD_REQUEST"
    }
    ```
  - `404 Not Found` (Post does not exist).

---

### Connections Service APIs

Base URL: `http://34.95.113.61/api/v1/connections` (Controller: `/core`, Context Path: `/connections`)

| Method | Endpoint | Controller | Purpose | Auth Requirement |
|---|---|---|---|---|
| `GET` | `/api/v1/connections/core/{userId}/first-degree` | `ConnectionController` | Fetch first-degree connections of a user | Authenticated |
| `GET` | `/api/v1/connections/core/all` | `ConnectionController` | Retrieve all registered Person nodes | Authenticated |
| `GET` | `/api/v1/connections/core/requests/sent` | `ConnectionController` | View connection requests sent by current user | Authenticated |
| `GET` | `/api/v1/connections/core/requests/received` | `ConnectionController` | View incoming connection requests received | Authenticated |
| `POST` | `/api/v1/connections/core/request/{userId}` | `ConnectionController` | Send connection request to a target user | Authenticated |
| `POST` | `/api/v1/connections/core/accept/{senderId}` | `ConnectionController` | Accept pending connection request | Authenticated |
| `POST` | `/api/v1/connections/core/reject/{senderId}` | `ConnectionController` | Reject pending connection request | Authenticated |
| `DELETE` | `/api/v1/connections/core/remove/{userId}` | `ConnectionController` | Disconnect an established connection | Authenticated |

#### 8. Get First-Degree Connections
* **URL:** `GET http://34.95.113.61/api/v1/connections/core/{userId}/first-degree`
* **Headers:** `Authorization: Bearer <JWT_TOKEN>`
* **Path Variables:** `userId` (Long) - ID of user whose friends are being queried.
* **Success Response (`200 OK` - `List<Person>`):**
  ```json
  [
    {
      "id": 12,
      "userId": 2,
      "name": "John Smith"
    }
  ]
  ```

#### 9. Get All Persons
* **URL:** `GET http://34.95.113.61/api/v1/connections/core/all`
* **Headers:** `Authorization: Bearer <JWT_TOKEN>`
* **Success Response (`200 OK` - `List<Person>`):**
  ```json
  [
    {
      "id": 1,
      "userId": 1,
      "name": "Jane Doe"
    },
    {
      "id": 2,
      "userId": 2,
      "name": "John Smith"
    }
  ]
  ```

#### 10. Get Sent Connection Requests
* **URL:** `GET http://34.95.113.61/api/v1/connections/core/requests/sent`
* **Headers:** `Authorization: Bearer <JWT_TOKEN>`
* **Success Response (`200 OK` - `List<Person>`):**
  ```json
  [
    {
      "id": 2,
      "userId": 2,
      "name": "John Smith"
    }
  ]
  ```

#### 11. Get Received Connection Requests
* **URL:** `GET http://34.95.113.61/api/v1/connections/core/requests/received`
* **Headers:** `Authorization: Bearer <JWT_TOKEN>`
* **Success Response (`200 OK` - `List<Person>`):**
  ```json
  [
    {
      "id": 1,
      "userId": 1,
      "name": "Jane Doe"
    }
  ]
  ```

#### 12. Send Connection Request
* **URL:** `POST http://34.95.113.61/api/v1/connections/core/request/{userId}`
* **Headers:** `Authorization: Bearer <JWT_TOKEN>`
* **Path Variables:** `userId` (Long) - Target recipient user ID.
* **Success Response (`204 No Content`):** Empty body.
* **Error Responses:**
  - `400 Bad Request` (Self-request): `"Both sender and receiver are the same."`
  - `400 Bad Request` (Duplicate request): `"Connection Request already exists, can not send again"`
  - `400 Bad Request` (Already connected): `"Already connected users, cannot add connection request"`

#### 13. Accept Connection Request
* **URL:** `POST http://34.95.113.61/api/v1/connections/core/accept/{senderId}`
* **Headers:** `Authorization: Bearer <JWT_TOKEN>`
* **Path Variables:** `senderId` (Long) - ID of the user who initiated the request.
* **Success Response (`204 No Content`):** Empty body.
* **Error Responses:**
  - `400 Bad Request`: `"No connection request exists, cannot accept without request"`
  - `400 Bad Request`: `"Already connected users, cannot accept connection request again."`

#### 14. Reject Connection Request
* **URL:** `POST http://34.95.113.61/api/v1/connections/core/reject/{senderId}`
* **Headers:** `Authorization: Bearer <JWT_TOKEN>`
* **Path Variables:** `senderId` (Long) - ID of the user who initiated the request.
* **Success Response (`204 No Content`):** Empty body.
* **Error Responses:**
  - `400 Bad Request`: `"No connection request exists, cannot reject it."`

#### 15. Remove Connection
* **URL:** `DELETE http://34.95.113.61/api/v1/connections/core/remove/{userId}`
* **Headers:** `Authorization: Bearer <JWT_TOKEN>`
* **Path Variables:** `userId` (Long) - ID of the connection to terminate.
* **Success Response (`204 No Content`):** Empty body.
* **Error Responses:**
  - `400 Bad Request`: `"The users are not connected, cannot remove connection."`

---

### Internal Microservice APIs (Uploader Service)

> [!NOTE]
> The `uploader-service` is an **internal cluster-scoped microservice** called by `postService` over OpenFeign. It is deliberately not routed through the public API Gateway, preserving encapsulation and microservice boundaries.

* **Internal Endpoint:** `POST http://uploader-service:80/upload/file` (Local: `http://localhost:9050/upload/file`)
* **Controller:** `UploaderController` (`@RequestMapping("/file")`, context-path `/upload`)
* **Headers:** `Content-Type: multipart/form-data`
* **Request Part:** `@RequestPart("file") MultipartFile file`
* **Success Response (`200 OK` - `text/plain`):**
  ```text
  https://res.cloudinary.com/drsrdkhn7/image/upload/v1732063830/sample_image.jpg
  ```

---

## 5. Database Architecture & Persistence

### Relational Tier (PostgreSQL Instances)

To guarantee microservice autonomy and eliminate database-level coupling ("Database-per-Service" pattern), Connect-In deploys three distinct PostgreSQL 18 instances:

```
┌─────────────────────────────────┐   ┌─────────────────────────────────┐   ┌─────────────────────────────────┐
│     userDB (user-db:5432)       │   │    postsDB (posts-db:5432)      │   │ notificationDB (notification-db)│
├─────────────────────────────────┤   ├─────────────────────────────────┤   ├─────────────────────────────────┤
│ TABLE: users                    │   │ TABLE: post                     │   │ TABLE: notification             │
│ • id (PK, BigSerial)            │   │ • id (PK, BigSerial)            │   │ • id (PK, BigSerial)            │
│ • name (varchar, NOT NULL)      │   │ • content (varchar, NOT NULL)   │   │ • userId (bigint, NOT NULL)     │
│ • email (varchar, UNIQUE)       │   │ • imageUrl (varchar)            │   │ • message (varchar)             │
│ • password (varchar, NOT NULL)  │   │ • userId (bigint, NOT NULL)     │   │ • createdAt (timestamp)         │
│                                 │   │ • createdAt (timestamp)         │   │                                 │
│                                 │   │                                 │   │                                 │
│                                 │   │ TABLE: post_likes               │   │                                 │
│                                 │   │ • id (PK, BigSerial)            │   │                                 │
│                                 │   │ • userId (bigint, NOT NULL)     │   │                                 │
│                                 │   │ • postId (bigint, NOT NULL)     │   │                                 │
│                                 │   │ • createdAt (timestamp)         │   │                                 │
└─────────────────────────────────┘   └─────────────────────────────────┘   └─────────────────────────────────┘
```

### Graph Tier (Neo4j Social Graph)

The social graph is managed by Neo4j (`connections-db:7687`), running Community Edition 5.26.3. Relational databases require recursive $N$-table self-joins to resolve friendship networks, resulting in exponential $O(b^d)$ query latency. Neo4j executes native index-free adjacency graph queries in linear time proportional to the degree of the node:

* **Node Label:** `(:Person)`
  - Properties: `id` (Internal Graph ID), `userId` (Long), `name` (String)
* **Relationship Types:**
  - `(:Person)-[:CONNECTED_TO]->(:Person)`: Represents an established mutual connection.
  - `(:Person)-[:REQUESTED_TO]->(:Person)`: Represents a pending outbound connection request.

#### Cypher Query Implementations
* **Retrieve First-Degree Connections:**
  ```cypher
  MATCH (p:Person {userId: $userId})-[:CONNECTED_TO]->(friend:Person)
  RETURN friend
  ```
* **Verify Connection Status:**
  ```cypher
  MATCH (p1:Person)-[r:CONNECTED_TO]->(p2:Person) 
  WHERE p1.userId = $senderId AND p2.userId = $receiverId
  RETURN count(r) > 0
  ```
* **Accept Connection Request (Atomic Transition):**
  ```cypher
  MATCH (p1:Person)-[r:REQUESTED_TO]->(p2:Person) 
  WHERE p1.userId = $senderId AND p2.userId = $receiverId
  DELETE r 
  CREATE (p1)-[:CONNECTED_TO]->(p2)
  ```

### Kubernetes StatefulSets & Volume Storage (PVC)

Databases are deployed as **Kubernetes StatefulSets** rather than Deployments.

#### Why StatefulSets for Databases?
1. **Stable Network Identifiers:** Each database pod receives an immutable ordinal hostname (`user-db-0`, `posts-db-0`, `connections-db-0`, `notification-db-0`) managed by Headless Services (`clusterIP: None`), preventing DNS re-resolution errors when pods restart.
2. **Dedicated Storage per Replica:** Deployments share persistent volumes or risk concurrent mount race conditions. StatefulSets use `volumeClaimTemplates` to automatically provision an independent persistent disk per pod.
3. **Graceful Startup & Shutdown:** StatefulSets enforce strict ordering guarantees, preventing data corruption during cluster scaling or rolling node upgrades.

#### Storage Specifications (GKE)
* **Storage Class:** `standard-rwo` (Google Cloud Compute Engine Persistent Disk - ReadWriteOnce).
* **Allocations:**
  - `user-db`: `1Gi` mounted at `/var/lib/postgresql/data`
  - `posts-db`: `1Gi` mounted at `/var/lib/postgresql/data`
  - `notification-db`: `1Gi` mounted at `/var/lib/postgresql/data`
  - `connections-db`: `2Gi` mounted at `/data` + `1Gi` mounted at `/logs`

---

## 6. Local Development Setup

### Prerequisites
* **Java:** OpenJDK 21 or higher (`java -version`)
* **Build Tool:** Apache Maven 3.9+ (or use the included `./mvnw` wrappers)
* **Containers:** Docker Engine 24+ & Docker Compose v2+
* **Version Control:** Git

---

### Step-by-Step Installation & Running Guide

#### 1. Clone the Repository
```bash
git clone https://github.com/gitcoder-aman/ConnectIn.git
cd ConnectIn
```

#### 2. Start Supporting Infrastructure (Kafka & Kafka UI)
The repository provides a `docker-compose.yml` preconfigured for Apache Kafka in KRaft mode:
```bash
docker compose up -d
```
Verify containers are healthy:
```bash
docker ps
```
* **Apache Kafka (Broker):** `localhost:9092`
* **Kafka UI:** `http://localhost:8081`

#### 3. Start Local Databases (PostgreSQL & Neo4j)
Run local containerized databases matching application expectations:
```bash
# 1. User Database
docker run -d --name user-db -p 5432:5432 \
  -e POSTGRES_USER=user \
  -e POSTGRES_PASSWORD=3092 \
  -e POSTGRES_DB=userDB postgres:18-alpine

# 2. Posts Database (port 5433 to prevent collisions locally)
docker run -d --name posts-db -p 5433:5432 \
  -e POSTGRES_USER=user \
  -e POSTGRES_PASSWORD=3092 \
  -e POSTGRES_DB=postsDB postgres:18-alpine

# 3. Notification Database (port 5434)
docker run -d --name notification-db -p 5434:5432 \
  -e POSTGRES_USER=user \
  -e POSTGRES_PASSWORD=3092 \
  -e POSTGRES_DB=notificationDB postgres:18-alpine

# 4. Neo4j Graph Database
docker run -d --name connections-db -p 7474:7474 -p 7687:7687 \
  -e NEO4J_AUTH=neo4j/09876543 \
  -e NEO4J_server_memory_heap_initial__size=512M \
  -e NEO4J_server_memory_heap_max__size=1G neo4j:5.26.3-community
```

#### 4. Configure Local Environment Variables
Export configuration variables in your shell or IDE run configurations:
```bash
export JWT_SECRET_KEY="asfhjsadfjhsdafsdajhfjasdhgfsdahvfnmswjhrhiuewafkmnsadkfjsad"
export CLOUD_NAME="drsrdkhn7"
export API_KEY="567275729665999"
export API_SECRET="HyMdGjnOfnx3GmLdLmmzV-FiduI"
```

#### 5. Build All Microservices
Execute Maven compilation across the microservice modules:
```bash
for service in discoveryServer apigateway userService postService connectionService notification-service uploader-service; do
  echo "Building $service..."
  (cd "$service" && ./mvnw clean compile)
done
```

#### 6. Microservice Startup Sequence
Start the applications in order:

1. **Discovery Server (Port 8761):**
   ```bash
   cd discoveryServer && ./mvnw spring-boot:run
   ```
   *Dashboard available at:* `http://localhost:8761`

2. **API Gateway (Port 8080):**
   ```bash
   cd apigateway && ./mvnw spring-boot:run
   ```

3. **Domain Microservices (Open in separate terminal tabs):**
   ```bash
   # User Service (Port 9020)
   cd userService && DB_URL="jdbc:postgresql://localhost:5432/userDB" DB_USERNAME="user" DB_PASSWORD="3092" ./mvnw spring-boot:run

   # Connections Service (Port 9030)
   cd connectionService && ./mvnw spring-boot:run

   # Uploader Service (Port 9050)
   cd uploader-service && ./mvnw spring-boot:run

   # Post Service (Port 9010)
   cd postService && DB_URL="jdbc:postgresql://localhost:5433/postsDB" DB_USERNAME="user" DB_PASSWORD="3092" \
     CONNECTION_SERVICE_URI="http://localhost:9030" \
     UPLOADER_SERVICE_URI="http://localhost:9050" ./mvnw spring-boot:run

   # Notification Service (Port 9040)
   cd notification-service && DB_URL="jdbc:postgresql://localhost:5434/notificationDB" DB_USERNAME="user" DB_PASSWORD="3092" ./mvnw spring-boot:run
   ```

---

## 7. Docker & Containerization Implementation

### Daemonless Image Creation with Google Jib

Connect-In employs **Google Jib** (`jib-maven-plugin:3.4.4`) rather than traditional Dockerfiles.

#### Why Jib over Dockerfiles?
* **No Docker Daemon Required:** Builds OCI-compliant container images directly without root privileges, Docker CLI, or Docker daemon access.
* **Intelligent Layering:** Separates application code into immutable layers (dependencies, resources, classes). Only changed Java class files are re-uploaded during builds, reducing image push times from minutes to seconds.
* **Deterministic & Reproducible:** Eliminates OS cache inconsistencies across development workstations.

#### Configuration in `pom.xml`
Every service declares Jib bound to the Maven `package` phase:
```xml
<plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>jib-maven-plugin</artifactId>
    <version>3.4.4</version>
    <configuration>
        <to>
            <image>docker.io/aman06383/connectin-app-${project.name}:${project.version}</image>
            <tags>
                <tag>latest</tag>
            </tags>
        </to>
    </configuration>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>build</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Build & Push Commands

#### Push to Docker Hub Registry
```bash
# Log in to Docker Hub
docker login

# Build and push an individual service
cd userService
mvn compile jib:build

# Or build and push all services simultaneously
for dir in apigateway connectionService notification-service postService uploader-service userService; do
  echo "Packaging & pushing $dir to Docker Hub..."
  (cd "$dir" && mvn compile jib:build)
done
```

#### Build Directly into Local Docker Daemon (No Push)
If you wish to test images locally via Docker without pushing to Docker Hub:
```bash
cd userService
mvn compile jib:dockerBuild
```

#### Published Image Registry Tags
* `docker.io/aman06383/connectin-app-api-gateway:latest`
* `docker.io/aman06383/connectin-app-user-service:latest`
* `docker.io/aman06383/connectin-app-posts-service:latest`
* `docker.io/aman06383/connectin-app-connections-service:latest`
* `docker.io/aman06383/connectin-app-notification-service:latest`
* `docker.io/aman06383/connectin-app-uploader-service:latest`

---

## 8. Kubernetes (K8s) Deployment Architecture

### Workload Topologies: Deployments vs. StatefulSets

The cluster topology in `k8s/` strictly enforces container lifecycle segregation:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        KUBERNETES CLUSTER                              │
│                                                                        │
│   [ GCE Ingress: myingress ]                                           │
│         │                                                              │
│         ▼                                                              │
│   [ ClusterIP Service: api-gateway:80 ]                                │
│         │                                                              │
│  ───────┴───────────────────────────────┬────────────────────────────  │
│  DEPLOYMENTS (Stateless Services)       │ STATEFULSETS (Databases)     │
│                                         │                              │
│  • api-gateway                          │  • user-db (PostgreSQL 18)   │
│    (Limits: 500Mi RAM / 200m CPU)       │    (PVC: 1Gi standard-rwo)   │
│                                         │                              │
│  • user-service                         │  • posts-db (PostgreSQL 18)  │
│    (Limits: 400Mi RAM / 100m CPU)       │    (PVC: 1Gi standard-rwo)   │
│                                         │                              │
│  • posts-service                        │  • notification-db (Postgres)│
│    (Limits: 400Mi RAM / 100m CPU)       │    (PVC: 1Gi standard-rwo)   │
│                                         │                              │
│  • connections-service                  │  • connections-db (Neo4j)    │
│    (Limits: 400Mi RAM / 100m CPU)       │    (PVCs: 2Gi data, 1Gi log) │
│                                         │                              │
│  • notification-service                 │  • kafka (Confluent 7.5.0)   │
│    (Limits: 400Mi RAM / 100m CPU)       │    (2 Replicas, KRaft Mode)  │
│                                         │                              │
│  • uploader-service                     │                              │
│    (Limits: 400Mi RAM / 100m CPU)       │                              │
└─────────────────────────────────────────┴──────────────────────────────┘
```

### Networking, DNS, & Ingress Routing
* **Service Discovery:** Handled automatically by Kubernetes CoreDNS. Services resolve each other by internal hostnames:
  - `http://user-service`
  - `http://post-service` (and alias `http://posts-service`)
  - `http://connections-service`
  - `http://notification-service`
  - `http://uploader-service`
  - `bolt://connections-db:7687`
  - `jdbc:postgresql://user-db:5432/userDB`
* **GCE Ingress (`k8s/ingress.yml`):**
  - Allocates an external Google Cloud IP (`34.95.113.61`).
  - Routes root path prefix `/` to backend service `api-gateway` on port `80`.
* **Readiness Probes:** `api-gateway` defines an HTTP probe on `/actuator/health` to guarantee zero-downtime traffic switching.

### Manifest Registry & Inspection Commands

#### Manifest Inventory (`k8s/`)
1. `ingress.yml`: GCE Ingress configuration.
2. `api-gateway.yml`: API Gateway Deployment & Service.
3. `user-service.yml`: User Service Deployment & Service.
4. `user-db.yml`: PostgreSQL StatefulSet & Headless Service for `userDB`.
5. `posts-service.yml`: Post Service Deployment and Services (`posts-service` and `post-service`).
6. `posts-db.yml`: PostgreSQL StatefulSet & Headless Service for `postsDB`.
7. `connections-service.yml`: Connections Service Deployment & Service.
8. `connections-db.yml`: Neo4j StatefulSet with volume templates & Headless Service.
9. `notification-service.yml`: Notification Service Deployment & Service.
10. `notification-db.yml`: PostgreSQL StatefulSet & Headless Service for `notificationDB`.
11. `uploader-service.yml`: Cloudinary Secret, Deployment, and Service.
12. `kafka.yml`: 2-node Kafka KRaft cluster StatefulSet & Headless Service.

#### Operational Commands
```bash
# Apply entire cluster configuration
kubectl apply -f k8s/

# Verify Workloads
kubectl get pods -o wide
kubectl get deployments
kubectl get statefulsets

# Verify Networking & Storage
kubectl get services
kubectl get ingress
kubectl get pvc

# Stream Logs from Specific Microservice
kubectl logs -f deployment/api-gateway
kubectl logs -f deployment/posts-service
kubectl logs -f statefulset/kafka

# Inspect Events on a Pod
kubectl describe pod api-gateway-<pod-hash>
```

---

## 9. Google Kubernetes Engine (GKE) Deployment Guide

### Cloud Deployment Workflow

The production cluster is deployed on Google Cloud Platform with external ingress at `http://34.95.113.61/`.

```
[ Developer ]
     │
     ├── 1. mvn compile jib:build ────▶ Docker Hub (aman06383/connectin-app-*)
     │
     ├── 2. gcloud container clusters create ...
     │
     ├── 3. kubectl apply -f k8s/ ────▶ GKE Master Node
     │                                     │
     │   ┌─────────────────────────────────┴─────────────────────────────────┐
     │   ▼                                                                   ▼
     │ [ Stateful Infrastructure ]                               [ Microservice Deployments ]
     │ • kafka (StatefulSet)                                     • api-gateway
     │ • user-db, posts-db, notification-db                      • user-service, posts-service
     │ • connections-db (Neo4j)                                  • connections-service
     │ • GKE Persistent Disks (standard-rwo)                     • notification-service, uploader
     │                                                                       │
     └── 4. GCE Ingress Controller ──────────────────────────────────────────┘
             │ Provisions External IP: 34.95.113.61
             ▼
       Public Internet Traffic Routed Successfully
```

---

### Step-by-Step GKE Provisioning Commands

#### 1. Set Google Cloud Project & Compute Zone
```bash
gcloud auth login
gcloud config set project <YOUR_GCP_PROJECT_ID>
gcloud config set compute/zone us-central1-a
```

#### 2. Enable Required Google Cloud APIs
```bash
gcloud services enable container.googleapis.com compute.googleapis.com
```

#### 3. Create GKE Cluster
```bash
gcloud container clusters create connectin-cluster \
    --num-nodes=3 \
    --machine-type=e2-standard-4 \
    --disk-size=50GB \
    --zone=us-central1-a
```

#### 4. Configure Kubectl Context
```bash
gcloud container clusters get-credentials connectin-cluster --zone us-central1-a
```

#### 5. Deploy Persistent Storage & Databases First
```bash
kubectl apply -f k8s/kafka.yml
kubectl apply -f k8s/user-db.yml
kubectl apply -f k8s/posts-db.yml
kubectl apply -f k8s/notification-db.yml
kubectl apply -f k8s/connections-db.yml
```
*Wait for databases to initialize:*
```bash
kubectl rollout status statefulset/kafka
kubectl rollout status statefulset/user-db
kubectl rollout status statefulset/posts-db
kubectl rollout status statefulset/notification-db
kubectl rollout status statefulset/connections-db
```

#### 6. Deploy Secrets & Microservices
```bash
kubectl apply -f k8s/uploader-service.yml
kubectl apply -f k8s/user-service.yml
kubectl apply -f k8s/posts-service.yml
kubectl apply -f k8s/connections-service.yml
kubectl apply -f k8s/notification-service.yml
kubectl apply -f k8s/api-gateway.yml
```

#### 7. Deploy Ingress Controller
```bash
kubectl apply -f k8s/ingress.yml
```

#### 8. Verify External IP Allocation
```bash
kubectl get ingress myingress --watch
```
Once the external IP appears as `34.95.113.61`, all public routes are accessible!

---

## 10. End-to-End API Testing with cURL

Below is a complete test script using the deployed base URL `http://34.95.113.61/`.

### 1. Register User A (Alice)
```bash
curl -X POST http://34.95.113.61/api/v1/users/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Cooper",
    "email": "alice@example.com",
    "password": "Password123!"
  }'
```

### 2. Register User B (Bob)
```bash
curl -X POST http://34.95.113.61/api/v1/users/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bob Martin",
    "email": "bob@example.com",
    "password": "Password123!"
  }'
```

### 3. Log In User A (Retrieve JWT)
```bash
curl -X POST http://34.95.113.61/api/v1/users/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "Password123!"
  }'
```
*Store the returned `accessToken` in an environment variable:*
```bash
export TOKEN_A="<INSERT_ALICE_ACCESS_TOKEN>"
```

### 4. Log In User B (Retrieve JWT)
```bash
curl -X POST http://34.95.113.61/api/v1/users/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "bob@example.com",
    "password": "Password123!"
  }'
```
*Store the returned `accessToken`:*
```bash
export TOKEN_B="<INSERT_BOB_ACCESS_TOKEN>"
```

### 5. Send Connection Request (Alice ➔ Bob: ID 2)
```bash
curl -X POST http://34.95.113.61/api/v1/connections/core/request/2 \
  -H "Authorization: Bearer $TOKEN_A"
```

### 6. Bob Checks Received Requests
```bash
curl -X GET http://34.95.113.61/api/v1/connections/core/requests/received \
  -H "Authorization: Bearer $TOKEN_B"
```

### 7. Bob Accepts Connection Request from Alice (ID 1)
```bash
curl -X POST http://34.95.113.61/api/v1/connections/core/accept/1 \
  -H "Authorization: Bearer $TOKEN_B"
```

### 8. Verify Mutual First-Degree Connection
```bash
curl -X GET http://34.95.113.61/api/v1/connections/core/1/first-degree \
  -H "Authorization: Bearer $TOKEN_A"
```

### 9. Alice Publishes a Post with Image
```bash
curl -X POST http://34.95.113.61/api/v1/posts/core \
  -H "Authorization: Bearer $TOKEN_A" \
  -F 'post={"content": "Hello Connect-In network! Here is our cluster setup."};type=application/json' \
  -F 'file=@/path/to/diagram.png'
```

### 10. Retrieve the Created Post by ID
```bash
curl -X GET http://34.95.113.61/api/v1/posts/core/1 \
  -H "Authorization: Bearer $TOKEN_A"
```

### 11. Bob Likes Alice's Post
```bash
curl -X POST http://34.95.113.61/api/v1/posts/likes/1 \
  -H "Authorization: Bearer $TOKEN_B"
```
*(This triggers a `PostLiked` event on Kafka, which `notification-service` persists in `notificationDB`)*

### 12. Bob Unlikes Alice's Post
```bash
curl -X DELETE http://34.95.113.61/api/v1/posts/likes/1 \
  -H "Authorization: Bearer $TOKEN_B"
```

---

## 11. Project Directory Structure

```
ConnectIn/
├── .gitignore
├── docker-compose.yml              # Local infrastructure orchestration (Kafka 4.3.1 & Kafka UI)
├── k8s/                            # Production Kubernetes & GKE manifests
│   ├── api-gateway.yml             # API Gateway Deployment & Service
│   ├── connections-db.yml          # Neo4j StatefulSet, PVCs, & Headless Service
│   ├── connections-service.yml     # Connections Service Deployment & Service
│   ├── ingress.yml                 # GCE Ingress configuration (Public IP: 34.95.113.61)
│   ├── kafka.yml                   # 2-broker Kafka StatefulSet (KRaft Mode)
│   ├── notification-db.yml         # Notification Postgres StatefulSet, PVC, & Headless Service
│   ├── notification-service.yml    # Notification Service Deployment & Service
│   ├── posts-db.yml                # Posts Postgres StatefulSet, PVC, & Headless Service
│   ├── posts-service.yml           # Post Service Deployment & Services (post-service, posts-service)
│   ├── uploader-service.yml        # Cloudinary Secret, Uploader Deployment, & Service
│   ├── user-db.yml                 # User Postgres StatefulSet, PVC, & Headless Service
│   └── user-service.yml            # User Service Deployment & Service
├── apigateway/                     # Spring Cloud Gateway WebMVC
│   ├── pom.xml                     # Gateway dependencies & Jib plugin
│   └── src/main/
│       ├── java/com/tech/connectIn/apigateway/
│       │   ├── ApigatewayApplication.java
│       │   ├── config/WebConfig.java             # Gateway interceptor registry & CORS setup
│       │   └── filter/
│       │       ├── AuthenticationFilter.java    # JWT verification & X-User-Id header enrichment
│       │       └── JwtService.java              # JJWT claim parsing
│       └── resources/
│           ├── application.yaml                 # Local routes, ports, & Eureka config
│           └── application-k8s.yml              # K8s profile route URIs
├── discoveryServer/                # Netflix Eureka Service Discovery (Local Dev)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/tech/connectIn/discoveryServer/DiscoveryServerApplication.java
│       └── resources/application.yaml           # Eureka server configuration (Port 8761)
├── userService/                    # Identity & User Management Microservice
│   ├── pom.xml                     # JPA, WebMVC, Kafka, JBCrypt, Jib
│   └── src/main/
│       ├── java/com/tech/connectIn/
│       │   ├── userService/
│       │   │   ├── UserServiceApplication.java
│       │   │   ├── config/KafkaConfig.java      # user_created_topic definition
│       │   │   ├── controller/UserController.java# /auth/signup & /auth/login
│       │   │   ├── dto/                         # SignupRequestDto, LoginResponse, UserDto
│       │   │   ├── entity/User.java             # JPA Entity mapped to 'users' table
│       │   │   ├── event/UserCreatedEvent.java  # Kafka event contract
│       │   │   ├── repository/UserRepository.java
│       │   │   └── service/                     # AuthService & JwtService implementations
│       │   └── utils/BCryptUtil.java            # One-way password hashing utility
│       └── resources/
│           ├── application.yaml
│           ├── application-k8s.yml
│           └── kafka.yaml                       # Kafka Producer serializer configuration
├── postService/                    # Post Publishing & Likes Microservice
│   ├── pom.xml                     # JPA, OpenFeign, Kafka, Jib
│   └── src/main/
│       ├── java/com/tech/connectIn/postService/
│       │   ├── PostServiceApplication.java
│       │   ├── auth/                            # RequestInterceptor, FeignClientInterceptor
│       │   ├── client/                          # ConnectionServiceClient, UploaderServiceClient
│       │   ├── config/KafkaConfig.java          # post_created_topic & post_liked_topic
│       │   ├── controller/
│       │   │   ├── PostController.java          # /core post endpoints
│       │   │   └── PostLikesController.java     # /likes post endpoints
│       │   ├── dto/                             # PostCreateRequestDto, PostDto, PersonDto
│       │   ├── entity/Post.java, PostLike.java  # JPA Entities ('post', 'post_likes')
│       │   ├── event/                           # PostCreated, PostLiked Kafka events
│       │   ├── repository/                      # PostRepository, PostLikeRepository
│       │   └── service/impl/                    # PostServiceImpl, PostLikeServiceImpl
│       └── resources/
│           ├── application.yaml
│           ├── application-k8s.yml
│           └── kafka.yaml
├── connectionService/              # Social Graph Microservice
│   ├── pom.xml                     # Spring Data Neo4j, Kafka Consumer, Jib
│   └── src/main/
│       ├── java/com/tech/connectIn/connectionService/
│       │   ├── ConnectionServiceApplication.java
│       │   ├── auth/RequestInterceptor.java     # X-User-Id header context holder
│       │   ├── consumer/UserServiceConsumer.java# Consumes user_created_topic -> creates :Person
│       │   ├── controller/ConnectionController.java # /core connection endpoints
│       │   ├── entity/Person.java               # SDN Node (:Person)
│       │   ├── repository/PersonRepository.java # Neo4j Cypher graph queries
│       │   └── service/impl/                    # ConnectionServiceImpl, PersonServiceImpl
│       └── resources/
│           ├── application.yaml
│           ├── application-k8s.yml
│           └── kafka.yaml                       # Kafka Consumer deserializer config
├── notification-service/           # Asynchronous Notification Ingestion Microservice
│   ├── pom.xml                     # JPA, Kafka Consumer, Postgres, Jib
│   └── src/main/
│       ├── java/com/tech/connectIn/notification_service/
│       │   ├── NotificationServiceApplication.java
│       │   ├── consumer/PostsConsumer.java      # Consumes post_created_topic & post_liked_topic
│       │   ├── entity/Notification.java         # JPA Entity ('notification')
│       │   ├── repository/NotificationRepository.java
│       │   └── service/impl/NotificationServiceImpl.java
│       └── resources/
│           ├── application.yaml
│           ├── application-k8s.yml
│           └── kafka.yaml
└── uploader-service/               # Stateless Cloudinary Media Storage Microservice
    ├── pom.xml                     # Cloudinary HTTP5 SDK, WebMVC, Jib
    └── src/main/
        ├── java/com/tech/connectIn/uploader_service/
        │   ├── UploaderServiceApplication.java
        │   ├── config/UploaderConfig.java       # Cloudinary bean configuration
        │   ├── controller/UploaderController.java# /file multipart endpoint
        │   └── service/impl/CloudinaryUploaderService.java
        └── resources/
            ├── application.yaml
            └── application-k8s.yml
```

---

## 12. Troubleshooting & Production Operations

### 1. `ImagePullBackOff` or `ErrImagePull`
* **Symptom:** Microservice pods remain in `ImagePullBackOff` or `ErrImagePull` status.
* **Root Cause:** Incorrect Docker image tag or missing Docker Hub repository credentials.
* **Remediation:**
  ```bash
  # Check failure event description
  kubectl describe pod <pod-name>
  
  # Ensure the image was built and published with Jib:
  mvn compile jib:build -Dimage=docker.io/aman06383/<image-name>:latest
  ```

### 2. `CrashLoopBackOff` on Microservice Pods
* **Symptom:** Microservice starts and immediately terminates.
* **Root Cause:** Microservice starts before its backing database or Kafka broker is accessible, causing Spring context initialization to fail.
* **Remediation:**
  ```bash
  # Check startup logs
  kubectl logs <pod-name> --previous
  
  # Verify database readiness:
  kubectl get pods -l app=user-db
  
  # Ensure SPRING_PROFILES_ACTIVE is set to 'k8s'
  ```

### 3. Database Connection Failures
* **Symptom:** `org.postgresql.util.PSQLException: Connection refused` or `org.neo4j.driver.exceptions.ServiceUnavailableException`.
* **Root Cause:** Pod attempting to connect to `localhost` rather than the Kubernetes headless service name.
* **Remediation:** Verify environment variables in the Deployment spec:
  - User DB: `DB_SERVER=user-db`, `DB_NAME=userDB`, `DB_USERNAME=user`, `DB_PASSWORD=3092`
  - Posts DB: `DB_SERVER=posts-db`, `DB_NAME=postsDB`
  - Neo4j: `DB_NAME=connections-db`, `DB_PASSWORD=1234567890`

### 4. Kubernetes Service DNS Resolution Issues
* **Symptom:** `UnknownHostException: post-service` or `user-service`.
* **Root Cause:** CoreDNS transient failure or mismatched service name. Notice that `posts-service.yml` defines two services (`posts-service` and `post-service`) to satisfy both routing conventions.
* **Remediation:** Test DNS resolution from a debug pod:
  ```bash
  kubectl run curl-test --rm -it --image=curlimages/curl -- sh
  # Inside the test pod:
  nslookup user-service
  nslookup post-service
  nslookup kafka
  ```

### 5. Kafka Broker Connection & KRaft Quorum Failures
* **Symptom:** `org.apache.kafka.common.errors.TimeoutException: Topic not present in metadata`.
* **Root Cause:** StatefulSet ordinal pods `kafka-0` and `kafka-1` failed KRaft voter consensus.
* **Remediation:** Check KRaft initialization script in `k8s/kafka.yml`:
  ```bash
  kubectl logs kafka-0 -c kafka
  ```
  Ensure `KAFKA_CONTROLLER_QUORUM_VOTERS` points to `0@kafka-0.kafka:29093,1@kafka-1.kafka:29093`.

### 6. GCE Ingress 502 Bad Gateway / Health Check Failures
* **Symptom:** Ingress returns `HTTP 502 Bad Gateway` when accessing `http://34.95.113.61/`.
* **Root Cause:** GCE Ingress requires backend services to respond with `200 OK` to Google Cloud health check probes. If `api-gateway` takes longer than expected to start up, GCE marks the backend unhealthy.
* **Remediation:** Inspect `api-gateway.yml`:
  ```yaml
  readinessProbe:
    httpGet:
      path: /actuator/health
      port: 8080
    initialDelaySeconds: 300
    periodSeconds: 20
    timeoutSeconds: 20
  ```
  Allow the initial 300-second warm-up window for GCE load balancer health probes to stabilize.

### 7. PersistentVolumeClaim Pending State
* **Symptom:** Database pod stuck in `Pending`. `kubectl describe pvc` shows `waiting for first consumer` or `storageclass not found`.
* **Root Cause:** GKE regional vs zonal persistent disk mismatch or missing storage class.
* **Remediation:** Ensure the GKE cluster supports `storageClassName: "standard-rwo"`. If using regional clusters, ensure nodes exist in the same availability zone as the PVC.

---

## 13. Future Architectural Roadmap

1. **Notification REST Controller & Feed Aggregator:**
   - Implement read endpoints on `notification-service` (`GET /api/v1/notifications`) so users can view their notification history in UI applications.
   - Introduce an asynchronous Feed Service aggregating posts based on graph depth and engagement metrics.
2. **Real-Time Push Notifications via WebSockets / SSE:**
   - Integrate Spring WebFlux or STOMP WebSockets into the notification layer to push events directly to active browser sessions without client polling.
3. **Resilience & Fault Tolerance:**
   - Add **Resilience4j** Circuit Breakers and Fallbacks across OpenFeign inter-service clients (`PostService ➔ UploaderService` and `PostService ➔ ConnectionService`) to handle downstream timeouts gracefully.
4. **Distributed Tracing & Centralized Observability:**
   - Integrate **Micrometer Tracing** with OpenTelemetry and Jaeger/Zipkin to propagate correlation IDs across HTTP headers and Kafka message metadata.
   - Deploy Prometheus & Grafana to monitor JVM memory, Kafka consumer lag, and database connection pools.
5. **TLS / HTTPS Ingress Termination:**
   - Configure **cert-manager** with Let's Encrypt to provision automatic SSL/TLS certificates on the GCE Ingress controller, upgrading `http://34.95.113.61/` to `https://`.
6. **Automated CI/CD Delivery Pipeline:**
   - Establish GitHub Actions workflows to automate unit testing, Jib container image publishing upon Git pushes to `main`, and automated rollout updates in GKE.

---

## License & Author
* **Project Name:** Connect-In
* **Architect & Developer:** Aman Kumar ([@gitcoder-aman](https://github.com/gitcoder-aman))
* **License:** [Apache License 2.0](LICENSE)
