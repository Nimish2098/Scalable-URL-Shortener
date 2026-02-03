# 🔗 URL Shortener Service

A **production-ready URL shortener** built using **Java and Spring Boot** that converts long URLs into compact **Base62 short codes** and redirects users using proper **HTTP 302** responses.  
The system uses **Redis** for high-speed caching, **MySQL** for persistent storage and analytics, supports **time-based URL expiration**, and is fully **Dockerized** for easy deployment.

---

## 🚀 Features

- Base62 short code generation (collision-free)
- HTTP 302 redirection with `Location` header
- Redis cache (cache-aside pattern)
- URL expiry using Redis TTL
- Click count tracking
- Global exception handling (`@ControllerAdvice`)
- Dockerized setup (App + MySQL + Redis)
- Environment-based configuration using `.env.local`

---

## 🏗️ Tech Stack

- **Java 17**
- **Spring Boot**
- **MySQL**
- **Redis**
- **Maven**
- **Docker & Docker Compose**

---

