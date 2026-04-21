import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { seatAPI, bookingAPI, showAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function SeatSelection() {
  const { showId } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const [show, setShow] = useState(null);
  const [seats, setSeats] = useState([]);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [booking, setBooking] = useState(false);
  const [locking, setLocking] = useState(false);
  const [locked, setLocked] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    fetchData();
  }, [showId]);

  const fetchData = async () => {
    try {
      const [showRes, seatsRes] = await Promise.all([
        showAPI.getById(showId),
        seatAPI.getByShow(showId),
      ]);
      setShow(showRes.data);
      setSeats(seatsRes.data);
    } catch (err) {
      console.error('Failed to fetch data:', err);
    } finally {
      setLoading(false);
    }
  };

  const toggleSeat = (seat) => {
    if (seat.status !== 'AVAILABLE' || locked) return;

    setSelectedSeats((prev) => {
      const exists = prev.find((s) => s.id === seat.id);
      if (exists) return prev.filter((s) => s.id !== seat.id);
      return [...prev, seat];
    });
  };

  const totalAmount = selectedSeats.reduce((sum, s) => sum + s.price, 0);

  const handleBookNow = async () => {
    // Redirect to login if not authenticated
    if (!isAuthenticated) {
      // Save intent so we can redirect back after login
      localStorage.setItem('bookingIntent', JSON.stringify({
        showId,
        seatIds: selectedSeats.map((s) => s.id),
      }));
      navigate('/login?redirect=' + encodeURIComponent(`/shows/${showId}/seats`));
      return;
    }

    // Step 1: Lock seats
    setError('');
    setLocking(true);
    try {
      await seatAPI.lock({ showId: parseInt(showId), seatIds: selectedSeats.map((s) => s.id) });
      setLocked(true);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to lock seats. Try different seats.');
      setLocking(false);
      return;
    }
    setLocking(false);

    // Step 2: Confirm booking
    setBooking(true);
    try {
      const res = await bookingAPI.create({
        showId: parseInt(showId),
        seatIds: selectedSeats.map((s) => s.id),
      });
      setSuccess(`Booking confirmed! Reference: ${res.data.bookingReference}`);
      localStorage.removeItem('bookingIntent');
      setTimeout(() => navigate('/bookings'), 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'Booking failed. Please try again.');
      setLocked(false);
    } finally {
      setBooking(false);
    }
  };

  // Group seats by row
  const seatsByRow = seats.reduce((acc, seat) => {
    if (!acc[seat.rowName]) acc[seat.rowName] = [];
    acc[seat.rowName].push(seat);
    return acc;
  }, {});

  if (loading) return <div className="loading"><div className="spinner"></div></div>;
  if (!show) return <div className="container"><div className="empty-state"><div className="empty-state-title">Show not found</div></div></div>;

  return (
    <div className="container">
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: '2rem' }}>
        {/* Seat Map */}
        <div>
          <div className="page-header">
            <h1 className="page-title" style={{ fontSize: '1.5rem' }}>{show.eventName}</h1>
            <p className="page-subtitle">
              {new Date(show.showTime).toLocaleDateString('en-IN', { weekday: 'long', day: 'numeric', month: 'long' })}
              {' • '}
              {new Date(show.showTime).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' })}
            </p>
          </div>

          <div className="card" style={{ padding: '2rem' }}>
            <div className="seat-map">
              <div className="screen-label">Screen</div>
              <div className="screen"></div>

              {Object.entries(seatsByRow)
                .sort(([a], [b]) => a.localeCompare(b))
                .map(([rowName, rowSeats]) => (
                  <div key={rowName} className="seat-row">
                    <div className="seat-row-label">{rowName}</div>
                    {rowSeats
                      .sort((a, b) => {
                        const numA = parseInt(a.seatNumber.replace(/\D/g, ''));
                        const numB = parseInt(b.seatNumber.replace(/\D/g, ''));
                        return numA - numB;
                      })
                      .map((seat) => {
                        const isSelected = selectedSeats.find((s) => s.id === seat.id);
                        let className = 'seat ';

                        if (seat.status === 'BOOKED') className += 'seat-booked';
                        else if (seat.status === 'LOCKED') className += 'seat-locked';
                        else if (isSelected) className += 'seat-selected';
                        else className += 'seat-available';

                        if (seat.seatType === 'VIP') className += ' seat-vip';
                        else if (seat.seatType === 'PREMIUM') className += ' seat-premium';

                        return (
                          <div
                            key={seat.id}
                            className={className}
                            onClick={() => toggleSeat(seat)}
                            title={`${seat.seatNumber} - ${seat.seatType} - ₹${seat.price}`}
                          >
                            {seat.seatNumber.replace(rowName, '')}
                          </div>
                        );
                      })}
                    <div className="seat-row-label">{rowName}</div>
                  </div>
                ))}

              <div className="seat-legend">
                <div className="seat-legend-item">
                  <div className="seat-legend-box" style={{ background: 'rgba(0,206,201,0.15)', border: '2px solid rgba(0,206,201,0.3)' }}></div>
                  Available
                </div>
                <div className="seat-legend-item">
                  <div className="seat-legend-box" style={{ background: 'var(--accent-primary)', border: '2px solid var(--accent-secondary)' }}></div>
                  Selected
                </div>
                <div className="seat-legend-item">
                  <div className="seat-legend-box" style={{ background: 'rgba(255,255,255,0.03)', border: '2px solid rgba(255,255,255,0.05)', opacity: 0.4 }}></div>
                  Booked
                </div>
                <div className="seat-legend-item">
                  <div className="seat-legend-box" style={{ background: 'rgba(253,203,110,0.15)', border: '2px solid rgba(253,203,110,0.4)' }}></div>
                  VIP
                </div>
                <div className="seat-legend-item">
                  <div className="seat-legend-box" style={{ background: 'rgba(108,92,231,0.15)', border: '2px solid rgba(108,92,231,0.4)' }}></div>
                  Premium
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Booking Summary */}
        <div>
          <div className="booking-summary">
            <h3 style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '1rem' }}>Booking Summary</h3>

            {error && <div className="alert alert-error">{error}</div>}
            {success && <div className="alert alert-success">{success}</div>}

            {selectedSeats.length === 0 ? (
              <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', textAlign: 'center', padding: '2rem 0' }}>
                Select seats to continue
              </p>
            ) : (
              <>
                <div style={{ marginBottom: '1rem' }}>
                  {selectedSeats.map((seat) => (
                    <div key={seat.id} className="summary-row">
                      <span>
                        {seat.seatNumber}
                        <span className={`badge badge-${seat.seatType.toLowerCase()}`} style={{ marginLeft: '0.5rem' }}>
                          {seat.seatType}
                        </span>
                      </span>
                      <span className="price">₹{seat.price.toLocaleString()}</span>
                    </div>
                  ))}
                </div>

                <div className="summary-row total">
                  <span>Total ({selectedSeats.length} seat{selectedSeats.length > 1 ? 's' : ''})</span>
                  <span className="price price-large">₹{totalAmount.toLocaleString()}</span>
                </div>

                <div style={{ marginTop: '1.5rem' }}>
                  <button
                    className="btn btn-primary btn-lg"
                    style={{ width: '100%' }}
                    onClick={handleBookNow}
                    disabled={locking || booking}
                  >
                    {locking ? '🔒 Locking seats...' : booking ? 'Confirming...' : isAuthenticated ? '✅ Book Now' : '🔐 Login & Book'}
                  </button>
                  {!isAuthenticated && (
                    <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textAlign: 'center', marginTop: '0.5rem' }}>
                      You'll be redirected to login
                    </p>
                  )}
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
