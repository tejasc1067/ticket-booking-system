import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { eventAPI } from '../services/api';

const CITIES = [
  'Pandharpur', 'Mumbai', 'Pune', 'Satara', 'Sangli',
  'Kolhapur', 'Nagpur', 'Latur', 'Nanded', 'Nashik'
];

export default function Home() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedCity, setSelectedCity] = useState('Pandharpur');
  const [search, setSearch] = useState('');

  useEffect(() => {
    fetchEventsByCity(selectedCity);
  }, [selectedCity]);

  const fetchEventsByCity = async (city) => {
    setLoading(true);
    try {
      const res = await eventAPI.getByCity(city);
      setEvents(res.data);
    } catch (err) {
      console.error('Failed to fetch events:', err);
      setEvents([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e) => {
    const keyword = e.target.value;
    setSearch(keyword);
    if (keyword.trim().length > 1) {
      try {
        const res = await eventAPI.search(keyword);
        // Filter by selected city
        setEvents(res.data.filter((ev) => ev.city === selectedCity));
      } catch (err) {
        console.error('Search failed:', err);
      }
    } else if (keyword.trim().length === 0) {
      fetchEventsByCity(selectedCity);
    }
  };

  const handleCityChange = (city) => {
    setSelectedCity(city);
    setSearch('');
  };

  return (
    <>
      <div className="hero">
        <h1 className="hero-title">
          Book Your <span className="gradient-text">Experience</span>
        </h1>
        <p className="hero-subtitle">
          Discover amazing events and book your seats instantly with real-time availability
        </p>
        <div style={{ maxWidth: '500px', margin: '0 auto' }}>
          <input
            type="text"
            className="form-input"
            placeholder="🔍  Search events, concerts, shows..."
            value={search}
            onChange={handleSearch}
            style={{ fontSize: '1.05rem', padding: '0.9rem 1.2rem', borderRadius: 'var(--radius-xl)' }}
          />
        </div>
      </div>

      <div className="container">
        {/* City Selector */}
        <div className="city-selector">
          <div className="city-selector-label">📍 Events near</div>
          <div className="city-pills">
            {CITIES.map((city) => (
              <button
                key={city}
                className={`city-pill ${selectedCity === city ? 'city-pill-active' : ''}`}
                onClick={() => handleCityChange(city)}
              >
                {city}
              </button>
            ))}
          </div>
        </div>

        {loading ? (
          <div className="loading"><div className="spinner"></div></div>
        ) : events.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">🎭</div>
            <div className="empty-state-title">No events in {selectedCity}</div>
            <p>Check back later or try another city</p>
          </div>
        ) : (
          <>
            <div className="page-header" style={{ marginTop: '0.5rem' }}>
              <p className="page-subtitle">{events.length} events in {selectedCity}</p>
            </div>
            <div className="grid grid-3">
              {events.map((event) => (
                <Link to={`/events/${event.id}`} key={event.id}>
                  <div className="card">
                    {event.imageUrl ? (
                      <img src={event.imageUrl} alt={event.name} className="card-image" />
                    ) : (
                      <div className="card-image" style={{
                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                        fontSize: '3rem', background: 'linear-gradient(135deg, var(--bg-secondary), var(--bg-card))'
                      }}>🎪</div>
                    )}
                    <h3 className="card-title">{event.name.replace(` — ${event.city}`, '')}</h3>
                    <p className="card-subtitle">📍 {event.venue}</p>
                    {event.description && (
                      <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.5rem' }}>
                        {event.description.length > 100
                          ? event.description.substring(0, 100) + '...'
                          : event.description}
                      </p>
                    )}
                  </div>
                </Link>
              ))}
            </div>
          </>
        )}
      </div>
    </>
  );
}
