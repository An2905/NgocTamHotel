import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useLanguage } from '../contexts/LanguageContext.jsx';
import { apiRequest } from '../api/client.js';
import Navbar from '../components/Navbar.jsx';
import Footer from '../components/Footer.jsx';

function formatPrice(price) {
  return Number(price).toLocaleString('vi-VN');
}

export default function RoomsPage() {
  const { t } = useLanguage();
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiRequest('/rooms')
      .then(setRooms)
      .catch(() => setRooms([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="public-site">
      <Navbar />

      <main className="page-content">
        <section className="page-hero rooms-hero">
          <div className="page-hero-overlay"></div>
          <div className="page-hero-content">
            <p className="kicker">{t('rooms_kicker')}</p>
            <h1>{t('rooms_all_types')}</h1>
            <p>{t('rooms_subtitle')}</p>
          </div>
        </section>

        <section className="rooms-list container">
          {loading ? (
            <div className="loading-state"><p>{t('common_loading')}</p></div>
          ) : (
            <div className="room-grid">
              {rooms.map(room => (
                <article className="room-card" key={room.id}>
                  <div className="room-card-img" style={{
                    backgroundImage: `url(${room.imageUrl || 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&w=900&q=80'})`
                  }}>
                    <span className="room-badge">{t('rooms_capacity', { n: room.maxAdults })}</span>
                    {room.viewType === 'sea_view' && <span className="room-badge sea">🌊 Sea View</span>}
                  </div>
                  <div className="room-card-body">
                    <h3>{room.name}</h3>
                    <p className="room-desc">{room.description?.substring(0, 100)}...</p>
                    <div className="room-amenities-preview">
                      {room.amenities?.slice(0, 4).map((a, i) => (
                        <span key={i} className="amenity-tag">{a}</span>
                      ))}
                    </div>
                    <div className="room-card-footer">
                      <p className="room-price">
                        {t('rooms_from')} <strong>{formatPrice(room.basePrice)}đ</strong> {t('rooms_per_night')}
                      </p>
                      <div className="room-card-actions">
                        <Link to={`/rooms/${room.id}`} className="btn-outline">{t('rooms_view_detail')}</Link>
                        <Link to={`/booking?roomType=${room.id}`} className="btn-primary small">{t('rooms_book_now')}</Link>
                      </div>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
      </main>

      <Footer />
    </div>
  );
}
