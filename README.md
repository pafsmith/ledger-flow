## LedgerFlow

LedgerFlow is a **personal finance backend API** built with Spring Boot.

It allows users to:

- manage transactions
- organise spending into categories
- (soon) track budgets

The project is designed to simulate a **real-world backend system**, focusing on:

- secure API design
- user-scoped data access
- clean architecture
- testability

---

## Tech Stack

### Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security

### Database

- PostgreSQL
- Flyway (migrations)

### Auth

- JWT (JSON Web Tokens)
- BCrypt password hashing

### Testing

- JUnit 5
- Mockito
- MockMvc

### API Docs

- OpenAPI / Swagger

---

## Features

### Authentication

```
POST /api/auth/register
POST /api/auth/login
GET /api/auth/me

```

- users register with email + password
- passwords are securely hashed using BCrypt
- login returns a JWT token

- `/api/auth/me` returns the authenticated user

### Transactions

```
POST /api/transactions
GET /api/transactions
GET /api/transactions/{transactionId}
PUT /api/transactions/{transactionId}
DELETE /api/transactions/{transactionId}
```

Transactions are:

- scoped to the authenticated user
- protected by JWT
- validated using Bean Validation

---

## Filtering & Pagination

Transactions support:

- type (EXPENSE / INCOME / TRANSFER)
- category
- account
- date range
- amount range
- pagination
- sorting

### Example

```
GET /api/transactions?type=EXPENSE&from=2026-03-01&to=2026-03-31&page=0&size=10&sortBy=transactionDate&direction=desc
Authorization: Bearer <token>
```

---

## Authentication Flow

### 1. Register

```
POST /api/auth/register
{
 "firstName": "Paul",
 "lastName": "Smith",
 "email": "<paul@test.com>",
 "password": "password123"
}
```

### 2. Login

```
POST /api/auth/login
{
 "email": "<paul@test.com>",
 "password": "password123"
}
```

Response:

```
{
 "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 3. Use token

```
Authorization: Bearer <token>
```

---

## Example Transaction Request

```
POST /api/transactions
{
 "accountId": "22222222-2222-2222-2222-222222222222",
 "categoryId": "33333333-3333-3333-3333-333333333333",
 "description": "Tesco shop",
 "amount": 45.50,
 "type": "EXPENSE",
 "transactionDate": "2026-03-10"
}
```

---

## Security

- all endpoints (except auth + Swagger) require JWT
- user identity is derived from the token (not request body)
- users cannot access other users’ data
- stateless authentication (no sessions)

---

## Running Locally

### 1. Create your environment file

```
cp .env.example .env
```

### 2. Start app + postgres with Docker Compose

```
docker compose up --build
```

The API is available at:

```
http://localhost:8080
```

Swagger UI:

``` 
http://localhost:8080/swagger-ui.html
```

### 3. (Optional) Run app on host instead of Docker

If you run Spring Boot directly, override `DB_URL` for localhost:

```
DB_URL=jdbc:postgresql://localhost:5433/ledgerflow ./mvnw spring-boot:run
```

---

## Testing

```
./mvnw test
```

Includes:

- service tests
- controller tests
- security edge-case tests

---

## API Docs

Swagger UI:

<http://localhost:8080/swagger-ui.html>

---

## Architecture

Controller → Service → Repository → Database

- DTOs for API boundaries
- services contain business logic
- repositories handle persistence

---

## Future Improvements

- budget management
- dashboard reporting (spending insights)
- account management
- recurring transactions
- full role-based access control

# Author

Paul Smith

LinkedIn: [https://www.linkedin.com/in/pafsmith]()

---
