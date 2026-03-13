# LedgerFlow

LedgerFlow is a **personal finance backend API** built with **Spring Boot**.  
It allows users to track accounts, manage transactions, organise spending into categories, and build budgets.

This project is designed to demonstrate:

- REST API design
- layered backend architecture
- database migrations
- validation and error handling
- automated testing

---

# Tech Stack

### Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security

### Database

- PostgreSQL
- Flyway (database migrations)

### API Documentation

- OpenAPI / Swagger (springdoc-openapi)

### Testing

- JUnit 5
- Mockito
- MockMvc

### Build Tool

- Maven

---

# Features Implemented

## Transactions

Create, retrieve, update, and delete financial transactions.

Endpoints:

```
POST   /api/transactions
GET    /api/transactions/{transactionId}
GET    /api/transactions/user/{userId}
GET    /api/transactions/account/{accountId}
GET    /api/transactions/category/{categoryId}
PUT    /api/transactions/{transactionId}
DELETE /api/transactions/{transactionId}
```

Supported transaction types:

- EXPENSE
- INCOME
- TRANSFER

Validation rules enforce:

- positive transaction amounts
- correct category types
- correct transfer behaviour

---

## Categories

Categories organise transactions into spending groups.

Endpoints:

```
POST /api/categories
GET  /api/categories/{categoryId}
GET  /api/categories/user/{userId}
GET  /api/categories/user/{userId}/type/{type}
```

Category types:

- EXPENSE
- INCOME

---

## Validation

Requests are validated using **Jakarta Bean Validation**.

Examples:

- `@NotNull`
- `@NotBlank`
- `@DecimalMin`
- `@Size`

Validation errors return structured API responses.

---

## Error Handling

A **global exception handler** provides consistent responses.

Example error:

```
{
  "timestamp": "2026-03-10T12:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Transaction not found",
  "path": "/api/transactions/123"
}
```

---

# API Documentation

Swagger UI is available at:

<http://localhost:8080/swagger-ui.html>

The OpenAPI specification is available at:

`/v3/api-docs`

---

# Database

The application uses **PostgreSQL**.

Schema management is handled using **Flyway migrations**.

Migrations are located in:

`src/main/resources/db/migration`

Seed data is included to simplify local testing.

---

# Running the Application

### 1. Start PostgreSQL

Example using Docker:

```
docker run -d \
  --name ledgerflow-postgres \
  -p 5432:5432 \
  -e POSTGRES_DB=ledgerflow \
  -e POSTGRES_USER=ledgerflow_user \
  -e POSTGRES_PASSWORD=ledgerflow_password \
  postgres:15
```

### 2. Create .env file from .env.example

`cp .env.example .env`

### 3. Run the application

`./mvnw spring-boot:run`

The API will start at:

`http://localhost:8080`

---

# Running Tests

Run all tests:

`./mvnw test`

The project contains:

- service layer unit tests
- controller layer tests using MockMvc

---

# Project Structure

controller  
service  
repository  
entity  
dto  
exception  
config

This follows a standard **layered architecture**:

Controller → Service → Repository → Database

---

# Example Request

Create a transaction:

```
{
  "userId": "11111111-1111-1111-1111-111111111111",
  "accountId": "22222222-2222-2222-2222-222222222222",
  "categoryId": "33333333-3333-3333-3333-333333333333",
  "description": "Tesco shop",
  "amount": 45.50,
  "type": "EXPENSE",
  "transactionDate": "2026-03-10",
  "merchant": "Tesco"
}
```

---

# Future Improvements

Planned next steps:

- budget management endpoints
- dashboard summary queries
- account management endpoints
- authentication and user login
- pagination and filtering
- Docker based development environment

---

# Author

Paul Smith

LinkedIn: [https://www.linkedin.com/in/pafsmith]()

---
