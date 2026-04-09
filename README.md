# 📦 Myth Cloth API

Core backend API powering the **Myth Cloth Collection Manager** app.
This service handles data persistence, search, and management of **Saint
Seiya Cloth figurines**.

## 🚀 Features

-   Manage Myth Cloth figurines and related metadata
-   REST API built with **Spring Boot**
-   Persistence using **PostgreSQL**
-   Includes **Docker Compose** setup for local development
-   Supports expansion for distributors, catalogs, and more

## 🛠️ Tech Stack

-   **Java 21+**
-   **Spring Boot**
-   **Spring Data JPA**
-   **PostgreSQL**
-   **Docker / Docker Compose**
-   **Gradle**

## 🐳 Running PostgreSQL using Docker Compose

### Start the database

``` sh
docker compose up -d
```

### Stop the database

``` sh
docker compose down
```

### View logs

``` sh
docker compose logs -f
```

### Check running containers

``` sh
docker ps
```

## ▶️ Running the Application

``` sh
./gradlew bootRun
```

## 🧪 Running Tests

``` sh
./gradlew test
```

## 📄 API Documentation

After starting the app, open:

- `http://localhost:8080/api/v1/swagger-ui.html`

Raw OpenAPI file:

- `http://localhost:8080/api/v1/swagger.yaml`
