# Kitchen App

A full-stack kitchen inventory manager. Track food items, get expiration alerts, and reduce waste.

**Live demo:** [https://kitchen-theta-orpin.vercel.app/](https://kitchen-theta-orpin.vercel.app/)
**API docs:** [https://kitchen-production-4ea5.up.railway.app/swagger-ui/index.html](https://kitchen-production-4ea5.up.railway.app/swagger-ui/index.html)

---

## Tech Stack

**Backend**
- Java 17 · Spring Boot 4 · Spring Security
- PostgreSQL · Flyway migrations · Spring Data JPA
- JWT authentication with HttpOnly refresh token cookies
- Bucket4j rate limiting · SpringDoc OpenAPI

**Frontend**
- React 18 · TypeScript · Vite

**Infrastructure**
- Docker · Docker Compose (local)
- Railway (backend) · Vercel (frontend)

---

## Features

- JWT auth with silent token refresh via HttpOnly cookie
- Add, edit, soft-delete, and consume food items
- Filter by food type or storage location · group by either
- Expiring-soon alerts
- Per-user custom food types and storage locations
- Item defaults — autocomplete pre-fills type, unit, location, and expiry days from previous entries
- Paginated item list
- Rate limiting on auth endpoints

---

## Project Structure

```
kitchen/
├── backend/                  # Spring Boot API
│   ├── src/main/java/com/example/kitchen/
│   │   ├── configuration/    # Security, CORS, rate limiting, OpenAPI
│   │   ├── controller/       # REST controllers
│   │   ├── service/          # Business logic
│   │   ├── repository/       # Spring Data JPA repositories
│   │   ├── data/             # JPA entities
│   │   ├── dto/              # Request / response records
│   │   ├── event/            # Application events (user signup seeding)
│   │   └── exception/        # Custom exceptions + global handler
│   └── src/main/resources/
│       └── db/migration/     # Flyway SQL migrations
└── frontend/                 # React + TypeScript SPA
    └── src/
        ├── api/              # Fetch wrappers with auto token refresh
        ├── components/       # UI components
        └── types/            # Shared TypeScript types
```

---

## Running Locally

**Prerequisites:** Docker Desktop, Java 17, Node 18+

### Backend + Database (Docker Compose)

```bash
# Start Postgres and the Spring Boot app
docker compose up --build

# API is available at http://localhost:8080
# Swagger UI at  http://localhost:8080/swagger-ui.html
```

### Backend only (for active development)

```bash
# Start only the database
docker compose up db

# Run the app with the dev profile (verbose SQL logging, Lax cookies)
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Frontend

```bash
cd frontend
npm install
npm run dev

# App is available at http://localhost:5173
```

---

## Environment Variables

### Backend

| Variable | Description | Example |
|---|---|---|
| `DB_URL` | JDBC connection URL | `jdbc:postgresql://localhost:5432/kitchen` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `password` |
| `JWT_SECRET` | Base64 HS256 signing secret (min 32 bytes) | `ZINEk7BUcq6EJnLe...` |
| `CORS_ORIGIN` | Allowed frontend origin | `https://your-app.vercel.app` |
| `SPRING_PROFILES_ACTIVE` | Spring profile (`dev` or `prod`) | `prod` |

> **Generating a JWT secret:**
> ```bash
> openssl rand -base64 32
> ```

The `dev` profile enables SQL logging, debug log levels, and `SameSite=Lax` cookies (for localhost).
The `prod` profile uses `SameSite=None; Secure` cookies (required for cross-origin deployments).

### Frontend

| Variable | Description |
|---|---|
| `VITE_API_URL` | Backend base URL |

---

## Running Tests

```bash
cd backend

# All tests
./mvnw test

# Unit tests only
./mvnw test -Dtest="*ServiceTest,*FilterTest"

# Integration tests only
./mvnw test -Dtest="*IntegrationTest"
```

Integration tests spin up a real PostgreSQL instance via Docker (Testcontainers) and run against actual migrations.

---

## API Overview

Full interactive docs available at `/swagger-ui.html` on any running instance.

| Group | Endpoints |
|---|---|
| **Authentication** | `POST /v1/auth/signup` · `POST /v1/auth/login` · `POST /v1/auth/refresh` · `POST /v1/auth/logout` |
| **Food Items** | `GET/POST /v1/items` · `GET/PUT/DELETE /v1/items/{id}` · `PATCH /v1/items/{id}/consume` · `GET /v1/items/expiring` |
| **User Preferences** | `GET/POST/DELETE /v1/user/types` · `GET/POST/DELETE /v1/user/locations` · `GET /v1/user/item-defaults` |

All endpoints except `/v1/auth/signup` and `/v1/auth/login` require a Bearer token:
```
Authorization: Bearer <access_token>
```

---

## Authentication Flow

```
Login ──► POST /auth/login
              │
              ├─ Response body:  { token: "eyJ..." }   ← stored in memory
              └─ Set-Cookie:     refreshToken=...       ← HttpOnly, not JS-accessible

Every API call ──► Authorization: Bearer <token>
                       │
                   401 Unauthorized?
                       │
                       └─► POST /auth/refresh  (cookie sent automatically)
                               │
                               └─ New token ──► retry original request
```
