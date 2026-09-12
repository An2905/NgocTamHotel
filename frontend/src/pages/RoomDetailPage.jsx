import { useState, useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useLanguage } from '../contexts/LanguageContext.jsx';
import { apiRequest } from '../api/client.js';
import Navbar from '../components/Navbar.jsx';
import Footer from '../components/Footer.jsx';

function formatPrice(price) {
  return Number(price).toLocaleString('vi-VN');
}

const GALLERY_IMAGES = {
  1: [
    'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&w=900&q=80',
    'https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=900&q=80',
    'https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=900&q=80',
  ],
  2: [
    'https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&w=900&q=80',
    'https://images.unsplash.com/photo-1595576508898-0ad5c879a061?auto=format&fit=crop&w=900&q=80',
    'https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=900&q=80',
  ],
  3: [
    'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=900&q=80',
    'https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=900&q=80',
    'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=900&q=80',
  ],
  4: [
    'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=900&q=80',
    'https://images.unsplash.com/photo-1598928506311-c55ez633a2?auto=format&fit=crop&w=900&q=80',
    'https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=900&q=80',
  ],
};

export default function RoomDetailPage() {
  const { id } = useParams();
  const { t } = useLanguage();
  const [room, setRoom] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeImg, setActiveImg] = useState(0);

  useEffect(() => {
    apiRequest(`/rooms/${id}`)
      .then(data => {
        setRoom(data);
        setActiveImg(0);
      })
      .catch(() => setRoom(null))
      .finally(() => setLoading(false));
  }, [id]);

  const gallery = GALLERY_IMAGES[id] || GALLERY_IMAGES[1];

  if (loading) return <div className="public-site"><Navbar /><main className="page-content"><div className="loading-state container"><p>{t('common_loading')}</p></div></main></div>;
  if (!room) return <div className="public-site"><Navbar /><main className="page-content"><div className="loading-state container"><p>{t('common_error')}</p></div></main></div>;

  return (
    <div className="public-site">
      <Navbar />

      <main className="page-content">
        <section className="room-detail container">
          <Link to="/rooms" className="back-link">{t('room_detail_back')}</Link>

          {/* Gallery */}
          <div className="room-gallery">
            <div className="gallery-main">
              <img src={gallery[activeImg]} alt={room.name} />
            </div>
            <div className="gallery-thumbs">
              {gallery.map((img, i) => (
                <button
                  key={i}
                  className={`thumb ${i === activeImg ? 'active' : ''}`}
                  onClick={() => setActiveImg(i)}
                >
                  <img src={img} alt={`${room.name} ${i + 1}`} />
                </button>
              ))}
            </div>
          </div>

          <div className="room-detail-grid">
            {/* Info */}
            <div className="room-detail-info">
              <h1>{room.name}</h1>
              <p className="room-detail-capacity">{t('rooms_capacity', { n: room.maxAdults })} · {room.viewType === 'sea_view' ? '🌊 Sea View' : 'Standard'}</p>

              <div className="detail-section">
                <h3>{t('room_detail_description')}</h3>
                <p>{room.description}</p>
              </div>

              <div className="detail-section">
                <h3>{t('room_detail_amenities')}</h3>
                <div className="amenities-grid">
                  {room.amenities?.map((amenity, i) => (
                    <span key={i} className="amenity-item">✓ {amenity}</span>
                  ))}
                </div>
              </div>
            </div>

            {/* Booking Sidebar */}
            <aside className="room-booking-card">
              <div className="booking-card-price">
                <p className="price-label">{t('room_detail_price')}</p>
                <p className="price-value">{formatPrice(room.basePrice)}<span>đ / {t('common_night')}</span></p>
              </div>
              <div className="booking-card-info">
                <div className="info-row">
                  <span>Sức chứa</span>
                  <strong>{room.maxAdults} người lớn, {room.maxChildren} trẻ em</strong>
                </div>
                <div className="info-row">
                  <span>Phòng trống</span>
                  <strong>{room.availableRooms}/{room.totalRooms}</strong>
                </div>
              </div>
              <Link to={`/booking?roomType=${room.id}`} className="btn-primary full">{t('room_detail_book')}</Link>
            </aside>
          </div>
        </section>
      </main>

      <Footer />
    </div>
  );
}
