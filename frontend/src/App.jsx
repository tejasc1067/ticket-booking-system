import { useState } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import EventDetail from './pages/EventDetail';
import SeatSelection from './pages/SeatSelection';
import MyBookings from './pages/MyBookings';

export default function App() {
  const [selectedCity, setSelectedCity] = useState('Pandharpur');

  return (
    <BrowserRouter>
      <AuthProvider>
        <Navbar selectedCity={selectedCity} onCityChange={setSelectedCity} />
        <Routes>
          <Route path="/" element={<Home selectedCity={selectedCity} />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/events/:id" element={<EventDetail />} />
          <Route path="/shows/:showId/seats" element={<SeatSelection />} />
          <Route path="/bookings" element={<MyBookings />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
