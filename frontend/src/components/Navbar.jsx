import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const isActive = (path) => location.pathname === path ? 'nav-link active' : 'nav-link';

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">
        TicketBook<span>.io</span>
      </Link>

      <div className="navbar-links">
        <Link to="/" className={isActive('/')}>Events</Link>

        {isAuthenticated && (
          <Link to="/bookings" className={isActive('/bookings')}>My Bookings</Link>
        )}

        {isAuthenticated ? (
          <>
            <div className="nav-user">
              <div>
                <div className="nav-user-name">{user.fullName}</div>
                <div className="nav-user-role">{user.role}</div>
              </div>
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
