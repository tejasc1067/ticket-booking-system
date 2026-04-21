import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { eventAPI, showAPI } from '../services/api';

export default function EventDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [event, setEvent] = useState(null);
  const [shows, setShows] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [eventRes, showsRes] = await Promise.all([
          eventAPI.getById(id),
          showAPI.getUpcoming(id),
        ]);
        setEvent(eventRes.data);
        setShows(showsRes.data);
      } catch (err) {
        console.error('Failed to fetch event:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id]);

  if (loading) return <div className="loading"><div className="spinner"></div></div>;
  if (!event) return <div className="container"><div className="empty-state"><div className="empty-state-title">Event not found</div></div></div>;

  const formatDate = (dateStr) => {
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-IN', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' });
  };

  const formatTime = (dateStr) => {
    const d = new Date(dateStr);
    return d.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <div className="container">
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', marginBottom: '2rem' }}>
        <div>
          {event.imageUrl ? (
            <img src={event.imageUrl} alt={event.name} style={{
              width: '100%', height: '350px', objectFit: 'cover',
              borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)'
            }} />
          ) : (
            <div style={{
              width: '100%', height: '350px', borderRadius: 'var(--radius-lg)',
              background: 'linear-gradient(135deg, var(--bg-secondary), var(--bg-card))',
              display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '5rem',
              border: '1px solid var(--border)'
            }}>🎪</div>
          )}
        </div>

        <div>
          <h1 className="page-title">{event.name}</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '1.1rem', marginBottom: '1rem' }}>
            📍 {event.venue}, {event.city}
          </p>
          {event.description && (
            <p style={{ color: 'var(--text-muted)', lineHeight: '1.8', marginBottom: '1.5rem' }}>
              {event.description}
            </p>
          )}
          <div style={{ display: 'flex', gap: '1rem' }}>
            <span className="badge badge-success" style={{ fontSize: '0.8rem', padding: '0.4rem 0.8rem' }}>
              {shows.length} upcoming show{shows.length !== 1 ? 's' : ''}
            </span>
          </div>
        </div>
      </div>

      <div className="page-header">
        <h2 className="page-title" style={{ fontSize: '1.5rem' }}>Select a Show</h2>
        <p className="page-subtitle">Choose a showtime to book your seats</p>
      </div>

      {shows.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">📅</div>
          <div className="empty-state-title">No upcoming shows</div>
          <p>Check back later for new showtimes</p>
        </div>
      ) : (
        <div className="grid grid-2">
          {shows.map((show) => (
            <div key={show.id} className="card" style={{ cursor: 'pointer' }}
              onClick={() => navigate(`/shows/${show.id}/seats`)}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div>
                  <div style={{ fontSize: '1.1rem', fontWeight: '700', marginBottom: '0.25rem' }}>
                    {formatDate(show.showTime)}
                  </div>
                  <div style={{ fontSize: '1.3rem', fontWeight: '800', color: 'var(--accent-secondary)' }}>
                    {formatTime(show.showTime)}
                  </div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <div className="price price-large">₹{show.basePrice.toLocaleString()}</div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>onwards</div>
                </div>
              </div>
              <div style={{ marginTop: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                  {show.availableSeats}/{show.totalSeats} seats available
                </div>
                <div style={{
                  width: '100px', height: '4px', borderRadius: '2px', background: 'var(--bg-secondary)', overflow: 'hidden'
                }}>
                  <div style={{
                    width: `${(show.availableSeats / show.totalSeats) * 100}%`,
                    height: '100%', borderRadius: '2px',
                    background: show.availableSeats < show.totalSeats * 0.2 ? 'var(--danger)' : 'var(--success)'
                  }}></div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
