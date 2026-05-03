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
├── docker-compose.yml              # Local dev orchestration (PostgreSQL + services)
├── .gitlab-ci.yml                  # CI: build + tests + SAST + secret detection
└── README.md
```

Other services will be added by their owners as the project progresses.

## Account Management Service

Responsible for user and sport center registration, authentication, profile reads/updates, and account status changes. Built with Spring Boot 3, Java 17, PostgreSQL, JPA.

### Endpoints

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
docker compose down -v       # also remove volume
```

### Run locally (without Docker)

Prerequisites: Java 17, Maven 3.9+, PostgreSQL 15 running on `localhost:5434` with database `account_service_db` (user/password `postgres`).

```bash
cd account-management-service
mvn clean spring-boot:run
```

### Run tests

```bash
cd account-management-service
mvn test
```

## CI / CD

GitLab CI runs on every push:
- `account-service:build` — Maven build + unit tests (only when `account-management-service/**` changes)
- `sast` — static security analysis
- `secret_detection` — credential leak detection
