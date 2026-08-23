# Myth Cloth API

Backend API for the Myth Cloth Collection Manager app. It manages Saint Seiya figurines, catalogs, distributors, collector collections, purchases, and security/permissions.

## Tech stack

- Java 25
- Spring Boot 4.1.0
- PostgreSQL
- RabbitMQ
- Gradle Wrapper (`./gradlew`)

## Runtime basics

- API base path: `http://localhost:9090/api/v1`
- Swagger UI: `http://localhost:9090/api/v1/swagger-ui.html`
- OpenAPI YAML: `http://localhost:9090/api/v1/swagger.yaml`
- Actuator: `http://localhost:9090/api/v1/actuator`

## Profiles

- `application.yaml`: shared API path, JWT settings, and RabbitMQ listener defaults.
- `local`: local DB, demo mode, local RabbitMQ, and dev logging.
- `prod`: environment-driven database, OAuth, RabbitMQ, and bootstrap settings.

## Environment variables

### Required everywhere

| Variable                            | Purpose                                                         |
|-------------------------------------|-----------------------------------------------------------------|
| `FACEBOOK_BOOTSTRAP_ADMIN_PROVIDER` | Facebook provider user id used to bootstrap the admin account   |
| `GOOGLE_BOOTSTRAP_ADMIN_PROVIDER`   | Google provider user id used to bootstrap the admin account     |
| `DEMO_BOOTSTRAP_ADMIN_PROVIDER`     | Local/demo provider user id used to bootstrap the admin account |
| `FACEBOOK_APP_SECRET`               | Facebook app secret                                             |
| `JWT_SECRET`                        | JWT signing secret                                              |

### Required in production

| Variable                | Purpose                |
|-------------------------|------------------------|
| `FACEBOOK_APP_ID`       | Facebook app id        |
| `GOOGLE_CLIENT_ID`      | Google OAuth client id |
| `DB_URL`                | PostgreSQL JDBC URL    |
| `DB_USERNAME`           | PostgreSQL username    |
| `DB_PASSWORD`           | PostgreSQL password    |
| `RABBITMQ_HOST`         | RabbitMQ host          |
| `RABBITMQ_PORT`         | RabbitMQ port          |
| `RABBITMQ_USERNAME`     | RabbitMQ username      |
| `RABBITMQ_PASSWORD`     | RabbitMQ password      |
| `RABBITMQ_VIRTUAL_HOST` | RabbitMQ virtual host  |

### Local defaults

With the `local` profile, these values already have defaults:

- `FACEBOOK_APP_ID=4393993617525351`
- `GOOGLE_CLIENT_ID=1068577777042-fmeugfjurp1nbbfqq4q544gjk2h2l3cb.apps.googleusercontent.com`
- `DB_URL=jdbc:postgresql://localhost:5432/mythclothlocal`
- `DB_USERNAME=postgres`
- `DB_PASSWORD=postgres`
- RabbitMQ: `localhost:5672`, user/password `mythcloth` / `mythcloth`, virtual host `/`

## Local setup

1. Start the dependencies:

   ```sh
   docker compose up -d
   ```

2. Export the required secrets:

   ```sh
   export FACEBOOK_BOOTSTRAP_ADMIN_PROVIDER="facebook-admin-provider-user-id"
   export GOOGLE_BOOTSTRAP_ADMIN_PROVIDER="google-admin-provider-user-id"
   export DEMO_BOOTSTRAP_ADMIN_PROVIDER="demo-admin-provider-user-id"
   export FACEBOOK_APP_SECRET="your-facebook-app-secret"
   export JWT_SECRET="$(openssl rand -base64 32)"
   ```

3. Run the API:

   ```sh
   ./gradlew bootRun --args='--spring.profiles.active=local'
   ```

## Local testing

- Unit tests: `./gradlew test`
- Integration tests: `./gradlew integrationTest`
- Full verification: `./gradlew check`

## Useful commands

```sh
docker compose up -d
docker compose down
docker compose down -v
./gradlew build
./gradlew clean build
```

## Authentication notes

- Public endpoints include:
  - `GET /figurines/**`
  - `GET /catalogs/{catalogType}/**`
  - `GET /anniversaries/**`
  - `GET /swagger-ui.html`
  - `GET /swagger.yaml`
  - `POST /collectors/auth/{provider}`
- Most other endpoints require authentication with role/permission claims.

For authenticated testing, use:

`POST /api/v1/collectors/auth/{provider}` (for example `google` or `facebook`)

Request body:

```json
{
  "idToken": "provider-id-token",
  "accessToken": "provider-access-token"
}
```
