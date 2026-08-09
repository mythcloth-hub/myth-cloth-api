# Myth Cloth API

Backend API for the Myth Cloth Collection Manager app.  
It manages Saint Seiya figurines, catalogs, distributors, collector collections, purchases, and security/permissions.

## Project overview

- **Framework**: Spring Boot (Java 25)
- **Database**: PostgreSQL
- **Build tool**: Gradle Wrapper (`./gradlew`)
- **API base URL**: `http://localhost:9090/api/v1`
- **OpenAPI/Swagger**:
  - `http://localhost:9090/api/v1/swagger-ui.html`
  - `http://localhost:9090/api/v1/swagger.yaml`

Swagger is generated at runtime from the controller mappings and DTO metadata, so it stays in sync with API changes.
The build also exports a YAML snapshot to `build/generated/openapi/swagger.yaml`.

## Environment variables

The API now reads sensitive/runtime config from environment variables.

### Required in production

| Variable              | Purpose                                   |
|-----------------------|-------------------------------------------|
| `FACEBOOK_APP_ID`     | Facebook app id for social login          |
| `FACEBOOK_APP_SECRET` | Facebook app secret                       |
| `GOOGLE_CLIENT_ID`    | Google OAuth client id                    |
| `JWT_SECRET`          | JWT signing secret                        |
| `DB_URL`              | PostgreSQL JDBC URL                       |
| `DB_USERNAME`         | PostgreSQL username                       |
| `DB_PASSWORD`         | PostgreSQL password                       |
| `PORT`                | Server port (Render usually injects this) |

### Local development notes

- The base config (`application.yaml`) expects these variables to be present:
  - `FACEBOOK_APP_ID`
  - `FACEBOOK_APP_SECRET`
  - `GOOGLE_CLIENT_ID`
  - `JWT_SECRET`
  - `DB_URL`
  - `DB_USERNAME`
  - `DB_PASSWORD`
- `application-local.yaml` adds local overrides for:
  - `server.servlet.context-path=/api/v1`
  - `myth-cloth.security.jwt.issuer=myth-cloth-api`
  - `myth-cloth.security.jwt.ttl-minutes=60`

### Profiles used by this project

- `local`: enables local API context path (`/api/v1`) and JWT issuer/TTL defaults.
- `prod`: overrides DB/JPA/logging/CORS values from `application-prod.yaml`.
- No profile: only `application.yaml` is loaded.

---

## Local setup (new machine) - step by step

### 1. Install prerequisites on a fresh Ubuntu-based distro

This project now targets **Java 25**.

Run these commands in order:

```sh
sudo apt update
sudo apt upgrade -y
sudo apt install -y ca-certificates curl gnupg lsb-release git
```

Install Java 25:

```sh
sudo apt install -y openjdk-25-jdk
```
Because Linux Mint is based on Ubuntu, we will configure it to use the correct upstream Ubuntu repositories.
Step 1: Install Prerequisites
```sh
sudo apt update
sudo apt install -y ca-certificates curl gnupg
```
Add Docker's Official GPG Key
Create the directory for repository keys and download Docker's security key so your system trusts the downloads:
```sh
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
```
Set Up the Docker Repository
Because Linux Mint uses its own codenames (like wilma or virginia), standard Ubuntu commands fail. We must explicitly tell the system to use the UBUNTU_CODENAME:
Bash
```sh
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo ${UBUNTU_CODENAME}) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
```
Install Docker & Docker Compose
Now, refresh your package database to include the new Docker repository and install everything at once:
```sh
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```
Enable Non-Root Access
By default, Docker requires sudo for every command. To allow your current user account to run Docker commands without root privileges, add yourself to the docker group:
```sh
sudo usermod -aG docker $USER
```
    ⚠️ Crucial Step: For this group change to take effect, you must log out of Linux Mint and log back in (or restart your computer). Alternatively, you can apply the changes instantly to your current terminal window by running: newgrp docker.

Verify Everything Works

To confirm that both Docker and Docker Compose installed perfectly, run these two verification commands:
```sh
# Test the Docker Engine
docker run hello-world

# Test Docker Compose
docker compose version
```

Note: Docker Compose is now integrated directly into the Docker CLI as a plugin. You invoke it using a space (docker compose) rather than a hyphen (docker-compose).

Check versions:

```sh
git --version
java -version
docker --version
docker compose version
```

Verify the installed JDK points to Java 25:

```sh
java -version
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

Export env vars (example values for local):

```sh
export FACEBOOK_APP_ID="your-facebook-app-id"
export FACEBOOK_APP_SECRET="your-facebook-app-secret"
export GOOGLE_CLIENT_ID="your-google-client-id"
export JWT_SECRET="$(openssl rand -base64 32)"
export DB_URL="jdbc:postgresql://localhost:5432/mythclothlocal"
export DB_USERNAME="postgres"
export DB_PASSWORD="postgres"
```

Start the app with the local profile:

```sh
./gradlew bootRun --args='--spring.profiles.active=local'
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

- With `local` profile, JPA settings come from `application.yaml` (`ddl-auto: update`, `sql.init.mode: never`).
- With `prod` profile, JPA settings are overridden by `application-prod.yaml` (`ddl-auto: create-drop`, `sql.init.mode: always`).

---

## Authentication notes for local testing

- Public endpoints include:
  - `GET /figurines/**`
  - `GET /catalogs/{catalogType}/**`
  - `GET /anniversaries/**`
  - `GET /swagger-ui.html`
  - `GET /swagger.yaml`
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
