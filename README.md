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

## Repository Layout

```
sportlink-project/
├── account-management-service/     # Zeynep — registration, login, profile management
├── activity-management-service/    # Kirill
├── booking-service/                # Olena
├── rating-management-service/      # Kateryna
├── docker-compose.yml              # Local dev orchestration (PostgreSQL + services)
├── .gitlab-ci.yml                  # CI: per-service build jobs + SAST + secret detection
└── README.md
```

Each service is autonomous, has its own database, and is deployed independently. Additional services are added by their owners as work progresses.

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

On startup, `DatabaseInitializer` seeds the database with two demo users and one demo facility (only when the database is empty), so that GET endpoints can be demonstrated without a prior POST:
- `demo@sportlink.test` / `demoPassword1` (USER, INTERMEDIATE skill, Tartu)
- `organizer@sportlink.test` / `organizerPass1` (ORGANIZER, ADVANCED skill, Tallinn)
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

## CI / CD

GitLab CI runs on every push:
- `account-service:build` — Maven build + unit tests (only when `account-management-service/**` changes)
- `sast` — static security analysis
- `secret_detection` — credential leak detection

## AI Use Declaration

During the development of this project, AI-assisted tools were used in the following ways:

- **Problem Solving:** For debugging complex issues and exploring solutions.

- **Documentation drafting and language polishing:** Drafting and refining the wording of this README, commit messages, and inline explanations; checking grammar, sentence structure, and terminology consistency in English-language documentation.

All AI-assisted output was reviewed, modified, and validated before being committed. Architectural decisions, API contracts, and the implementation responsibilities defined in Assignment 3 were authored by the team.
