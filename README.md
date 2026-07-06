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
