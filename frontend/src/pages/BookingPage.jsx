import { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useLanguage } from '../contexts/LanguageContext.jsx';
import { apiRequest } from '../api/client.js';
import Navbar from '../components/Navbar.jsx';
import Footer from '../components/Footer.jsx';

function formatPrice(price) {
  return Number(price).toLocaleString('vi-VN');
}

export default function BookingPage() {
  const { t } = useLanguage();
  const [searchParams] = useSearchParams();
  const [roomTypes, setRoomTypes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(null);
  const today = new Date().toISOString().slice(0, 10);

  const [form, setForm] = useState({
    roomTypeId: searchParams.get('roomType') || '',
    checkInDate: searchParams.get('checkin') || '',
    checkOutDate: searchParams.get('checkout') || '',
    adults: parseInt(searchParams.get('guests') || '1'),
    children: 0,
    fullName: '',
    phone: '',
    email: '',
    identityNumber: '',
    notes: '',
  });

  useEffect(() => {
    apiRequest('/rooms').then(setRoomTypes).catch(() => {});
  }, []);

  function updateField(field, value) {
    setForm(prev => ({ ...prev, [field]: value }));
  }

  function calculateNights() {
    if (!form.checkInDate || !form.checkOutDate) return 0;
    const diff = new Date(form.checkOutDate) - new Date(form.checkInDate);
    return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)));
  }

  function getSelectedRoom() {
    return roomTypes.find(r => String(r.id) === String(form.roomTypeId));
  }

  function calculateTotal() {
    const room = getSelectedRoom();
    if (!room) return 0;
    return Number(room.basePrice) * calculateNights();
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');

    if (!form.fullName || !form.phone || !form.roomTypeId || !form.checkInDate || !form.checkOutDate) {
      setError(t('booking_error_required'));
      return;
    }
    if (form.checkOutDate <= form.checkInDate) {
      setError(t('booking_error_checkout'));
      return;
    }

    setLoading(true);
    try {
      const result = await apiRequest('/bookings', {
        method: 'POST',
        body: JSON.stringify({
          ...form,
          roomTypeId: parseInt(form.roomTypeId),
          adults: parseInt(form.adults),
          children: parseInt(form.children),
        }),
      });
      setSuccess(result);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  if (success) {
    return (
      <div className="public-site">
        <Navbar />
        <main className="page-content">
          <div className="booking-success container">
            <div className="success-card">
              <div className="success-icon">✓</div>
              <h1>{t('booking_success_title')}</h1>
              <p>{t('booking_success_msg')}</p>
              <p className="booking-code">{success.bookingCode}</p>
              <p className="success-note">{t('booking_success_note')}</p>
              <div className="success-actions">
                <Link to={`/payment/${success.id}`} className="btn-primary">{t('booking_pay_now')}</Link>
                <Link to="/" className="btn-outline">{t('booking_back_home')}</Link>
              </div>
            </div>
          </div>
        </main>
        <Footer />
      </div>
    );
  }

  const nights = calculateNights();
  const total = calculateTotal();
  const selectedRoom = getSelectedRoom();

  return (
    <div className="public-site">
      <Navbar />

      <main className="page-content">
        <section className="page-hero booking-hero">
          <div className="page-hero-overlay"></div>
          <div className="page-hero-content">
            <p className="kicker">{t('booking_page_title').toUpperCase()}</p>
            <h1>{t('booking_page_title')}</h1>
            <p>{t('booking_page_subtitle')}</p>
          </div>
        </section>

        <section className="booking-page container">
          <form className="booking-form-full" onSubmit={handleSubmit}>
            <div className="booking-form-left">
              {/* Booking Info */}
              <div className="form-section">
                <h2>{t('booking_info_title')}</h2>
                <div className="form-grid">
                  <label>
                    <span>{t('booking_room_type')} *</span>
                    <select value={form.roomTypeId} onChange={e => updateField('roomTypeId', e.target.value)} required>
                      <option value="">{t('booking_select_room')}</option>
                      {roomTypes.map(rt => (
                        <option key={rt.id} value={rt.id}>{rt.name} - {formatPrice(rt.basePrice)}đ/{t('common_night')}</option>
                      ))}
                    </select>
                  </label>
                  <label>
                    <span>{t('booking_checkin')} *</span>
                    <input type="date" min={today} value={form.checkInDate}
                      onChange={e => updateField('checkInDate', e.target.value)} required />
                  </label>
                  <label>
                    <span>{t('booking_checkout')} *</span>
                    <input type="date" min={form.checkInDate || today} value={form.checkOutDate}
                      onChange={e => updateField('checkOutDate', e.target.value)} required />
                  </label>
                  <label>
                    <span>{t('booking_adults')}</span>
                    <select value={form.adults} onChange={e => updateField('adults', e.target.value)}>
                      {[1,2,3,4].map(n => <option key={n} value={n}>{n}</option>)}
                    </select>
                  </label>
                  <label>
                    <span>{t('booking_children')}</span>
                    <select value={form.children} onChange={e => updateField('children', e.target.value)}>
                      {[0,1,2,3].map(n => <option key={n} value={n}>{n}</option>)}
                    </select>
                  </label>
                </div>
              </div>

              {/* Guest Info */}
              <div className="form-section">
                <h2>{t('booking_guest_info')}</h2>
                <div className="form-grid">
                  <label className="full-width">
                    <span>{t('booking_fullname')} *</span>
                    <input type="text" value={form.fullName} onChange={e => updateField('fullName', e.target.value)} required />
                  </label>
                  <label>
                    <span>{t('booking_phone')} *</span>
                    <input type="tel" value={form.phone} onChange={e => updateField('phone', e.target.value)} required />
                  </label>
                  <label>
                    <span>{t('booking_email')}</span>
                    <input type="email" value={form.email} onChange={e => updateField('email', e.target.value)} />
                  </label>
                  <label>
                    <span>{t('booking_id_number')}</span>
                    <input type="text" value={form.identityNumber} onChange={e => updateField('identityNumber', e.target.value)} />
                  </label>
                  <label className="full-width">
                    <span>{t('booking_notes')}</span>
                    <textarea rows="3" value={form.notes} onChange={e => updateField('notes', e.target.value)}></textarea>
                  </label>
                </div>
              </div>
            </div>

            {/* Summary Sidebar */}
            <aside className="booking-summary">
              <h3>{t('booking_summary')}</h3>
              {selectedRoom && (
                <>
                  <div className="summary-room">
                    <img src={selectedRoom.imageUrl} alt={selectedRoom.name} />
                    <div>
                      <strong>{selectedRoom.name}</strong>
                      <p>{formatPrice(selectedRoom.basePrice)}đ / {t('common_night')}</p>
                    </div>
                  </div>
                  {nights > 0 && (
                    <div className="summary-details">
                      <div className="summary-row">
                        <span>{t('booking_nights', { n: nights })}</span>
                        <span>{formatPrice(total)}đ</span>
                      </div>
                      <div className="summary-total">
                        <span>{t('booking_total')}</span>
                        <strong>{formatPrice(total)}đ</strong>
                      </div>
                    </div>
                  )}
                </>
              )}

              {error && <p className="form-error">{error}</p>}

              <button type="submit" className="btn-primary full" disabled={loading}>
                {loading ? t('booking_confirming') : t('booking_confirm')}
              </button>
            </aside>
          </form>
        </section>
      </main>

      <Footer />
    </div>
  );
}
