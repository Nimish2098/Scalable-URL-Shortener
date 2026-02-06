# 🔗 URL Shortener Service

A **production-ready URL shortener** built using **Java and Spring Boot** that converts long URLs into compact **Base62 short codes** and redirects users using proper **HTTP 302** responses.  
The system uses **Redis** for high-speed caching, **MySQL** for persistent storage and analytics, supports **time-based URL expiration**, and is fully **Dockerized** for easy deployment.

---

## 🚀 Features

- **High Performance**: Redis caching (cache-aside) for sub-millisecond redirects.
- **Analytics**: Tracks click counts and Geo Location (City, Country).
- **Notifications**: Async Slack notifications for every link access.
- **Expiry**: smart TTL management for temporary links.
- **Scalable**: Dockerized setup with MySQL and Redis.
- **Security**: Environment-based configuration.

---

## 🛠️ Tech Stack

- **Java 17** & **Spring Boot 3**
- **MySQL 8** (Persistence)
- **Redis** (Caching & Rate Limiting)
- **Docker & Docker Compose**
- **Maven** (Build Tool)

---

## ⚙️ Configuration

The application uses a `.env` file for sensitive configuration. Create a file named `.env` in the root directory:

```properties
# Database
DB_URL=jdbc:mysql://localhost:3306/urlshort_db
DB_USERNAME=root
DB_PASSWORD=your_password

# Redis
REDIS_HOST=localhost

# Notifications
SLACK_WEBHOOK_URL=https://hooks.slack.com/services/YOUR/WEBHOOK/URL
```

---

## 🏃‍♂️ Getting Started

### Option 1: Docker (Recommended)
Run the entire stack (App + DB + Redis) with one command:

```bash
docker-compose up --build
```
The app will be available at `http://localhost:8081`.

### Option 2: Local Development
**Prerequisites**: MySQL and Redis running locally.

1.  **Clone the repo**:
    ```bash
    git clone https://github.com/Nimish2098/Scalable-URL-Shortener.git
    cd urlshortener
    ```
2.  **Build**:
    ```bash
    ./mvnw clean package
    ```
3.  **Run**:
    ```bash
    ./mvnw spring-boot:run
    ```

---

## 🔌 API Documentation

### 1. Shorten URL
Values `trackingTag` and `expiresAt` are optional.

- **Endpoint**: `POST /api/shorten`
- **Body**:
  ```json
  {
    "longUrl": "https://www.google.com",
    "trackingTag": "MarketingCampaign",
    "expiresAt": "2025-12-31T23:59:59"
  }
  ```
- **Response**: `202 Accepted`
  ```text
  http://localhost:8081/AbC12
  ```

### 2. Redirect
- **Endpoint**: `GET /{shortCode}`
- **Response**: `302 Found` (Redirects to original URL)

### 3. Get Stats
- **Endpoint**: `GET /api/stats/{shortCode}`
- **Response**: `200 OK`
  ```json
  {
    "shortCode": "AbC12",
    "longUrl": "https://www.google.com",
    "clickCount": 42,
    "createdAt": "2024-02-06T10:00:00",
    "expired": false
  }
  ```

---

## 🧪 Testing

Run unit tests using Maven:
```bash
./mvnw test
```
