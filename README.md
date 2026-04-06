# Zorvyn — Finance Dashboard API

A REST API backend for tracking personal or business finances. Built with Spring Boot 3 and MySQL, it handles user authentication, role-based access control, income/expense records, and an aggregated dashboard with monthly trends and category breakdowns.

This was built as an assignment project. The focus was on getting the architecture right — clean layering, proper JWT auth, and making sure role permissions are enforced at both the route and method level.

---

## What it does

- **Authentication** — register and login with email/password, get back a JWT token
- **Financial records** — create, read, update, and soft-delete income/expense entries
- **Filtering** — query records by type, category, or date range (all paginated)
- **Dashboard** — aggregated summary with totals, category breakdowns, and monthly trend data
- **User management** — admin can list, update, activate/deactivate accounts and change roles

---

## Tech stack

| | |
|---|---|
| Language | Java 23 |
| Framework | Spring Boot 3.3 |
| Security | Spring Security + JWT (JJWT 0.11.5) |
| Database | MySQL 8 |
| ORM | Spring Data JPA / Hibernate |
| Validation | Jakarta Bean Validation |
| Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven (wrapper included) |
| Utilities | Lombok |

---

## Getting started

### Prerequisites

- Java 23+
- MySQL 8 running locally
- Maven 3.9+ (or just use `./mvnw`)

### Database setup

```sql
CREATE DATABASE my_database;
```

The app connects to `localhost:3307` by default (note: port 3307, not the standard 3306). If your MySQL is on 3306, update `application.properties`.

### Configuration

Open `src/main/resources/application.properties` and update these to match your setup:

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/my_database?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false
spring.datasource.username=root
spring.datasource.password=your_password

jwt.secret=change-this-to-a-long-random-string-in-production
jwt.expiration=86400000
```

The server runs on **port 8081** with a base path of `/api/v1.0`.

### Run it

```bash
./mvnw spring-boot:run
```

Or build a JAR first:

```bash
./mvnw package -DskipTests
java -jar target/zorvyn-0.0.1-SNAPSHOT.jar
```

Once it's up, Swagger UI is at:
```
http://localhost:8081/api/v1.0/swagger-ui/index.html
```

---

## Default accounts

Three users are seeded automatically on first startup:

| Email | Password | Role |
|---|---|---|
| admin@finance.com | Admin@123 | ADMIN |
| analyst@finance.com | Analyst@123 | ANALYST |
| viewer@finance.com | Viewer@123 | VIEWER |

> Don't use these credentials in any real deployment — they're only there for development convenience.

---

## Roles and permissions

```
VIEWER   → GET /api/v1.0/dashboard/summary only

ANALYST  → Everything VIEWER can do
           + Read and filter financial records

ADMIN    → Everything ANALYST can do
           + Create, update, delete financial records
           + Full user management (list, update, activate/deactivate)
```

Access is enforced at two levels: route-level in `SecurityConfig` and method-level with `@PreAuthorize` annotations on each controller method.

One thing worth noting: the system prevents you from deactivating the last active admin account. If you try, you'll get a 400 with a message telling you to promote someone else first.

---

## API endpoints

All endpoints below are prefixed with `/api/v1.0`.

### Auth (no token required)

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register` | Create a new account, returns JWT |
| POST | `/api/auth/login` | Login, returns JWT |

### Dashboard

| Method | Path | Roles | Description |
|---|---|---|---|
| GET | `/api/dashboard/summary?months=6` | All | Totals, trends, category breakdown |

### Financial records

| Method | Path | Roles | Description |
|---|---|---|---|
| POST | `/api/records` | ADMIN | Create a record |
| GET | `/api/records` | ADMIN, ANALYST | List all (paginated) |
| GET | `/api/records/{id}` | ADMIN, ANALYST | Get one by ID |
| PUT | `/api/records/{id}` | ADMIN | Full update |
| DELETE | `/api/records/{id}` | ADMIN | Soft delete |
| GET | `/api/records/filter/type` | ADMIN, ANALYST | Filter by INCOME or EXPENSE |
| GET | `/api/records/filter/category` | ADMIN, ANALYST | Filter by category (case-insensitive) |
| GET | `/api/records/filter/date-range` | ADMIN, ANALYST | Filter by date range (YYYY-MM-DD) |

### Users

| Method | Path | Roles | Description |
|---|---|---|---|
| GET | `/api/users/me` | All | Get own profile |
| GET | `/api/users` | ADMIN | List all users (paginated) |
| GET | `/api/users/{id}` | ADMIN | Get user by ID |
| PATCH | `/api/users/{id}` | ADMIN | Update name/role/status |
| POST | `/api/users/{id}/activate` | ADMIN | Activate account |
| POST | `/api/users/{id}/deactivate` | ADMIN | Deactivate account |
| GET | `/api/users/by-role?role=ANALYST` | ADMIN | Filter users by role |

---

## How authentication works

1. Call `/api/auth/login` with your email and password
2. Copy the `token` from the response
3. Add it to every subsequent request as a header: `Authorization: Bearer <token>`

Tokens expire after 24 hours (configurable via `jwt.expiration` in milliseconds).

---

## Request / response examples

### Login

```http
POST /api/v1.0/api/auth/login
Content-Type: application/json

{
  "email": "admin@finance.com",
  "password": "Admin@123"
}
```

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "userId": 1,
    "name": "System Administrator",
    "email": "admin@finance.com",
    "role": "ADMIN"
  }
}
```

### Create a financial record

```http
POST /api/v1.0/api/records
Authorization: Bearer <token>
Content-Type: application/json

{
  "amount": 3000.00,
  "type": "INCOME",
  "category": "Salary",
  "transactionDate": "2024-04-01",
  "notes": "April salary"
}
```

### Dashboard summary

```http
GET /api/v1.0/api/dashboard/summary?months=6
Authorization: Bearer <token>
```

```json
{
  "success": true,
  "data": {
    "totalIncome": 18000.00,
    "totalExpense": 9200.00,
    "totalBalance": 8800.00,
    "totalIncomeRecords": 6,
    "totalExpenseRecords": 14,
    "incomeByCategory": {
      "Salary": 15000.00,
      "Freelance": 3000.00
    },
    "expenseByCategory": {
      "Rent": 4800.00,
      "Groceries": 1800.00,
      "Utilities": 1100.00
    },
    "incomeMonthlyTrend": [
      { "month": "2023-11", "total": 2500.00 },
      { "month": "2023-12", "total": 3000.00 }
    ],
    "expenseMonthlyTrend": [...],
    "recentActivity": [...]
  }
}
```

### Error format

All errors follow the same shape:

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "amount": "must be greater than 0",
    "category": "must not be blank"
  }
}
```

---

## Database schema

Hibernate auto-generates the schema on startup (`ddl-auto=update`), so you don't need to run any SQL migrations manually.

```
users
  id, name, email (unique), password (BCrypt), role, active, created_at, updated_at

financial_records
  id, amount (DECIMAL 15,2), type (INCOME|EXPENSE), category, transaction_date,
  notes, deleted (soft delete flag), created_by (FK → users.id), created_at, updated_at
```

Indexes are set on `type`, `category`, `transaction_date`, and `created_by` in `financial_records` to keep filter queries fast as data grows.

---

## Project structure

```
src/main/java/com/finance/zorvyn/
├── ZorvynApplication.java
├── config/
│   ├── ApplicationConfig.java       # UserDetailsService bean
│   ├── Datalntializer.java          # Seeds default users on startup
│   ├── OpenApiConfig.java           # Swagger / bearer auth setup
│   └── SecurityConfig.java          # Filter chain, CORS, public routes
├── controller/
│   ├── AuthController.java
│   ├── DashboardController.java
│   ├── FinancialRecordController.java
│   └── UserController.java
├── dto/
│   ├── request/                     # Inbound payloads (validated)
│   └── response/                    # Outbound payloads (no passwords)
├── entity/
│   ├── FinancialRecord.java
│   ├── Role.java
│   ├── TransactionType.java
│   └── User.java
├── exception/
│   └── GlobalExceptionHandler.java  # Centralised error handling
├── repository/
│   ├── FinancialRecordRepository.java
│   └── UserRepository.java
├── security/
│   ├── JwtAuthenticationFilter.java
│   └── JwtService.java
└── service/
    ├── impl/                        # Business logic implementations
    └── *.java                       # Service interfaces
```

---

## A few implementation notes

**Soft deletes** — records are never physically removed. A `deleted` flag is set to `true` and all queries filter it out. This keeps the audit trail intact.

**BigDecimal for money** — using `double` for financial amounts causes floating-point precision bugs. All amounts are stored and calculated as `BigDecimal`.

**Stateless sessions** — no server-side session storage. The JWT carries the user's identity; the server validates it and loads the user from the database on every request. This means the app can scale horizontally without any shared session state.

**DTO separation** — request and response DTOs are kept separate from JPA entities. This avoids accidentally exposing the password hash or allowing clients to set internal fields like `createdBy` or `deleted`.

**Last-admin guard** — if you try to deactivate or demote the only active admin, the API returns a 400 instead of letting you lock yourself out.

---

## Running tests

```bash
./mvnw test
```

Make sure MySQL is running and the database is accessible before running tests, since they use the actual datasource.
