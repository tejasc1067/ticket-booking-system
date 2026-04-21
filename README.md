# 🎫 Ticket Booking System

A production-ready, full-stack ticket booking platform built with **Spring Boot**, **React**, **PostgreSQL**, and **Redis**.

## 🧱 Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 17, Spring Boot 3.4.5, Spring Security, Spring Data JPA |
| **Frontend** | React 19, Vite, React Router, Axios |
| **Database** | PostgreSQL (production), H2 (development) |
| **Cache** | Redis (seat locking) |
| **Auth** | JWT (JJWT 0.12.6) |

## 📁 Project Structure

```
ticket-booking-system/
├── backend/                    # Spring Boot API
│   └── src/main/java/com/ticketbooking/
│       ├── config/             # Security, CORS, Redis config
│       ├── controller/         # REST controllers
│       ├── dto/                # Request/Response DTOs + Mapper
│       ├── entity/             # JPA entities
│       ├── enums/              # BookingStatus, SeatStatus, etc.
│       ├── exception/          # Custom exceptions + GlobalHandler
│       ├── repository/         # Spring Data JPA repositories
│       ├── security/           # JWT filter, service, UserDetails
│       └── service/            # Business logic
├── frontend/                   # React SPA
│   └── src/
│       ├── components/         # Navbar
│       ├── context/            # AuthContext
│       ├── pages/              # Home, Login, Register, EventDetail, SeatSelection, MyBookings
│       └── services/           # Axios API client
└── README.md
```

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- Redis (for seat locking)
- PostgreSQL (optional, H2 used in dev)

### Backend Setup

```bash
cd backend

# Run with H2 (no external DB needed)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run with PostgreSQL
./mvnw spring-boot:run
```

Backend runs at: `http://localhost:8080`

### Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at: `http://localhost:5173`

## 🔐 API Endpoints

### Auth
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register new user |
| POST | `/api/auth/login` | Public | Login & get JWT |

### Events
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/events` | Public | List all events |
| GET | `/api/events/{id}` | Public | Get event details |
| GET | `/api/events/city/{city}` | Public | Filter by city |
| GET | `/api/events/search?keyword=` | Public | Search events |
| POST | `/api/events` | Admin | Create event |
| PUT | `/api/events/{id}` | Admin | Update event |
| DELETE | `/api/events/{id}` | Admin | Delete event |

### Shows
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/shows/event/{eventId}` | Public | List shows |
| GET | `/api/shows/event/{eventId}/upcoming` | Public | Upcoming shows |
| POST | `/api/shows/event/{eventId}` | Admin | Create show |

### Seats
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/seats/show/{showId}` | Public | All seats |
| GET | `/api/seats/show/{showId}/available` | Public | Available seats |
| POST | `/api/seats/lock` | Auth | Lock seats (5 min) |
| POST | `/api/seats/unlock` | Auth | Unlock seats |

### Bookings
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/bookings` | Auth | Create booking |
| GET | `/api/bookings/my` | Auth | My bookings |
| GET | `/api/bookings/reference/{ref}` | Auth | Lookup by reference |
| PUT | `/api/bookings/{id}/cancel` | Auth | Cancel booking |

## 🏗️ Architecture

### Booking Flow
```
1. Browse Events → Select Show → View Seats
2. Select Seats → Lock (Redis SET NX, 5min TTL)
3. Confirm Booking → Validate Lock → Pessimistic DB Lock → Book → Release Lock
4. If abandoned → Lock auto-expires
```

### Concurrency Protection (3 Layers)
1. **Redis Lock** — Application-level, prevents concurrent seat selection
2. **Pessimistic Lock** — Database row-level lock during booking transaction
3. **Optimistic Lock** — `@Version` on Seat/Show entities with auto-retry

### Security
- JWT stateless authentication
- BCrypt password hashing
- Role-based access (CUSTOMER / ADMIN)
- Public read, authenticated write, admin management

## 📊 Database Schema

```
Users ──────────┐
                ├──< Bookings >──┬── Shows ──< Events
Seats >─────────┘                │
  │                              │
  └──── (booking_seats) ────────┘
```

## 🛠️ Configuration

### Environment Variables
| Property | Default | Description |
|---|---|---|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/ticket_booking_db` | Database URL |
| `spring.data.redis.host` | `localhost` | Redis host |
| `spring.data.redis.port` | `6379` | Redis port |
| `jwt.secret` | (base64 key) | JWT signing key |
| `jwt.expiration` | `86400000` | Token expiry (24h) |
