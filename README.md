# Smart Appointment Booking System

Full-stack appointment booking application built with **Spring Boot** (backend) and **React** (frontend), as per the detailed specification. It supports user registration/login, viewing available slots, booking/cancelling/rescheduling appointments, admin slot and appointment management, and notifications.

## Tech Stack

- **Backend:** Spring Boot 3, Spring Data JPA, Spring Security, JWT, H2 (dev) / MySQL or PostgreSQL
- **Frontend:** React (Vite), Axios, React Router
- **Security:** JWT-based authentication, role-based access (USER, ADMIN)

## Project Structure

```
Sample/
├── appointment-booking-backend/   # Spring Boot REST API
└── appointment-booking-frontend/  # React SPA
```

## Quick Start

### Backend

1. Open `appointment-booking-backend` in your IDE (e.g. IntelliJ / Eclipse) or use Maven from the command line.
2. Run the main class `AppointmentBookingApplication` or run: `mvn spring-boot:run` (or `./mvnw spring-boot:run` if you have the Maven wrapper).
3. API runs at **http://localhost:8080**.
4. Default admin: **admin@booking.com** / **admin123**.

### Frontend

1. Open a terminal in `appointment-booking-frontend`.
2. Run: `npm install` then `npm run dev`.
3. App runs at **http://localhost:3000**.

### First Use

1. Register a new user or sign in as admin.
2. As **admin**: go to "Manage slots", select a service, add a date and time range to create slots.
3. As **user**: go to "Book appointment", choose service and date, pick a slot and book.
4. View/cancel in "My appointments". Admin can approve/reject in "Admin: Appointments".

## API Endpoints (summary)

| Area | Method | Endpoint | Description |
|------|--------|----------|-------------|
| Auth | POST | `/api/auth/register` | Register user |
| Auth | POST | `/api/auth/login` | Login (returns JWT) |
| Slots | GET | `/api/slots/available?serviceId=&date=` | Available slots |
| Appointments | POST | `/api/appointments/book` | Book appointment |
| Appointments | GET | `/api/appointments/my` | My appointments |
| Appointments | PUT | `/api/appointments/cancel/{id}` | Cancel |
| Appointments | PUT | `/api/appointments/reschedule/{id}` | Reschedule |
| Admin | POST | `/api/admin/slots` | Create slot |
| Admin | GET | `/api/admin/appointments` | All appointments |
| Admin | PUT | `/api/admin/appointments/{id}/status` | Approve/Reject |
| Services | GET | `/api/services` | List services |

## Configuration

- **Backend:** `application.yaml` – port, datasource, JWT secret/expiry, mail (for notifications).
- **Frontend:** Set `VITE_API_URL` to your API base URL (default: `http://localhost:8080/api`).

## References

- Design and APIs aligned with the Smart Appointment Booking System detailed document.
- Backend structure and patterns inspired by [Cafe-API](https://github.com/Nckil1710/Cafe-API) and [ECom-msa](https://github.com/Nckil1710/ECom-msa) (Entity/Repository/Service/Controller, DTOs, exception handling).
