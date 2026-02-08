# API Testing & MySQL Persistence

Backend base URL: **http://localhost:8080/api**

MySQL: schema **appointment**, user **root**, password **root@123**.

---

## Quick test (PowerShell)

From the `appointment-booking-backend` folder:

```powershell
.\api-test.ps1
```

This will: register/login → list services → list slots → book (if slots exist) → list my appointments → admin login → create slot → list all appointments.

---

## Manual API tests (curl)

### 1. Register

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Test User\",\"email\":\"test@example.com\",\"password\":\"test123\",\"phone\":\"9999999999\"}"
```

Save the `token` from the response.

### 2. Login

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"admin@booking.com\",\"password\":\"admin123\"}"
```

Use the returned `token` as `Bearer <token>` in the next requests.

### 3. List services (requires auth)

```bash
curl -s http://localhost:8080/api/services \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4. Available slots (requires auth)

Use a `serviceId` from step 3 and a date in `YYYY-MM-DD`:

```bash
curl -s "http://localhost:8080/api/slots/available?serviceId=1&date=2026-02-10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 5. Book appointment (requires auth)

Use a `slotId` from step 4:

```bash
curl -s -X POST http://localhost:8080/api/appointments/book \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d "{\"slotId\":1,\"notes\":\"First booking\"}"
```

### 6. My appointments (requires auth)

```bash
curl -s http://localhost:8080/api/appointments/my \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 7. Admin: create slot (admin token only)

```bash
curl -s -X POST http://localhost:8080/api/admin/slots \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -d "{\"serviceId\":1,\"slotDate\":\"2026-02-10\",\"startTime\":\"09:00\",\"endTime\":\"09:30\"}"
```

### 8. Admin: all appointments (admin token only)

```bash
curl -s http://localhost:8080/api/admin/appointments \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

### 9. Admin: approve/reject appointment

```bash
curl -s -X PUT http://localhost:8080/api/admin/appointments/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -d "{\"status\":\"APPROVED\"}"
```

### 10. Cancel appointment (user token)

```bash
curl -s -X PUT http://localhost:8080/api/appointments/cancel/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Verify persistence in MySQL

Connect to MySQL (command line or MySQL Workbench):

```bash
mysql -u root -p
# password: root@123
USE appointment;
```

### Tables created by Hibernate (ddl-auto: update)

- `users` – registered users and admin
- `services` – appointment services (seeded on startup)
- `slots` – time slots per service/date
- `appointments` – booked appointments

### Useful queries

```sql
-- List users (admin + registered)
SELECT id, email, name, role FROM users;

-- List services
SELECT * FROM services;

-- List slots (with service name via join)
SELECT s.id, s.slot_date, s.start_time, s.end_time, s.available, sv.name AS service_name
FROM slots s
JOIN services sv ON s.service_id = sv.id
ORDER BY s.slot_date, s.start_time;

-- List appointments with user and slot info
SELECT a.id, u.name AS user_name, u.email, sv.name AS service_name,
       s.slot_date, s.start_time, s.end_time, a.status, a.created_at
FROM appointments a
JOIN users u ON a.user_id = u.id
JOIN slots s ON a.slot_id = s.id
JOIN services sv ON s.service_id = sv.id
ORDER BY a.created_at DESC;
```

After running the API tests (or the app), run these queries and confirm rows appear and match the API responses.

---

## Default data (on first run)

- **Admin user:** email `admin@booking.com`, password `admin123`
- **Services:** General Consultation, Dental, Physiotherapy (inserted by `DataInitializer`)

If the schema is empty, Hibernate creates tables and the initializer inserts admin + services. Add slots via **POST /api/admin/slots** (as admin), then book via **POST /api/appointments/book** (as user).
