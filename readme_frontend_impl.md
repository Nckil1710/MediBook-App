# MediBook Frontend – Implementation Walkthrough

This document explains what each **frontend** source file does, including the major React functions/hooks used.

## Tech stack (frontend)

- **React** (functional components)
- **React Router** (`Routes`, `Route`, `Navigate`, `Link`)
- **Axios** for HTTP
- **Tailwind CSS** for styling
- **Vite** for dev/build (`import.meta.env`)

---

## High-level architecture

- **Routing** is defined in `src/App.jsx`.
- **Authentication state** (current user + role) is stored in `localStorage` and exposed through `src/context/AuthContext.jsx`.
- **API calls** are centralized in `src/api/client.js`.
- **Layout** (sidebar + header + footer) is centralized in `src/components/Layout.jsx` and used by authenticated pages.

---

## File-by-file documentation

### `src/main.jsx`

- **Purpose**: React entry point.
- **Key functions**:
  - `createRoot(...).render(...)`: mounts React into `#root`.
  - `StrictMode`: development-only checks/warnings.

### `src/index.css`

- **Purpose**: global CSS.
- **Key points**:
  - `@import "tailwindcss";` enables Tailwind.
  - Sets base `body` font + default margin/min-height.

### `src/App.jsx`

- **Purpose**: main router + auth provider.
- **Key parts**:
  - `AuthProvider`: wraps the app to provide auth state.
  - `AppRoutes()`: chooses which routes exist / which are protected.
  - Uses `useAuth()` to wait for initial loading, then renders routes.

- **Routing behavior**:
  - `/login`, `/register`: if already logged in, redirect to `/dashboard`.
  - `ProtectedRoute`: wraps authenticated pages.
  - Admin-only routes use `ProtectedRoute adminOnly`.

### `src/api/client.js`

- **Purpose**: the single place for all HTTP requests.
- **Key pieces**:
  - `API_BASE`: uses `VITE_API_URL` if set, otherwise defaults to `/api`.
  - `axios.create({ baseURL })`: creates a configured Axios instance.

- **Interceptors**:
  - **Request interceptor**: adds `Authorization: Bearer <token>` if token exists in `localStorage`.
  - **Response interceptor**: if backend returns `401`, clears auth data and redirects to `/login`.

- **Exports**:
  - `auth.register`, `auth.login`
  - `services.list`
  - `doctors.list`, `doctors.byService`
  - `slots.getAvailable`, `slots.byDate`
  - `appointments.book`, `appointments.my`, `appointments.cancel`, `appointments.reschedule`
  - `admin.createSlot`, `admin.getAppointments`, `admin.updateAppointmentStatus`

### `src/context/AuthContext.jsx`

- **Purpose**: stores and exposes authentication state (user + role).
- **Key hooks**:
  - `useState`: stores `user` and `loading`.
  - `useEffect`: runs once on mount to load user from `localStorage`.

- **Key functions**:
  - `login(data)`: persists token + user fields into `localStorage` and updates React state.
  - `logout()`: clears token + user.

- **Key values**:
  - `isAdmin`: `true` if `user.role === 'ADMIN'`.

### `src/components/ProtectedRoute.jsx`

- **Purpose**: route guard for authenticated and admin-only pages.
- **How it works**:
  - Reads `user`, `loading`, `isAdmin` from `useAuth()`.
  - If not logged in → redirects to `/login`.
  - If adminOnly and not admin → redirects to `/dashboard`.

### `src/components/Layout.jsx`

- **Purpose**: shared authenticated layout.
- **UI structure**:
  - Sidebar navigation (Dashboard / About Us / Book / My Appointments / Profile).
  - Admin-only links: Manage Slots, Admin Appointments.
  - Top bar with hamburger (mobile sidebar toggle).
  - Footer section: “Contact Us”.

- **Key hooks**:
  - `useLocation()`: highlights current active nav item.
  - `useNavigate()`: programmatic navigation on sign-out.
  - `useState(sidebarOpen)`: controls sidebar visibility on mobile.

- **Key function**:
  - `handleSignOut()`: clears auth storage then navigates to `/login`.

### `src/components/DashboardHeader.jsx`

- **Purpose**: small top header for pages (book button + profile avatar shortcut).
- **Key props**:
  - `title`: shown by some pages.
  - `showBookButton`: when true, shows a “Book Appointment” button.

### `src/pages/Login.jsx`

- **Purpose**: login form.
- **State**:
  - `email`, `password`: controlled inputs.
  - `error`, `loading`: UX state.

- **Key function**:
  - `handleSubmit(e)`: calls `auth.login()`, then `login(data)` (AuthContext), then redirects based on role.

### `src/pages/Register.jsx`

- **Purpose**: register form.
- **Extra validations**:
  - password match check
  - min length check

- **Key function**:
  - `handleSubmit(e)`: calls `auth.register()` then `login(data)` then navigates to `/dashboard`.

### `src/pages/Logout.jsx`

- **Purpose**: clears session when user visits `/logout`.
- **Key hook**:
  - `useEffect`: runs once to call `logout()` and then redirect to `/login`.

### `src/pages/Dashboard.jsx`

- **Purpose**: patient dashboard.
- **Data fetching**:
  - Loads `appointments.my()` for appointment list.
  - Computes “Available Slots Today” by:
    - fetching all doctors
    - for each doctor fetching `slots.getAvailable(doctorId, today)`
    - filtering out times that are already in the past

- **Key helper functions**:
  - `toLocalDateStr(date)`: avoids UTC offset bugs.
  - `StatusBadge`: maps status → Tailwind badge style.

### `src/pages/BookAppointment.jsx`

- **Purpose**: service → doctor → date → slot selection and booking.
- **Key hooks**:
  - `useMemo`: computes calendar grid for selected month.
  - `useLocation`: reads `rescheduleAppointment` if navigation came from reschedule.
  - `useNavigate`: redirects after reschedule.

- **Booking workflow**:
  - Fetch services.
  - When service chosen → fetch doctors for that service.
  - When doctor+date chosen → call `slots.byDate()` to fetch all slots, then disable booked/past slots.
  - Submit:
    - If rescheduling: `appointments.reschedule(id, { newSlotId })`.
    - Else booking: `appointments.book({ slotId })`.

### `src/pages/MyAppointments.jsx`

- **Purpose**: lists the user’s appointments.
- **Actions**:
  - Cancel: calls `appointments.cancel(id)`.
  - Reschedule: navigates to `/book` with `state: { rescheduleAppointment: appointment }`.

### `src/pages/Profile.jsx`

- **Purpose**: displays logged-in user profile details (read-only).

### `src/pages/AdminSlots.jsx`

- **Purpose**: admin page to manually create extra slots.
- **Behavior**:
  - Loads doctors list.
  - Submits `admin.createSlot({ doctorId, slotDate, startTime, endTime })`.

### `src/pages/AdminAppointments.jsx`

- **Purpose**: admin appointment management.
- **Behavior**:
  - Loads all appointments via `admin.getAppointments()`.
  - Approve/Reject only shown for `PENDING` status.
  - Updates status via `admin.updateAppointmentStatus(id, status)` and updates UI in-place.

### `src/pages/AboutUs.jsx`

- **Purpose**: About Us content as a dedicated authenticated page.

### CSS usage

- The UI is primarily styled using **Tailwind utility classes** in JSX.
- The only required stylesheet entry is `src/index.css`, which:
  - loads Tailwind via `@import "tailwindcss";`
  - applies a few global styles (e.g., base `body` font/margins)
- Legacy page-level CSS files (for example, `src/pages/*.css` and `src/App.css`) were removed because they were not imported anywhere.

---

## Common React concepts used

- **Controlled inputs**: form inputs use `value` and `onChange` tied to component state.
- **`useEffect`**: runs side effects (data fetching) after render.
- **`useMemo`**: memoizes expensive computations (calendar grid).
- **`useNavigate`**: programmatic navigation.
- **`Link`**: client-side navigation without full reload.

---

## Notes / improvements you might consider later

- Move duplicated helpers (`formatTime`, `toLocalDateStr`, `StatusBadge`) into a shared `utils/` module.
- Replace `window.location.reload()` in `Layout` sign-out with `logout()` from `AuthContext` (cleaner state handling).
