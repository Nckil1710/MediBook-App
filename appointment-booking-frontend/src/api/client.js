import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_URL || '/api';

export const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (r) => r,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export const auth = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
};

export const services = {
  list: () => api.get('/services'),
};

export const doctors = {
  list: () => api.get('/doctors'),
  byService: (serviceId) => api.get(`/doctors/by-service/${serviceId}`),
};

export const slots = {
  getAvailable: (doctorId, date) =>
    api.get('/slots/available', { params: { doctorId, date } }),
  /** All slots for doctor+date; each has available: true|false (false = booked) */
  byDate: (doctorId, date) =>
    api.get('/slots/by-date', { params: { doctorId, date } }),
};

export const appointments = {
  book: (data) => api.post('/appointments/book', data),
  my: () => api.get('/appointments/my'),
  cancel: (id) => api.put(`/appointments/cancel/${id}`),
  reschedule: (id, data) => api.put(`/appointments/reschedule/${id}`, data),
};

export const admin = {
  createSlot: (data) => api.post('/admin/slots', data),
  getAppointments: () => api.get('/admin/appointments'),
  updateAppointmentStatus: (id, status) =>
    api.put(`/admin/appointments/${id}/status`, { status }),
};
