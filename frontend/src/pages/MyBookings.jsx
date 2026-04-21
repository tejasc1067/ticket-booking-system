import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { bookingAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function MyBookings() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    fetchBookings();
  }, [isAuthenticated]);

  const fetchBookings = async () => {
    try {
      const res = await bookingAPI.getMy();
      setBookings(res.data);
    } catch (err) {
      console.error('Failed to fetch bookings:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (bookingId) => {
    if (!window.confirm('Are you sure you want to cancel this booking?')) return;

    try {
      await bookingAPI.cancel(bookingId);
      fetchBookings();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to cancel booking');
    }
  };

  const getStatusBadge = (status) => {
    const map = {
      CONFIRMED: 'badge-success',
      PENDING: 'badge-warning',
      CANCELLED: 'badge-danger',
      EXPIRED: 'badge-danger',
    };
    return `badge ${map[status] || 'badge-regular'}`;
  };

  if (loading) return <div className="loading"><div className="spinner"></div></div>;

  return (
    <div className="container">
      <div className="page-header">
        <h1 className="page-title">My Bookings</h1>
        <p className="page-subtitle">View and manage your ticket bookings</p>
      </div>

      {bookings.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">🎫</div>
          <div className="empty-state-title">No bookings yet</div>
          <p>Browse events and book your first tickets!</p>
          <button className="btn btn-primary" style={{ marginTop: '1rem' }} onClick={() => navigate('/')}>
            Browse Events
          </button>
        </div>
      ) : (
        <div className="grid grid-2">
          {bookings.map((b) => (
            <div key={b.id} className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                <div>
                  <h3 className="card-title">{b.eventName}</h3>
                  <p className="card-subtitle">
                    {new Date(b.showTime).toLocaleDateString('en-IN', { weekday: 'short', day: 'numeric', month: 'short' })}
                    {' • '}
                    {new Date(b.showTime).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' })}
                  </p>
                </div>
                <span className={getStatusBadge(b.status)}>{b.status}</span>
              </div>

              <div style={{ background: 'var(--bg-secondary)', borderRadius: 'var(--radius-sm)', padding: '0.75rem', marginBottom: '1rem' }}>
                <div className="summary-row" style={{ padding: '0.25rem 0' }}>
                  <span className="summary-label">Reference</span>
                  <span style={{ fontWeight: '700', fontFamily: 'monospace', color: 'var(--accent-secondary)' }}>{b.bookingReference}</span>
                </div>
                <div className="summary-row" style={{ padding: '0.25rem 0' }}>
                  <span className="summary-label">Seats</span>
                  <span>{b.seats.map((s) => s.seatNumber).join(', ')}</span>
                </div>
                <div className="summary-row" style={{ padding: '0.25rem 0' }}>
                  <span className="summary-label">Total</span>
                  <span className="price">₹{b.totalAmount.toLocaleString()}</span>
                </div>
              </div>

              {b.status === 'CONFIRMED' && (
                <button className="btn btn-danger btn-sm" onClick={() => handleCancel(b.id)}>
                  Cancel Booking
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
