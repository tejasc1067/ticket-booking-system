# 🎫 Ticket Booking System

A production-ready ticket booking system built with **Java Spring Boot**, **React**, **PostgreSQL**, and **Redis**.

## Tech Stack

### Backend
- Java 17 + Spring Boot 3.5
- Spring Security (JWT)
- Spring Data JPA
- PostgreSQL
- Redis (seat locking)

### Frontend
- React
- Axios

### DevOps
- Docker & Docker Compose

## Project Structure

```
ticket-booking-system/
├── backend/          # Spring Boot API
├── frontend/         # React application
└── README.md
```

## Getting Started

### Prerequisites
- Java 17+
- Maven
- PostgreSQL
- Node.js 18+

### Backend Setup

```bash
cd backend
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

### Health Check

```
GET http://localhost:8080/api/health
```

## License

MIT
