#  Booking System Application

##  Overview
This is a **Spring Boot-based Booking System** that allows users to:
- Register **units** (homes, flats, apartments) for booking.
- Search for **available units** based on filters like **dates, price, and type**.
- **Book a unit** and manage **payments**.
- Use **Redis caching** for performance optimization.
- **Auto-cancel unpaid bookings** after **15 minutes**.
- **Swagger UI** for API documentation.

---

##  Tech Stack
- **Java 21**
- **Spring Boot 3.3.10-SNAPSHOT**
- **Spring Web, Spring Data JPA, Spring Boot Cache**
- **PostgreSQL (Database)**
- **Liquibase (Database migrations)**
- **Redis (Caching & Expiring Payments)**
- **Springdoc OpenAPI (Swagger UI)**
- **MapStruct (DTO Mapping)**
- **JUnit 5 & Mockito (Testing)**
- **Gradle (Build & Dependency Management)**

---

##  Setup & Installation

### 1️⃣ Prerequisites
- **Java 21**
- **Gradle**
- **Docker & Docker Compose**

### 2️⃣ Clone the Repository
```bash
git clone https://github.com/your-repo/bookingsystem.git
cd bookingsystem
```

### 3️⃣ Start PostgreSQL & Redis (Docker)
```bash
docker compose up -d
```

### 4️⃣ Run the Application
```bash
./gradlew bootRun
```

### 5️⃣ Access API Documentation
- **Swagger UI**: [http://localhost:8080/swagger](http://localhost:8080/swagger)
- **OpenAPI Docs (JSON)**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

---

##  API Endpoints

### 1️⃣ User Management
| Method | Endpoint | Description |
|--------|---------|------------|
| `POST` | `/users` | Create a new user |
| `GET` | `/users/{id}` | Get user details |

### 2️⃣ Unit Management
| Method | Endpoint | Description |
|--------|---------|------------|
| `POST` | `/units` | Add a new unit |
| `GET` | `/units/search` | Search for available units |
| `GET` | `/units/available-count` | Get available unit count |

### 3️⃣ Booking & Payments
| Method | Endpoint | Description |
|--------|---------|------------|
| `POST` | `/bookings` | Book a unit |
| `POST` | `/payments/{bookingId}/pay` | Make a payment |
| `GET` | `/payments/{paymentId}` | Get payment status |

---

##  Database Migration
- The database schema is **automatically managed** using **Liquibase**.
- **To apply migrations manually:**
```bash
./gradlew update
```

---

##  Running Tests
```bash
./gradlew test
```

---

## 📌 Environment Variables
| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/spribe` | Database URL |
| `SPRING_DATASOURCE_USERNAME` | `spribe_user` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | `spribe_password` | Database password |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |

---

## 📌 Key Features
✅ **Redis-backed caching** for faster unit availability lookup.  
✅ **15-minute expiration policy** for unpaid bookings.  
✅ **Automated database migrations** with Liquibase.  
✅ **Springdoc OpenAPI for clear API documentation**.  
✅ **Thread-safe & optimized** parallel booking handling.

---

## 📌 Author
🚀 Developed by **Mikhail Holub**  
🔗 GitHub: [Your GitHub Profile](https://github.com/your-profile)

---

