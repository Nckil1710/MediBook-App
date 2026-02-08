# Postman API Reference – Smart Appointment Booking

**Base URL:** `http://localhost:8080/api`

**Auth:** Use **Authorization** → Type **Bearer Token** → paste the `token` from login/register. Use for all endpoints except **1** and **2**.

---

## Quick reference

| # | Method | Endpoint | Auth |
|---|--------|----------|------|
| 1 | POST | `/auth/register` | No |
| 2 | POST | `/auth/login` | No |
| 3 | GET | `/services` | Yes |
| 4 | GET | `/doctors` | Yes |
| 4b | GET | `/doctors/by-service/{serviceId}` | Yes |
| 5 | GET | `/slots/available?doctorId=&date=` | Yes |
| 6 | POST | `/appointments/book` | Yes |
| 7 | GET | `/appointments/my` | Yes |
| 8 | PUT | `/appointments/cancel/{id}` | Yes |
| 9 | PUT | `/appointments/reschedule/{id}` | Yes |
| 10 | POST | `/admin/slots` | Admin |
| 11 | GET | `/admin/appointments` | Admin |
| 12 | PUT | `/admin/appointments/{id}/status` | Admin |
| 13 | POST | `/notifications/send` | Yes |

**Services:** General Consultation, Dental, Physiotherapy.  
**Doctors:** 2 per service (6 total). Slots are auto-generated per day for all doctors (weekdays 4–6 slots, weekends 2–3 per doctor).

---

## 1. Register

| Field | Value |
|-------|--------|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/api/auth/register` |
| **Headers** | `Content-Type: application/json` |

**Body (raw JSON):**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "9876543210"
}
```
`phone` is optional. Save the `token` from the response.

---

## 2. Login

| Field | Value |
|-------|--------|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/api/auth/login` |
| **Headers** | `Content-Type: application/json` |

**Body (raw JSON) – admin:**
```json
{
  "email": "admin@booking.com",
  "password": "admin123"
}
```

**Body – user:** Use the email/password from **Register**. Save the `token` for subsequent requests.

---

## 3. List services

| Field | Value |
|-------|--------|
| **Method** | `GET` |
| **URL** | `http://localhost:8080/api/services` |
| **Headers** | `Authorization: Bearer <token>` |

No body. Use response to get `id` for **List doctors by service** (e.g. 1 = General, 2 = Dental, 3 = Physiotherapy).

---

## 4. List doctors

| Field | Value |
|-------|--------|
| **Method** | `GET` |
| **URL** | `http://localhost:8080/api/doctors` |
| **Headers** | `Authorization: Bearer <token>` |

No body. Returns all doctors with `id`, `name`, `title`, `serviceId`, `serviceName`, `weekdaySlotCount`, `weekendSlotCount`.

**Doctors by service:**

| Field | Value |
|-------|--------|
| **Method** | `GET` |
| **URL** | `http://localhost:8080/api/doctors/by-service/1` |
| **Headers** | `Authorization: Bearer <token>` |

Replace `1` with a service id from **List services**. Use a `doctorId` from the response for **Get available slots**.

---

## 5. Get available slots (by doctor + date)

| Field | Value |
|-------|--------|
| **Method** | `GET` |
| **URL** | `http://localhost:8080/api/slots/available?doctorId=1&date=2026-02-15` |
| **Headers** | `Authorization: Bearer <token>` |

**Query params:**

| Param | Example | Description |
|-------|---------|-------------|
| `doctorId` | `1` | From **List doctors** or **doctors/by-service/{id}** |
| `date` | `2026-02-15` | `YYYY-MM-DD` (today or future) |

No body. Use a returned slot `id` for **Book appointment**.

---

## 6. Book appointment

| Field | Value |
|-------|--------|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/api/appointments/book` |
| **Headers** | `Content-Type: application/json`, `Authorization: Bearer <token>` |

**Body (raw JSON):**
```json
{
  "slotId": 1,
  "notes": "Optional note for the appointment"
}
```
`slotId` = id from **Get available slots**. `notes` optional.

---

## 7. My appointments

| Field | Value |
|-------|--------|
| **Method** | `GET` |
| **URL** | `http://localhost:8080/api/appointments/my` |
| **Headers** | `Authorization: Bearer <token>` |

No body. Returns the logged-in user’s appointments (includes `doctorName`, `serviceName`, `slotDate`, `startTime`, `endTime`, `status`).

---

## 8. Cancel appointment

| Field | Value |
|-------|--------|
| **Method** | `PUT` |
| **URL** | `http://localhost:8080/api/appointments/cancel/1` |
| **Headers** | `Authorization: Bearer <token>` |

Replace `1` with the appointment `id`. No body.

---

## 9. Reschedule appointment

| Field | Value |
|-------|--------|
| **Method** | `PUT` |
| **URL** | `http://localhost:8080/api/appointments/reschedule/1` |
| **Headers** | `Content-Type: application/json`, `Authorization: Bearer <token>` |

Replace `1` with the appointment `id`.

**Body (raw JSON):**
```json
{
  "newSlotId": 2,
  "notes": "Optional updated note"
}
```
`notes` optional.

---

## 10. Admin – Create slot

| Field | Value |
|-------|--------|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/api/admin/slots` |
| **Headers** | `Content-Type: application/json`, `Authorization: Bearer <admin_token>` |

**Body (raw JSON):**
```json
{
  "doctorId": 1,
  "slotDate": "2026-02-15",
  "startTime": "09:00",
  "endTime": "09:30"
}
```
Use **admin token** (login as `admin@booking.com`). Slots are auto-generated per doctor per day; this adds an extra slot for a doctor on a given date.

---

## 11. Admin – All appointments

| Field | Value |
|-------|--------|
| **Method** | `GET` |
| **URL** | `http://localhost:8080/api/admin/appointments` |
| **Headers** | `Authorization: Bearer <admin_token>` |

No body. Returns all appointments in the system.

---

## 12. Admin – Approve / Reject appointment

| Field | Value |
|-------|--------|
| **Method** | `PUT` |
| **URL** | `http://localhost:8080/api/admin/appointments/1/status` |
| **Headers** | `Content-Type: application/json`, `Authorization: Bearer <admin_token>` |

Replace `1` with the appointment `id`.

**Body (raw JSON):**
```json
{
  "status": "APPROVED"
}
```
Or `"status": "REJECTED"`.

---

## 13. Send notification (optional)

| Field | Value |
|-------|--------|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/api/notifications/send` |
| **Headers** | `Content-Type: application/json`, `Authorization: Bearer <token>` |

**Body (raw JSON):**
```json
{
  "toEmail": "user@example.com",
  "toPhone": "9876543210",
  "subject": "Reminder",
  "message": "Your appointment is tomorrow at 9 AM."
}
```

---

## Suggested testing flow in Postman

1. **Environment (optional)**  
   - Variable `base_url` = `http://localhost:8080/api`  
   - Variable `token` = (set after login)

2. **Login (admin)**  
   - **POST** `{{base_url}}/auth/login` with admin body → copy `token` → set as `token` in environment.

3. **List services**  
   - **GET** `{{base_url}}/services` → note `id` (e.g. 1, 2, 3).

4. **List doctors (all or by service)**  
   - **GET** `{{base_url}}/doctors` or **GET** `{{base_url}}/doctors/by-service/1` → note a `doctorId` (e.g. 1).

5. **Get available slots**  
   - **GET** `{{base_url}}/slots/available?doctorId=1&date=2026-02-15`  
   - Use a real date (today or later). Note a slot `id`.

6. **Register / Login (user)**  
   - **POST** `{{base_url}}/auth/register` or login with user credentials → set user `token`.

7. **Book appointment**  
   - **POST** `{{base_url}}/appointments/book` with `{"slotId": <id>, "notes": "Test"}`.

8. **My appointments**  
   - **GET** `{{base_url}}/appointments/my` → confirm the new booking.

9. **Admin: all appointments and status**  
   - Set `token` back to admin. **GET** `{{base_url}}/admin/appointments`.  
   - **PUT** `{{base_url}}/admin/appointments/<id>/status` with `{"status": "APPROVED"}` or `"REJECTED"`.

**Authorization:** In each request, **Authorization** tab → Type **Bearer Token** → Token: `{{token}}`.
