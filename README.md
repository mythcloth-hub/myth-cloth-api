# Myth Cloth API

Backend API for the Myth Cloth Collection Manager app.  
It manages Saint Seiya figurines, catalogs, distributors, collector collections, purchases, and security/permissions.

## Project overview

- **Framework**: Spring Boot (Java 21)
- **Database**: PostgreSQL
- **Build tool**: Gradle Wrapper (`./gradlew`)
- **API base URL**: `http://localhost:9090/api/v1`
- **OpenAPI/Swagger**:
  - `http://localhost:9090/api/v1/swagger-ui.html`
  - `http://localhost:9090/api/v1/swagger.yaml`

---

## Local setup (new machine) - step by step

### 1. Install prerequisites

Install these first:

1. **Git**
2. **Java 21** (JDK, not JRE)
3. **Docker Desktop** (or Docker Engine + Compose plugin)

Check versions:

```sh
git --version
java -version
docker --version
docker compose version
```

### 2. Clone the repository

```sh
git clone https://github.com/mythcloth-hub/myth-cloth-api.git
cd myth-cloth-api
```

### 3. Start PostgreSQL with Docker Compose

This project already includes `docker-compose.yml` with:
- DB host: `localhost`
- Port: `5432`
- DB name: `mythclothlocal`
- User: `postgres`
- Password: `postgres`

Start DB:

```sh
docker compose up -d
```

Verify DB container:

```sh
docker compose ps
docker compose logs -f postgres
```

### 4. Run the API

```sh
./gradlew bootRun
```

When startup is complete, API runs at:

`http://localhost:9090/api/v1`

### 5. Verify it is running

Open Swagger UI:

`http://localhost:9090/api/v1/swagger-ui.html`

Or call the OpenAPI file:

```sh
curl http://localhost:9090/api/v1/swagger.yaml
```

### 6. (Optional) Run tests locally

Unit tests:

```sh
./gradlew test
```

Full check pipeline (includes integration tests + PIT mutation tests):

```sh
./gradlew check
```

---

## Important local behavior

- `spring.jpa.hibernate.ddl-auto` is set to **`create-drop`** in `application.yaml`.
- On each app restart, schema is recreated and seeded again from `src/main/resources/data.sql`.
- This is fine for local development but means local DB data is not persistent across app restarts.

---

## Authentication notes for local testing

- Public endpoints include:
  - `GET /figurines/**`
  - `GET /catalogs/{catalogType}/**`
  - `GET /anniversaries/**`
  - `POST /collectors/auth/{provider}`
- Most other endpoints require Bearer JWT with role/permission claims.

If you need authenticated testing, use the social login endpoint:

`POST /api/v1/collectors/auth/{provider}` (e.g. `google`, `facebook`)

Request body shape:

```json
{
  "idToken": "provider-id-token",
  "accessToken": "provider-access-token"
}
```

---

## Useful local commands

Start DB:

```sh
docker compose up -d
```

Stop DB:

```sh
docker compose down
```

Stop DB and delete volume (full local reset):

```sh
docker compose down -v
```

Run app:

```sh
./gradlew bootRun
```

Run a clean build:

```sh
./gradlew clean build
```
