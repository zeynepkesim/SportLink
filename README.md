# SportLink

Microservices-based platform for sports matchmaking, activity organization, and facility booking. Course project for *Enterprise System Integration* (University of Tartu, 2026).

## Team

| Member | Service 1 | Service 2 / Component |
|---|---|---|
| Olena Ushenko | Booking Service | Search Service + RabbitMQ (shared) |
| Kateryna Manoilova | Matching Service | Rating Management Service |
| Zeynep Kesim | Account Management Service | Moderation Service + Resilience4j (shared) |
| Kirill Ryrmak | Activity Management Service | Scheduling Service |
| Muhammad Ahmed Khan | Interaction Service | Notification Service |

## Quick Start

Boot the whole stack (Kafka, both databases, both services) and the frontend:

```bash
docker compose up -d --build
cd frontend && npm install && npm run serve
```

Then open:

| URL | What |
|---|---|
| http://localhost:8080 | Vue frontend (sign in below) |
| http://localhost:8081/swagger-ui/index.html | Account Management Swagger UI |
| http://localhost:8090/swagger-ui/index.html | Moderation Swagger UI |

Demo accounts seeded automatically (`DatabaseInitializer`):

| Email | Password | Role |
|---|---|---|
| `moderator@sportlink.test` | `moderatorPass1` | MODERATOR |
| `demo@sportlink.test` | `demoPassword1` | USER |
| `organizer@sportlink.test` | `organizerPass1` | ORGANIZER |
| `info@tartusportshall.test` | `facilityPass1` | FACILITY |

## Repository Layout

```
sportlink-project/
├── account-management-service/     # Zeynep — registration, login, profile management
├── activity-management-service/    # Kirill
├── booking-service/                # Olena
├── interaction-service/            # Muhammad
├── moderation-service/             # Zeynep — content moderation cases, account sanctions
├── notification-service/           # Muhammad
├── rating-management-service/      # Kateryna
├── scheduling-service/             # Kirill
├── frontend/                       # Shared Vue 3 SPA (team)
├── docker-compose.yml              # Local dev orchestration (PostgreSQL + services)
├── .gitlab-ci.yml                  # CI: per-service build jobs + SAST + secret detection
└── README.md
```

Each service is autonomous, has its own database, and is deployed independently. The team shares a single Vue 3 frontend (`frontend/`) where each member adds the views that exercise their backend endpoints.

## Account Management Service (Zeynep)

Central identity provider of the platform. Responsible for:
- User registration and authentication
- Sport center (facility) registration and authentication
- Profile reads and updates
- Account status changes (ACTIVE / SUSPENDED / BANNED), called by the Moderation Service

Built with Spring Boot 3, Java 17, PostgreSQL 15, Spring Data JPA, BCrypt password hashing, and springdoc-openapi for Swagger UI.

### Endpoints (8 total, matching Assignment 3)

| Method | Path | Description |
|---|---|---|
| POST | `/api/users` | Register a new user |
| POST | `/api/auth/login` | Authenticate user or sport center |
| GET | `/api/users/{id}` | Get user profile |
| PUT | `/api/users/{id}` | Update user profile |
| PUT | `/api/users/{id}/status` | Update user status (ACTIVE / SUSPENDED / BANNED) |
| POST | `/api/sport-centers` | Register a new sport center |
| GET | `/api/sport-centers/{id}` | Get facility profile |
| PUT | `/api/sport-centers/{id}` | Update facility profile |

Swagger UI: http://localhost:8081/swagger-ui/index.html

### Architecture Layers

```
controller/   — REST API (Spring MVC, validation via @Valid)
dto/          — request/response DTOs (decouples API from domain model)
service/      — business logic (UserService, SportCenterService, AuthService)
repository/   — persistence (Spring Data JPA)
model/        — domain entities (UserProfile, FacilityProfile) + value objects + enums
exception/    — domain exceptions + GlobalExceptionHandler
config/       — beans (PasswordEncoder, OpenAPI metadata)
```

### Sample Data

On startup, `DatabaseInitializer` seeds the database with demo users and one demo facility (only when the database is empty), so that GET endpoints can be demonstrated without a prior POST:
- `demo@sportlink.test` / `demoPassword1` (USER, INTERMEDIATE skill, Tartu)
- `organizer@sportlink.test` / `organizerPass1` (ORGANIZER, ADVANCED skill, Tallinn)
- `moderator@sportlink.test` / `moderatorPass1` (MODERATOR — used to exercise MODERATOR-only endpoints in the frontend and Swagger UI)
- `info@tartusportshall.test` / `facilityPass1` (Tartu Sports Hall, badminton/basketball/volleyball)

### Run with Docker (recommended)

```bash
docker compose up -d --build
```

This starts:
- PostgreSQL on `localhost:5434` (database `account_service_db`)
- Account Management Service on `localhost:8081`

Logs:
```bash
docker compose logs -f account-service
```

Stop:
```bash
docker compose down          # keep data
docker compose down -v       # also remove the volume
```

### Run locally (without Docker)

Prerequisites: Java 17 (or 21), Maven 3.9+, PostgreSQL 15 running on `localhost:5434` with database `account_service_db` (user/password `postgres`).

```bash
cd account-management-service
mvn clean spring-boot:run
```

### Run tests

```bash
cd account-management-service
mvn test
```

`UserControllerTest` uses `@WebMvcTest` and `@MockBean` to isolate the controller from its dependency (`UserService`). It covers a happy-path test (POST `/api/users` → 201 with the expected response body) and an error-case test (GET `/api/users/{id}` when the user is missing → 404 with the structured error response).

## Moderation Service (Zeynep)

Handles content moderation cases and account sanctions. Synchronously updates the Account Management Service through a Resilience4j-protected client, so that sanctions and user account status stay consistent even when the downstream service is briefly unavailable.

Built with Spring Boot 3, Java 17, PostgreSQL 15, Spring Data JPA, Spring WebFlux (`WebClient`), and Resilience4j (circuit breaker + retry).

### Endpoints (7 total, matching Assignment 3)

| Method | Path | Description |
|---|---|---|
| POST | `/api/cases` | Submit a new moderation case |
| GET | `/api/cases` | List moderation cases (paginated, filterable by content type and verdict) |
| GET | `/api/cases/{id}` | Get a moderation case by ID |
| PUT | `/api/cases/{id}/verdict` | Submit a verdict for a pending case |
| POST | `/api/sanctions` | Apply a sanction (calls Account Management synchronously) |
| GET | `/api/sanctions/{userId}` | List sanction history for a user |
| DELETE | `/api/content/{contentType}/{contentId}` | Delegate content removal to the owning service |

Swagger UI: http://localhost:8090/swagger-ui/index.html

### Architecture Layers

```
controller/   — REST API (Spring MVC, validation via @Valid, pagination support)
dto/          — request/response DTOs
service/      — business logic (ModerationCaseService, SanctionService, ContentRemovalService)
repository/   — persistence (Spring Data JPA)
model/        — domain entities (ModerationCase, AccountSanction) + enums
exception/    — domain exceptions + GlobalExceptionHandler
client/       — outbound REST client (AccountManagementClient, Resilience4j-wrapped)
config/       — beans (WebClient, OpenAPI metadata)
```

### Synchronous Integration with Account Management (Basic Integration deliverable)

When a sanction is applied via `POST /api/sanctions`:

1. The sanction record is persisted locally (case-linked, with `accountSyncStatus = SYNCED` by default).
2. The service calls `PUT /api/users/{id}/status` on the Account Management Service through `AccountManagementClient` (a `WebClient` wrapped with Resilience4j).
3. The user status is mapped: `SUSPENSION → SUSPENDED`, `BAN → BANNED`.
4. On success, the sanction stays `SYNCED`.
5. On failure, the call is retried (3 attempts, 500 ms apart). If all retries fail, the circuit breaker fallback persists the sanction with `accountSyncStatus = PENDING_RETRY` and a captured `lastSyncError`, returning HTTP 202 Accepted to the client. The sanction is not lost; it is just queued for later reconciliation.

This is a real REST call between two implemented services (not mocked).

### Resilience4j: Concrete Failure Scenario

The Moderation Service depends on Account Management for one operation (applying sanctions). Resilience4j is configured for that specific call, with a concrete fallback behavior rather than generic protection:

- **Circuit breaker** `accountManagement` — sliding window of 10 calls, opens on 50% failure rate, 10 s in OPEN state, automatic transition to HALF_OPEN.
- **Retry** `accountManagement` — 3 attempts, 500 ms wait, triggered on `AccountSyncException`, `WebClientRequestException`, `IOException`.
- **Time limiter** `accountManagement` — 3 s timeout per attempt.
- **Fallback** `updateUserStatusFallback` — persists the sanction with `accountSyncStatus = PENDING_RETRY` and a descriptive error so the operation degrades gracefully instead of failing the request.

The visible effect: if Account Management is stopped while applying sanctions, the first sanction takes a few seconds (3 retries × timeout) and is recorded as `PENDING_RETRY`. After a few such failures, the circuit opens and subsequent sanctions return immediately in `PENDING_RETRY` without contacting Account Management. When Account Management comes back, the circuit transitions to HALF_OPEN, a few trial calls succeed, and the circuit closes again.

### Sample Data

On startup, `DatabaseInitializer` seeds three moderation cases (only when the database is empty) so that the list endpoint and Swagger UI can be demonstrated immediately:
- a `PENDING` comment report
- a `DISMISSED` activity description report
- a `REMOVE_CONTENT` user profile report

Sanctions are intentionally not seeded — they are created live during the demo to show the Moderation → Account Management integration.

### Run with Docker

The root `docker-compose.yml` orchestrates both services and their PostgreSQL databases:

```bash
docker compose up -d --build
```

This starts:
- Account Management DB on `localhost:5434`, service on `localhost:8081`
- Moderation DB on `localhost:5439`, service on `localhost:8090`

The Moderation Service container reads `ACCOUNT_MANAGEMENT_BASE_URL=http://account-service:8081` so it can reach Account Management over the Docker network.

### Run locally (without Docker)

Prerequisites: Java 17 (or 21), Maven 3.9+, PostgreSQL 15 running on `localhost:5439` with database `moderation_service_db` (user/password `postgres`). The Account Management Service must also be running on `localhost:8081` for the sanction flow to succeed.

```bash
cd moderation-service
./mvnw clean spring-boot:run
```

## Security: JWT Authentication & Role-Based Authorization (Zeynep)

Both Account Management and Moderation are protected by Spring Security with a stateless JWT filter chain.

- **Token issuance:** `POST /api/auth/login` returns an HS256-signed JWT containing `accountId`, `accountType` (`USER` / `FACILITY`), and `role` (`USER` / `ORGANIZER` / `MODERATOR`). Tokens expire after 12 hours by default.
- **Token validation:** Both services share the same JWT secret (`SPORTLINK_JWT_SECRET` env var) and the same issuer claim. A `JwtAuthenticationFilter` extracts the bearer token, validates it, and seeds Spring's `SecurityContext` with a `ROLE_<role>` authority.
- **Authorization:** Moderator-only endpoints (e.g. `PUT /api/users/{id}/status`, `PUT /api/cases/{id}/verdict`, `POST /api/sanctions`) carry `@PreAuthorize("hasRole('MODERATOR')")`. Public endpoints (login, register) are explicitly allow-listed.
- **Token propagation across services:** When the Moderation Service calls Account Management on behalf of a moderator (`POST /api/sanctions` → `PUT /api/users/{id}/status`), it forwards the moderator's bearer token through `AccountManagementClient`, so authorization is enforced end-to-end.
- **Error semantics:** A 401 from anywhere except `/api/auth/login` is treated as an expired session — the frontend clears its auth state and redirects to the login screen. A 401 from `/api/auth/login` itself is shown as an "Invalid email or password" inline error so the user can retry.
- **Password hashing:** BCrypt at registration time; passwords are never stored or logged in plaintext.

## Asynchronous Communication: Kafka Events (Zeynep)

Both services publish and consume domain events through Apache Kafka, replacing the originally planned RabbitMQ design after a team-wide migration to Kafka (Olena owns the shared broker container).

- **Producer (Account Management) → `account-events` topic** with subtype encoded in the message body:
  - `USER_PROFILE_UPDATED` after a successful `PUT /api/users/{id}`
  - `USER_STATUS_UPDATED` after a successful `PUT /api/users/{id}/status`
  - `FACILITY_PROFILE_UPDATED` after a successful `PUT /api/sport-centers/{id}`
- **Producer (Moderation) → `moderation-events` topic**:
  - `USER_RESTRICTED` after `POST /api/sanctions` persists a sanction
- **Consumer (Moderation):** A `@KafkaListener` on `account-events` filters down to `USER_STATUS_UPDATED` and logs it (so moderators can see when an account's status changes for any reason, not only as a result of a sanction).
- **Topology:** Each topic has 3 partitions, replication factor 1 (local development). The Kafka record key is the event subtype, so all events of one type share a partition and preserve per-type ordering.
- **Failure behavior:** Publication is fire-and-forget — `KafkaTemplate.send()` returns immediately and failures are logged but never roll back the HTTP request. This is intentional: the REST caller has already received a successful response by the time the event is published.

Kafka topics are auto-provisioned on application startup via `KafkaAdmin` and `@Bean NewTopic`. To inspect events live during the demo:

```bash
docker exec -it sportlink-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic account-events --from-beginning --property print.key=true
```

## Frontend (Vue 3)

Shared single-page application at `frontend/`, built with Vue 3 and Vue Router (Options API, no external state store). Each team member adds the views that exercise the endpoints of their backend services.

A central `api/client.js` wrapper injects the JWT `Authorization` header automatically, parses Spring's structured error responses (including the `details` array on validation failures), and on 401 clears the local auth state and redirects to the login screen with a toast. A small reactive `authStore` persists the session to `localStorage` so the login survives page reloads.

Zeynep's views (every Account Management and Moderation endpoint is reachable from the UI):

| View | Backend endpoint(s) |
|---|---|
| `views/account/Login.vue` | `POST /api/auth/login` |
| `views/account/Register.vue` | `POST /api/users`, `POST /api/sport-centers` (User / Sport Center tabs) |
| `views/account/UserProfile.vue` | `GET /api/users/{id}`, `PUT /api/users/{id}` |
| `views/account/UpdateUserStatus.vue` | `PUT /api/users/{id}/status` (MODERATOR-only) |
| `views/account/SportCenter.vue` | `GET /api/sport-centers/{id}`, `PUT /api/sport-centers/{id}` |
| `views/moderation/Cases.vue` | `GET /api/cases` with content-type and verdict filters |
| `views/moderation/CreateCase.vue` | `POST /api/cases` |
| `views/moderation/CaseDetail.vue` | `GET /api/cases/{id}`, `PUT /api/cases/{id}/verdict`, `DELETE /api/content/{type}/{id}` (one-click content removal after a REMOVE_CONTENT verdict) |
| `views/moderation/ApplySanction.vue` | `POST /api/sanctions` — surfaces the `accountSyncStatus` (`SYNCED`, `PENDING_RETRY`, `FAILED`) so the Resilience4j behavior is visible end-to-end |
| `views/moderation/SanctionHistory.vue` | `GET /api/sanctions/{userId}` |

Forms that operate on the signed-in account's own resources ("Profile", "Sport Center", "Create Case") expose a **Use mine** shortcut that autofills the UUID field from the JWT.

### Run frontend locally

```bash
cd frontend
npm install
npm run serve
```

The dev server runs at `http://localhost:8080`. Sign in as `moderator@sportlink.test` / `moderatorPass1` to exercise the moderator-only endpoints, or register a new user from the Register page.

## CI / CD

GitLab CI runs on every push:
- `account-service:build` — Maven build + unit tests (only when `account-management-service/**` changes)
- `moderation-service:build` — Maven compile (only when `moderation-service/**` changes; unit tests deferred per Checkpoint 2 guidelines)
- `sast` — static security analysis
- `secret_detection` — credential leak detection

## AI Use Declaration

During the development of this project, AI-assisted tools were used in the following ways:

- **Problem Solving:** For debugging complex issues and exploring solutions.

- **Documentation drafting and language polishing:** Drafting and refining the wording of this README, commit messages, and inline explanations; checking grammar, sentence structure, and terminology consistency in English-language documentation.

All AI-assisted output was reviewed, modified, and validated before being committed. Architectural decisions, API contracts, and the implementation responsibilities defined in Assignment 3 were authored by the team.
