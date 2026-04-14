# LedgerFlow

[![CI](https://github.com/pafsmith/ledger-flow/actions/workflows/ci.yml/badge.svg)](https://github.com/pafsmith/ledger-flow/actions/workflows/ci.yml)
![Java 21](https://img.shields.io/badge/Java-21-blue?logo=openjdk&logoColor=white)
![Spring Boot 4](https://img.shields.io/badge/Spring%20Boot-4-green?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql&logoColor=white)

A personal finance REST API where every endpoint is authenticated, every query is user-scoped, and every business rule is enforced server-side. Built with **Java 21** and **Spring Boot 4**, backed by **PostgreSQL** with **Flyway migrations**, and secured with **stateless JWT auth**.

This is a capstone project designed to demonstrate how I think about backend engineering: defensive security, domain-driven structure, and deliberate trade-offs documented out loud.

## Engineering Highlights

These are the decisions that separate this project from a tutorial follow-along:

- **No IDOR vulnerabilities** — every data-access path resolves the user from the JWT token via `@AuthenticationPrincipal`. There is no endpoint that accepts a `userId` parameter for read operations; the paginated `GET /api/transactions` uses JPA Specifications to compose filters server-side while keeping the query scoped to the authenticated user.
- **JPA Specifications for dynamic filtering** — transactions support composable, optional filters (type, category, account, date range, amount range) built with the Criteria API rather than N+1 repository methods. See `TransactionSpecification.java`.
- **Domain exceptions, not HTTP codes in services** — `ResourceNotFoundException`, `BadRequestException`, and `ForbiddenException` are thrown from service methods and mapped to proper HTTP statuses by `GlobalExceptionHandler`. Controllers never set status codes directly.
- **BigDecimal for money, UUID for IDs, LocalDate for business dates** — no `double` precision loss, no auto-increment ID exposure, no `java.util.Date` ambiguity.
- **Feature-based package structure** — `auth/`, `transaction/`, `category/`, `budgets/`, `summary/` each contain their own controller, service, repository, DTOs, and entities. No cross-cutting `controllers/` package.
- **Business rules enforced at the service layer** — expenses require an expense category, transfers require a destination account (different from source), and duplicate budget entries are rejected with a descriptive error. The controller doesn't validate; it delegates.

## Security Model

```
Register ──► POST /api/auth/register  (public)
Login   ──► POST /api/auth/login     (public) ──► JWT signed with UUID subject
Other   ──► Authorization: Bearer <token>      ──► JwtAuthenticationFilter
                                                    │
                                                    ▼
                                          CustomUserDetailsService
                                          loads User by UUID from DB
                                                    │
                                                    ▼
                                          @AuthenticationPrincipal UserDetails
                                          used in every protected controller
```

- JWT tokens contain the user's UUID as the subject claim — not an email or opaque identifier.
- Ownership checks happen in the service layer before any mutation: if a transaction's `user.id` doesn't match the authenticated user, a `ForbiddenException` is thrown returning `403`.
- `/api/health` and Swagger endpoints are public; all other routes require a valid Bearer token.
- No HTTP Basic fallback — pure stateless JWT authentication.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 4 (Web MVC, Data JPA, Security, Validation) |
| Database | PostgreSQL 16 |
| Migrations | Flyway (versioned SQL) |
| Auth | JWT (jjwt) + BCrypt |
| API Docs | Springdoc OpenAPI / Swagger UI |
| Testing | JUnit 5, Mockito, MockMvc, AssertJ |
| CI | GitHub Actions |
| Infrastructure | Docker, Docker Compose |
| Build | Maven Wrapper |

## API Overview

All endpoints require `Authorization: Bearer <token>` except where noted.

### Auth (public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Create account, receive JWT |
| POST | `/api/auth/login` | Authenticate, receive JWT |
| GET | `/api/auth/me` | Current user profile |

### Transactions
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/transactions` | Create (income, expense, or transfer) |
| GET | `/api/transactions` | List with dynamic filters and pagination |
| GET | `/api/transactions/{id}` | Get by ID (ownership enforced) |
| PUT | `/api/transactions/{id}` | Update (ownership enforced) |
| DELETE | `/api/transactions/{id}` | Delete (ownership enforced) |

### Categories
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/categories` | Create category (INCOME or EXPENSE) |
| GET | `/api/categories` | List user's categories |
| GET | `/api/categories/{id}` | Get by ID (ownership enforced) |
| GET | `/api/categories/type/{type}` | Filter by category type |
| DELETE | `/api/categories/{id}` | Delete (ownership enforced) |

### Budgets
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/budgets` | Create monthly budget per category |
| GET | `/api/budgets` | List budgets (filter by year/month) |
| GET | `/api/budgets/{id}` | Get by ID (ownership enforced) |
| PUT | `/api/budgets/{id}` | Update (ownership enforced) |
| DELETE | `/api/budgets/{id}` | Delete (ownership enforced) |

### Summary
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/summary/monthly?year=2026&month=3` | Income, expenses, net, category breakdown, budget vs actual |

### Health (public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/health` | Service status, version, timestamp |

> Full interactive documentation available at `/swagger-ui.html` when the app is running.

## Example Requests

Register and start querying in under 30 seconds:

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Paul","lastName":"Smith","email":"paul@example.com","password":"password123"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"paul@example.com","password":"password123"}'

# Create a transaction (uses token from login response)
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"accountId":"<uuid>","categoryId":"<uuid>","description":"Tesco shop","amount":45.50,"type":"EXPENSE","transactionDate":"2026-03-10"}'

# Get filtered, paginated transactions
curl "http://localhost:8080/api/transactions?type=EXPENSE&from=2026-03-01&to=2026-03-31&page=0&size=10&sortBy=transactionDate&direction=desc" \
  -H "Authorization: Bearer <token>"
```

## Architecture

```
Controller ──► Service ──► Repository ──► PostgreSQL
   (DTOs)       (rules)     (JPA)           + Flyway
```

Each domain (`auth`, `transaction`, `category`, `budgets`, `summary`) is a self-contained package with its own controller, service, repository, DTOs, and entities. Controllers are thin — they delegate to services and return DTOs. Services enforce business rules and ownership checks. Repositories handle persistence via Spring Data JPA.

```
src/main/java/dev/pafsmith/ledgerflow/
├── common/             # BaseEntity, domain exceptions, GlobalExceptionHandler
├── config/             # SecurityConfig, OpenApiConfig
├── user/               # User entity and repository
├── auth/               # Registration, login, JWT service, auth filter
├── account/            # Account entity, types, repository
├── category/           # Category CRUD (controller, service, repository, DTOs)
├── transaction/        # Transaction CRUD + JPA Specifications for filtering
├── budgets/            # Budget CRUD with year/month constraints
├── summary/            # Monthly analytics (income/expenses/budget comparison)
└── health/             # Health check endpoint
```

## Testing Strategy

**85 tests** across 14 test classes with consistent naming (`method_shouldOutcome`).

```bash
./mvnw test          # unit + slice tests
./mvnw verify         # full CI pipeline (compile, test, package)
```

| Layer | What's tested | How |
|-------|--------------|-----|
| Service | Business logic, validation rules, ownership enforcement, error paths | JUnit 5 + Mockito, `ArgumentCaptor` for persist verification, `assertThatThrownBy` for exceptions |
| Controller | HTTP status codes, JSON response shape, `@Valid` constraints, error payloads | `@WebMvcTest` + MockMvc, `@WithMockUser` for authenticated context |
| API | Full Spring context starts, all beans wired, Flyway migrations run against real Postgres | `@SpringBootTest` (via CI) |

Key testing patterns:
- `verify(..., never())` to confirm error paths short-circuit before database mutations
- `isEqualByComparingTo` for `BigDecimal` precision-safe assertions
- `BaseControllerTest` shared setup with `@AutoConfigureMockMvc(addFilters = false)` for isolated web-layer tests
- Separate test classes for distinct behaviours (e.g. `AuthServiceTest` vs `AuthServiceLoginTest`)

## Known Trade-offs and Future Work

| Trade-off | Why | Plan |
|-----------|-----|------|
| Monthly summary loads all month transactions into memory | Simple implementation, avoids query complexity with JPA | Replace with a `@Query` using `SUM`/`GROUP BY` for production scale |
| Register endpoint doesn't return a JWT | Keeps registration orthogonal to authentication | Return token in registration response to eliminate the required follow-up login |
| No token refresh or blacklist | Standard for a portfolio project; JWTs expire at `JWT_EXPIRATION` | Add refresh tokens and a token blacklist for production |
| No CORS configuration | API-only project; no frontend yet | Add CORS config when a frontend is introduced |
| No password strength validation | `@Valid` constraints on DTO but no complexity rules | Add a password strength validator |
| Account CRUD is read-only through the API | Accounts are seeded or managed via migrations | Add full Account CRUD endpoints |

## Running Locally

### Prerequisites
- Docker and Docker Compose
- Java 21 (for running without Docker)

### Quick Start

```bash
# 1. Clone and configure
git clone https://github.com/pafsmith/ledger-flow.git
cd ledger-flow
cp .env.example .env

# 2. Start PostgreSQL + the app
docker compose up --build

# 3. API available at:
#    http://localhost:8080
#    http://localhost:8080/swagger-ui.html
```

### Run on Host (without Docker for the app)

```bash
# Start only PostgreSQL
docker compose up -d postgres

# Run Spring Boot directly (override DB_URL for host networking)
DB_URL=jdbc:postgresql://localhost:5433/ledgerflow ./mvnw spring-boot:run
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | JDBC connection string | `jdbc:postgresql://postgres:5432/ledgerflow` |
| `DB_USERNAME` | Database user | `ledgerflow_user` |
| `DB_PASSWORD` | Database password | `ledgerflow_password` |
| `JWT_SECRET` | Signing key for JWT tokens | — |
| `JWT_EXPIRATION` | Token TTL in milliseconds | `36000` |

## Author

**Paul Smith** — [LinkedIn](https://www.linkedin.com/in/pafsmith) — [GitHub](https://github.com/pafsmith)