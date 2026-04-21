import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useState, useRef, useEffect } from 'react';

const CITIES = [
  'Pandharpur', 'Mumbai', 'Pune', 'Satara', 'Sangli',
  'Kolhapur', 'Nagpur', 'Latur', 'Nanded', 'Nashik'
];

export default function Navbar({ selectedCity, onCityChange }) {
  const { user, isAuthenticated, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [cityOpen, setCityOpen] = useState(false);
  const dropdownRef = useRef(null);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const isActive = (path) => location.pathname === path ? 'nav-link active' : 'nav-link';
  const firstName = user?.fullName?.split(' ')[0] || '';

  // Close dropdown on outside click
  useEffect(() => {
    const handleClick = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setCityOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, []);

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">
        TicketBook<span>.io</span>
      </Link>

      <div className="navbar-links">
        <Link to="/" className={isActive('/')}>Events</Link>

        {/* City Dropdown */}
        <div className="city-dropdown" ref={dropdownRef}>
          <button
            className="city-dropdown-btn"
            onClick={() => setCityOpen(!cityOpen)}
          >
            📍 {selectedCity}
            <span className="city-dropdown-arrow">{cityOpen ? '▲' : '▼'}</span>
          </button>
          {cityOpen && (
            <div className="city-dropdown-menu">
              {CITIES.map((city) => (
                <button
                  key={city}
                  className={`city-dropdown-item ${city === selectedCity ? 'city-dropdown-item-active' : ''}`}
                  onClick={() => {
                    onCityChange(city);
                    setCityOpen(false);
                  }}
                >
                  {city}
                </button>
              ))}
            </div>
          )}
        </div>

        {isAuthenticated && (
          <Link to="/bookings" className={isActive('/bookings')}>My Bookings</Link>
        )}

        {isAuthenticated ? (
          <>
            <div className="nav-user">
              <div className="nav-user-name">{firstName}</div>
            </div>
            <button className="nav-link" onClick={handleLogout}>Logout</button>
          </>
        ) : (
          <>
            <Link to="/login" className={isActive('/login')}>Login</Link>
            <Link to="/register">
              <button className="btn btn-primary btn-sm">Sign Up</button>
            </Link>
          </>
        )}
      </div>
    </nav>
  );
}
