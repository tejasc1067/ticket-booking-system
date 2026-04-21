import axios from 'axios';

// In dev: Vite proxy or direct backend URL
// In production (Docker): nginx proxies /api to backend
const API_BASE_URL = import.meta.env.DEV ? 'http://localhost:8080/api' : '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach JWT token to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 responses globally
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth
export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
};

// Events
export const eventAPI = {
  getAll: () => api.get('/events'),
  getById: (id) => api.get(`/events/${id}`),
  getByCity: (city) => api.get(`/events/city/${city}`),
  search: (keyword) => api.get(`/events/search?keyword=${keyword}`),
  create: (data) => api.post('/events', data),
  update: (id, data) => api.put(`/events/${id}`, data),
  delete: (id) => api.delete(`/events/${id}`),
};

// Shows
export const showAPI = {
  getByEvent: (eventId) => api.get(`/shows/event/${eventId}`),
  getUpcoming: (eventId) => api.get(`/shows/event/${eventId}/upcoming`),
  getById: (id) => api.get(`/shows/${id}`),
  create: (eventId, data) => api.post(`/shows/event/${eventId}`, data),
};

// Seats
export const seatAPI = {
  getByShow: (showId) => api.get(`/seats/show/${showId}`),
  getAvailable: (showId) => api.get(`/seats/show/${showId}/available`),
  lock: (data) => api.post('/seats/lock', data),
  unlock: (data) => api.post('/seats/unlock', data),
};

// Bookings
export const bookingAPI = {
  create: (data) => api.post('/bookings', data),
  getMy: () => api.get('/bookings/my'),
  getByReference: (ref) => api.get(`/bookings/reference/${ref}`),
  cancel: (bookingId) => api.put(`/bookings/${bookingId}/cancel`),
};

export default api;
