# MediBook Backend – Implementation Walkthrough

This document explains what each **backend** source file does, focusing on:

- What each class is responsible for
- Important Spring annotations (e.g., `@RestController`, `@Service`, `@Transactional`)
- Data flow between Controller → Service → Repository → Entity

## Tech stack (backend)

- **Spring Boot**
- **Spring Web (REST)**
- **Spring Data JPA + Hibernate**
- **Spring Security** (JWT-based stateless auth)
- **MySQL** (dev runtime)
- **Lombok** (generates boilerplate code)

---

## High-level architecture

### Request flow

1. **Frontend** calls `/api/...` endpoint.
2. **JwtAuthFilter** reads `Authorization: Bearer <token>`.
3. If token is valid, it sets an authenticated `UserPrincipal` in the SecurityContext.
4. Controller receives request.
5. Controller calls a Service method.
6. Service uses Repositories (JPA) to read/write Entities.
7. Service returns DTOs (`*Response`) back to Controller.

### Packages

- `controller/`: REST endpoints
- `service/`: business logic
- `repository/`: database access
- `entity/`: JPA entities (tables)
- `dto/`: request/response payloads
- `security/`: JWT parsing + user principal
- `config/`: Spring Security config, CORS, seed data, slot generation
- `exception/`: custom exceptions + global handler

---

## Common Spring / Lombok annotations used

### Spring annotations

- `@SpringBootApplication`: marks the main entry point and enables component scanning + auto configuration.
- `@EnableScheduling`: enables scheduled tasks (even if currently not used heavily).
- `@RestController`: class exposes REST endpoints; methods return JSON.
- `@RequestMapping`: base path for controller endpoints.
- `@GetMapping`, `@PostMapping`, `@PutMapping`: HTTP method mappings.
- `@RequestBody`: binds incoming JSON body to a Java object.
- `@PathVariable`: binds URL path parameter.
- `@RequestParam`: binds query parameters.
- `@Valid`: triggers validation based on Jakarta Validation annotations in DTOs.
- `@Service`: marks service classes for DI.
- `@Transactional`: wraps method in a DB transaction.
  - `readOnly=true` is used for read-only methods to avoid lazy-loading errors and improve performance.
- `@Component`: generic bean.
- `@Configuration`: configuration class.
- `@Bean`: creates Spring-managed beans.

### Lombok annotations

- `@Getter/@Setter`: generates getters/setters.
- `@NoArgsConstructor/@AllArgsConstructor`: generates constructors.
- `@Builder`: enables builder pattern.
- `@Builder.Default`: sets default builder values.
- `@RequiredArgsConstructor`: generates constructor for final fields (DI).

---

## File-by-file documentation

## `AppointmentBookingApplication.java`

- **Purpose**: application entry point.
- `@SpringBootApplication`: enables component scanning and auto configuration.
- `@EnableScheduling`: enables Spring scheduling support.
- `main()`: runs the Spring Boot app.

---

## `config/` package

### `SecurityConfig.java`

- **Purpose**: configures Spring Security.
- `@EnableWebSecurity`: enables web security.
- `@EnableMethodSecurity`: enables method-level security annotations (future-ready).

- **Key beans**:
  - `corsConfigurationSource()`: CORS allowed origins for local dev.
  - `filterChain(HttpSecurity)`: defines security rules:
    - `/api/auth/**` → public
    - `/api/admin/**` → requires ADMIN role
    - `/api/**` → requires authentication
  - Adds `jwtAuthFilter` before `UsernamePasswordAuthenticationFilter`.
  - Defines `PasswordEncoder` as BCrypt.

### `WebConfig.java`

- **Purpose**: CORS config at MVC layer (in addition to Security CORS).
- Implements `WebMvcConfigurer.addCorsMappings`.
- Allows local dev origins.

### `DataInitializer.java`

- **Purpose**: seeds initial data on startup (admin user, services, doctors).
- Implements `CommandLineRunner`: runs once at app startup.
- Uses `@Order(1)` so it runs before slot generation.

- **Important behavior**:
  - Creates admin if missing (`admin@booking.com`).
  - Inserts services only if `services` table is empty.
  - Inserts doctors only if `doctors` table is empty.

### `SlotGenerator.java`

- **Purpose**: generates fixed slots for all doctors for N days ahead.
- Implements `CommandLineRunner` with `@Order(2)`.
- Reads `app.slot-generation.days-ahead` from `application.yaml`.
- Calls `slotService.generateSlotsForDoctors(daysAhead)`.

### `AppointmentStatusDbMigration.java`

- **Purpose**: compatibility migration for enum changes.
- Runs at startup (`CommandLineRunner`) with `@Order(0)`.
- Executes SQL:
  - `UPDATE appointments SET status='REJECTED' WHERE status='CANCELLED'`

This prevents Hibernate from throwing an exception if the DB contains old enum values.

---

## `security/` package

### `JwtUtil.java`

- **Purpose**: generate and parse JWT tokens.
- Reads:
  - `app.jwt.secret`
  - `app.jwt.expiration-ms`

- **Key methods**:
  - `generateToken(email, userId, role)`: creates token with claims.
  - `parseToken(token)`: validates signature and returns JWT claims.

### `JwtAuthFilter.java`

- **Purpose**: parses JWT from incoming requests.
- Extends `OncePerRequestFilter`: runs once per request.

- **Logic**:
  - Reads `Authorization` header.
  - If `Bearer <token>`:
    - parse token
    - extract `email`, `role`, `userId`
    - create `UsernamePasswordAuthenticationToken` with `ROLE_<role>`
    - store in `SecurityContextHolder`

### `UserPrincipal.java`

- **Purpose**: lightweight authenticated user object.
- Stored in Spring Security context.
- Contains `userId`, `email`, `role`.

---

## `entity/` package (JPA / Hibernate models)

### `User.java`

- Maps to table: `users`.
- Fields: `email`, `password`, `name`, `phone`, `role`.
- Relationship:
  - `@OneToMany(mappedBy="user")`: user → appointments.

### `AppointmentService.java`

- Maps to table: `services`.
- Fields: `name`, `description`.
- Relationship:
  - `@OneToMany(mappedBy="service")`: service → doctors.

### `Doctor.java`

- Maps to table: `doctors`.
- Fields: `name`, `title`, `weekdaySlotCount`, `weekendSlotCount`.
- Relationship:
  - `@ManyToOne` to `AppointmentService` (service_id foreign key).
  - `@OneToMany` to `Slot`.

### `Slot.java`

- Maps to table: `slots`.
- Fields: `slotDate`, `startTime`, `endTime`, `available`.
- Relationship:
  - `@ManyToOne` to `Doctor`.
  - `@OneToMany(mappedBy="slot")` to `Appointment`.

### `Appointment.java`

- Maps to table: `appointments`.
- Relationship:
  - `@ManyToOne` to `User` and `Slot`.

- `AppointmentStatus` enum:
  - `PENDING`, `APPROVED`, `REJECTED`, `COMPLETED`

- Lifecycle hooks:
  - `@PrePersist`: sets `createdAt` and `updatedAt` on insert.
  - `@PreUpdate`: sets `updatedAt` on update.

---

## `repository/` package

These are Spring Data JPA interfaces. Spring auto-generates SQL based on method names.

### `UserRepository.java`

- `findByEmail(email)`
- `existsByEmail(email)`

### `AppointmentServiceRepository.java`

- Basic CRUD for `AppointmentService`.

### `DoctorRepository.java`

- `findByServiceIdOrderByName(serviceId)`
- `findAllByOrderByServiceIdAscNameAsc()`

### `SlotRepository.java`

- Queries for available slots by doctor/date.
- Also used to prevent duplicate slots:
  - `existsByDoctorIdAndSlotDateAndStartTime(...)`

### `AppointmentRepository.java`

- `findByUserIdOrderByCreatedAtDesc(userId)`
- `findAllByOrderByCreatedAtDesc()`
- `existsBySlotIdAndStatusIn(slotId, statuses)` for preventing double-booking.

---

## `dto/` package

DTOs represent the JSON shape used by REST requests/responses.

- `LoginRequest`: login body.
- `RegisterRequest`: register body.
- `AuthResponse`: returned after login/register (token + user info).
- `BookAppointmentRequest`: booking request (slotId).
- `RescheduleRequest`: reschedule request (newSlotId).
- `AppointmentStatusRequest`: admin update status request.
- `AppointmentResponse`: appointment response shown to frontend.
- `ServiceResponse`, `DoctorResponse`, `SlotResponse`: list endpoints.
- `SlotRequest`: admin create slot body.

Validation:
- Most request DTOs use Jakarta Validation annotations (triggered by `@Valid` in controllers).

---

## `service/` package

### `AuthService` / `AuthServiceImpl`

- **Purpose**: user registration and login.

- `register(RegisterRequest)`:
  - checks email uniqueness
  - encodes password using BCrypt
  - saves user
  - returns JWT token + user fields

- `login(LoginRequest)`:
  - checks email exists
  - validates password
  - returns JWT token + user fields

### `SlotService` / `SlotServiceImpl`

- **Purpose**: slot querying and generation.

- `getAvailableSlots(doctorId, date)`:
  - returns slots where `available=true` (by doctor/date filters)

- `getSlotsForDate(doctorId, date)`:
  - returns **all** slots for that date, then sets `available=false` if an appointment exists with status `PENDING` or `APPROVED`.

- `createSlot(SlotRequest)`:
  - admin creates a new slot for a doctor.

- `generateSlotsForDoctors(daysAhead)`:
  - for each doctor and for each day:
    - chooses a time list based on weekday/weekend
    - creates slots if not already present

### `AppointmentService` / `AppointmentServiceImpl`

- **Purpose**: booking and managing appointments.

- `book(userId, request)` (`@Transactional`):
  - validates user and slot
  - prevents double booking via repository check
  - ensures slot is available
  - creates appointment with `PENDING`
  - marks slot `available=false`

- `getMyAppointments(userId)`:
  - returns appointments for a user.

- `cancel(userId, appointmentId)` (`@Transactional`):
  - verifies ownership
  - sets status to `REJECTED` (used as cancel)
  - frees slot (`available=true`)

- `reschedule(userId, appointmentId, newSlotId)` (`@Transactional`):
  - validates ownership
  - blocks reschedule if rejected/completed
  - switches slot
  - sets status back to `PENDING`
  - frees old slot and marks new slot unavailable

- `getAllAppointmentsForAdmin()` (`@Transactional(readOnly = true)`):
  - returns all appointments sorted by createdAt desc.

- `updateStatusByAdmin(appointmentId, status)` (`@Transactional`):
  - accepts only `APPROVED` or `REJECTED`
  - if rejected, frees slot

- `toResponse(Appointment)`:
  - converts entity to `AppointmentResponse`
  - includes an **auto-complete** step:
    - if appointment is in the past and status is `PENDING/APPROVED`, it updates status to `COMPLETED`.

---

## `controller/` package

Controllers are thin: they validate input and delegate to services.

### `AuthController.java`

- `/api/auth/register`
- `/api/auth/login`

### `AppointmentController.java`

- `/api/appointments/book`
- `/api/appointments/my`
- `/api/appointments/cancel/{id}`
- `/api/appointments/reschedule/{id}`

- Uses `@AuthenticationPrincipal UserPrincipal` to get the logged-in user.

### `AdminController.java`

- `/api/admin/slots` (create slots)
- `/api/admin/appointments` (list all)
- `/api/admin/appointments/{id}/status` (approve/reject)

### `ServiceController.java`

- `/api/services` (list all services)

### `DoctorController.java`

- `/api/doctors` (list all)
- `/api/doctors/by-service/{serviceId}`

### `SlotController.java`

- `/api/slots/available` (available slots; filters optional)
- `/api/slots/by-date` (all slots for a doctor+date)

- Uses `@DateTimeFormat(iso=DATE)` to parse query string into `LocalDate`.

---

## `exception/` package

### `BadRequestException` and `ResourceNotFoundException`

- Simple custom runtime exceptions.

### `GlobalExceptionHandler.java`

- `@RestControllerAdvice`: catches exceptions and returns JSON.

Handlers:
- `ResourceNotFoundException` → 404 `{ "error": "..." }`
- `BadRequestException` → 400 `{ "error": "..." }`
- `MethodArgumentNotValidException` → 400 `{ "errors": { field: message } }`

---

## Configuration file

### `src/main/resources/application.yaml`

- Sets server port `8095`.
- MySQL datasource:
  - `jdbc:mysql://localhost:3306/appointment...`
  - username/password.
- JPA:
  - `ddl-auto: update` (updates schema but does not drop existing data).
- Custom:
  - `app.jwt.secret` and `app.jwt.expiration-ms`
  - `app.slot-generation.days-ahead`

---

## Notes / important behaviors

- **Seed data is not an updater**: `DataInitializer` only inserts when tables are empty.
- **Enum mismatch**: if DB contains old enum strings (like `CANCELLED`), Hibernate will crash until DB is fixed or migrated.
- **Slot availability consistency**:
  - Booking/rescheduling/rejection updates slot availability.
  - `getSlotsForDate` recomputes availability based on appointments to avoid inconsistencies.
