import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useLanguage } from '../contexts/LanguageContext.jsx';
import Navbar from '../components/Navbar.jsx';
import Footer from '../components/Footer.jsx';

const ROOM_IMAGES = [
  { id: 1, name: 'Phòng Đơn', price: '350.000', img: 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&w=900&q=80', guests: 1 },
  { id: 2, name: 'Phòng Đôi Thường', price: '550.000', img: 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&w=900&q=80', guests: 2 },
  { id: 3, name: 'Phòng Đôi View Biển', price: '600.000', img: 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=900&q=80', guests: 2 },
  { id: 4, name: 'Phòng Ba', price: '750.000', img: 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=900&q=80', guests: 3 },
];

export default function HomePage() {
  const { t } = useLanguage();
  const navigate = useNavigate();
  const today = new Date().toISOString().slice(0, 10);
  const [bookingForm, setBookingForm] = useState({ checkin: '', checkout: '', guests: '2' });

  function handleBookingSearch(e) {
    e.preventDefault();
    navigate(`/booking?checkin=${bookingForm.checkin}&checkout=${bookingForm.checkout}&guests=${bookingForm.guests}`);
  }

  return (
    <div className="public-site">
      <Navbar />

      <main>
        {/* Hero Banner */}
        <section className="hero-section" id="hero">
          <div className="hero-overlay"></div>
          <div className="hero-content">
            <p className="kicker">{t('hero_kicker')}</p>
            <h1>{t('hero_title')}</h1>
            <p className="hero-desc">{t('hero_subtitle')}</p>
            <Link className="btn-primary" to="/booking">{t('hero_cta')}</Link>
          </div>
          <div className="hero-badge">
            <span>{t('hero_note_number')}</span>
            <p>{t('hero_note_text')}</p>
          </div>
        </section>

        {/* Quick Booking Bar */}
        <section className="booking-bar-section" id="booking-bar">
          <form className="booking-bar" onSubmit={handleBookingSearch}>
            <label>
              <span>{t('booking_checkin')}</span>
              <input type="date" min={today} value={bookingForm.checkin}
                onChange={e => setBookingForm({ ...bookingForm, checkin: e.target.value })} required />
            </label>
            <label>
              <span>{t('booking_checkout')}</span>
              <input type="date" min={bookingForm.checkin || today} value={bookingForm.checkout}
                onChange={e => setBookingForm({ ...bookingForm, checkout: e.target.value })} required />
            </label>
            <label>
              <span>{t('booking_guests')}</span>
              <select value={bookingForm.guests} onChange={e => setBookingForm({ ...bookingForm, guests: e.target.value })}>
                <option value="1">1 {t('booking_guests').toLowerCase()}</option>
                <option value="2">2 {t('booking_guests').toLowerCase()}</option>
                <option value="3">3 {t('booking_guests').toLowerCase()}</option>
                <option value="4">4 {t('booking_guests').toLowerCase()}</option>
              </select>
            </label>
            <button type="submit">{t('booking_search')}</button>
          </form>
        </section>

        {/* About Section */}
        <section className="about-section container" id="about">
          <div className="about-left">
            <p className="kicker accent">{t('about_kicker')}</p>
            <h2>{t('about_title')}</h2>
          </div>
          <div className="about-right">
            <p>{t('about_desc')}</p>
            <div className="about-stats">
              <div className="stat"><strong>32</strong><span>{t('about_rooms')}</span></div>
              <div className="stat"><strong>4.8/5</strong><span>{t('about_rating')}</span></div>
              <div className="stat"><strong>24/7</strong><span>{t('about_support')}</span></div>
            </div>
          </div>
        </section>

        {/* Featured Rooms */}
        <section className="rooms-section" id="rooms">
          <div className="container">
            <div className="section-header">
              <div>
                <p className="kicker accent">{t('rooms_kicker')}</p>
                <h2>{t('rooms_title')}</h2>
              </div>
              <p className="section-desc">{t('rooms_subtitle')}</p>
            </div>
            <div className="room-grid">
              {ROOM_IMAGES.map(room => (
                <article className="room-card" key={room.id}>
                  <div className="room-card-img" style={{ backgroundImage: `url(${room.img})` }}>
                    <span className="room-badge">{t('rooms_capacity', { n: room.guests })}</span>
                  </div>
                  <div className="room-card-body">
                    <h3>{room.name}</h3>
                    <div className="room-card-footer">
                      <p className="room-price">{t('rooms_from')} <strong>{room.price}đ</strong> {t('rooms_per_night')}</p>
                      <div className="room-card-actions">
                        <Link to={`/rooms/${room.id}`} className="btn-outline">{t('rooms_view_detail')}</Link>
                        <Link to={`/booking?roomType=${room.id}`} className="btn-primary small">{t('rooms_book_now')}</Link>
                      </div>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          </div>
        </section>

        {/* Services Preview */}
        <section className="services-preview container" id="services-preview">
          <div className="services-header">
            <p className="kicker accent">{t('services_kicker')}</p>
            <h2>{t('services_title')}</h2>
          </div>
          <div className="service-grid">
            {[
              { num: '01', key: 'motorbike', icon: '🏍️' },
              { num: '02', key: 'electric_cart', icon: '🚗' },
              { num: '03', key: 'camping', icon: '🔥' },
              { num: '04', key: 'party', icon: '🎉' },
              { num: '05', key: 'food', icon: '🍽️' },
            ].map(svc => (
              <article className="service-card" key={svc.num}>
                <span className="service-icon">{svc.icon}</span>
                <span className="service-num">{svc.num}</span>
                <h3>{t(`service_${svc.key}`)}</h3>
                <p>{t(`service_${svc.key}_desc`)}</p>
              </article>
            ))}
          </div>
          <div className="services-cta">
            <Link to="/services" className="btn-outline">{t('nav_services')}</Link>
          </div>
        </section>

        {/* Customer Reviews */}
        <section className="reviews-section" id="reviews">
          <div className="container">
            <p className="kicker">{t('reviews_kicker')}</p>
            <h2>{t('reviews_title')}</h2>
            <div className="reviews-grid">
              {[1, 2, 3].map(i => (
                <article className="review-card" key={i}>
                  <div className="review-stars">★★★★★</div>
                  <p className="review-text">"{t(`review_${i}_text`)}"</p>
                  <div className="review-author">
                    <div className="review-avatar">{t(`review_${i}_name`).charAt(0)}</div>
                    <strong>{t(`review_${i}_name`)}</strong>
                  </div>
                </article>
              ))}
            </div>
          </div>
        </section>

        {/* CTA Banner */}
        <section className="cta-banner" id="cta">
          <p className="kicker">{t('contact_cta')}</p>
          <h2>{t('contact_cta_title')}</h2>
          <p>{t('contact_subtitle')}</p>
          <Link className="btn-primary" to="/booking">{t('contact_cta_button')}</Link>
        </section>
      </main>

      <Footer />
    </div>
  );
}
