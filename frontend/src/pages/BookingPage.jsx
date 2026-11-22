import { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useLanguage } from '../contexts/LanguageContext.jsx';
import { roomRequest } from '../api/client.js';
import { normalizeRoomCategory } from '../api/roomCategories.js';
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
  const [availability, setAvailability] = useState(null);
  const [checking, setChecking] = useState(false);
  const [hold, setHold] = useState(null);
  const [ownerKey] = useState(() => crypto.randomUUID());
  const [secondsLeft, setSecondsLeft] = useState(0);
  const today = new Date().toISOString().slice(0, 10);

  const [form, setForm] = useState({
    roomTypeId: searchParams.get('roomType') || '',
    checkInDate: searchParams.get('checkin') || '',
    checkOutDate: searchParams.get('checkout') || '',
    adults: parseInt(searchParams.get('guests') || '1'),
    children: 0,
    numberOfRooms: 1,
    fullName: '',
    phone: '',
    email: '',
    dateOfBirth: '',
    notes: '',
  });

  useEffect(() => {
    roomRequest('/rooms')
      .then(data => setRoomTypes(data.map(normalizeRoomCategory)))
      .catch(() => setError(t('booking_rooms_unavailable')));
  }, [t]);

  useEffect(() => {
    if (!hold) return undefined;
    const tick = () => setSecondsLeft(Math.max(0, Math.ceil((hold.expiresAtEpochMillis - Date.now()) / 1000)));
    tick();
    const timer = window.setInterval(tick, 1000);
    return () => window.clearInterval(timer);
  }, [hold]);

  async function releaseHold() {
    if (!hold) return;
    const id = hold.holdId;
    setHold(null);
    setAvailability(null);
    try { await roomRequest(`/room-holds/${id}?ownerKey=${ownerKey}`, { method: 'DELETE' }); }
    catch { /* Server expiry also releases a hold when the client disconnects. */ }
  }

  function updateField(field, value) {
    if (hold && ['roomTypeId', 'checkInDate', 'checkOutDate', 'numberOfRooms'].includes(field)) {
      void releaseHold();
    }
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
    return Number(room.basePrice) * calculateNights() * Number(form.numberOfRooms);
  }

  const availabilityKey = [form.roomTypeId, form.checkInDate, form.checkOutDate].join('|');
  const currentAvailability = availability?.key === availabilityKey ? availability.count : null;

  async function checkAvailability() {
    if (hold) await releaseHold();
    setError('');
    setAvailability(null);
    const room = getSelectedRoom();
    if (!room || !form.checkInDate || !form.checkOutDate) {
      setError(t('booking_error_required'));
      return;
    }
    if (form.checkOutDate <= form.checkInDate) {
      setError(t('booking_error_checkout'));
      return;
    }
    if (!room.roomType) {
      setError(t('booking_error_room_type'));
      return;
    }

    setChecking(true);
    try {
      const params = new URLSearchParams({
        checkInDay: form.checkInDate,
        checkOutDay: form.checkOutDate,
        roomType: room.roomType,
        roomCateId: room.id,
      });
      const count = await roomRequest(`/check-rooms-booking?${params}`);
      if (!Number.isInteger(count) || count < 0) throw new Error(t('booking_check_error'));
      setAvailability({ key: availabilityKey, count });
      updateField('numberOfRooms', 1);
    } catch (err) {
      setError(err.message || t('booking_check_error'));
    } finally {
      setChecking(false);
    }
  }

  async function handleHold() {
    const room = getSelectedRoom();
    if (!room || currentAvailability === null || Number(form.numberOfRooms) > currentAvailability) return;
    setChecking(true);
    setError('');
    try {
      const result = await roomRequest('/room-holds', {
        method: 'POST',
        body: JSON.stringify({ categoryId: room.id, ownerKey, checkInDate: form.checkInDate,
          checkOutDate: form.checkOutDate, numberOfRooms: Number(form.numberOfRooms) }),
      });
      setHold(result);
    } catch (err) {
      setAvailability(null);
      setError(err.message || t('booking_check_error'));
    } finally { setChecking(false); }
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    if (!hold || secondsLeft <= 0) {
      setHold(null);
      setAvailability(null);
      setError(t('booking_hold_expired'));
      return;
    }

    if (!form.fullName || !form.phone || !form.roomTypeId || !form.checkInDate || !form.checkOutDate) {
      setError(t('booking_error_required'));
      return;
    }
    if (form.checkOutDate <= form.checkInDate) {
      setError(t('booking_error_checkout'));
      return;
    }
    const room = getSelectedRoom();
    if (currentAvailability === null || currentAvailability < 1 || Number(form.numberOfRooms) > currentAvailability) {
      setError(t('booking_check_first'));
      return;
    }
    if (room && Number(form.adults) + Number(form.children) > room.maxAdults * Number(form.numberOfRooms)) {
      setError(t('booking_error_capacity'));
      return;
    }

    setLoading(true);
    try {
      const result = await roomRequest('/new-booking', {
        method: 'POST',
        body: JSON.stringify({
          categoryId: room.id,
          holdId: hold.holdId,
          ownerKey,
          checkInDate: form.checkInDate,
          checkOutDate: form.checkOutDate,
          adults: Number(form.adults),
          children: Number(form.children),
          note: form.notes,
          numberOfRooms: Number(form.numberOfRooms),
          fullName: form.fullName.trim(),
          phone: form.phone.trim(),
          email: form.email.trim() || null,
          dateOfBirth: form.dateOfBirth || null,
        }),
      });
      if (!result?.roomBookingId) throw new Error(t('booking_create_error'));
      setSuccess(result);
    } catch (err) {
      setError(err.message);
      if (/giữ phòng|hold/i.test(err.message || '')) { setHold(null); setAvailability(null); }
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
              <p className="booking-code">{success.roomBookingId}</p>
              <p>{getSelectedRoom()?.name} · {success.numberOfRooms} {t('booking_room_unit')} · {formatPrice(success.totalPrice)}đ</p>
              <p className="success-note">{t('booking_success_note')}</p>
              {success.paymentDeadlineEpochMillis && <p>{t('booking_payment_deadline', { time: new Date(success.paymentDeadlineEpochMillis).toLocaleString() })}</p>}
              <div className="success-actions">
                <Link to={`/payment/${success.roomBookingId}`} className="btn-primary">{t('booking_pay_now')}</Link>
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
                    <select value={form.roomTypeId} onChange={e => updateField('roomTypeId', e.target.value)} disabled={!!hold} required>
                      <option value="">{t('booking_select_room')}</option>
                      {roomTypes.map(rt => (
                        <option key={rt.id} value={rt.id}>{rt.name} - {formatPrice(rt.basePrice)}đ/{t('common_night')}</option>
                      ))}
                    </select>
                  </label>
                  <label>
                    <span>{t('booking_checkin')} *</span>
                    <input type="date" min={today} value={form.checkInDate} disabled={!!hold}
                      onChange={e => updateField('checkInDate', e.target.value)} required />
                  </label>
                  <label>
                    <span>{t('booking_checkout')} *</span>
                    <input type="date" min={form.checkInDate || today} value={form.checkOutDate} disabled={!!hold}
                      onChange={e => updateField('checkOutDate', e.target.value)} required />
                  </label>
                  <label>
                    <span>{t('booking_adults')}</span>
                    <input type="number" min="1" step="1" inputMode="numeric" value={form.adults}
                      onChange={e => updateField('adults', e.target.value)} required />
                  </label>
                  <label>
                    <span>{t('booking_children')}</span>
                    <input type="number" min="0" step="1" inputMode="numeric" value={form.children}
                      onChange={e => updateField('children', e.target.value)} required />
                  </label>
                </div>
                <div className="availability-check">
                  <button type="button" className="btn-outline" onClick={checkAvailability} disabled={checking || roomTypes.length === 0 || !!hold}>
                    {checking ? t('booking_checking') : t('booking_check_availability')}
                  </button>
                  {currentAvailability === 0 && <p className="form-error">{t('booking_no_available_rooms')}</p>}
                  {currentAvailability > 0 && <p className="form-success">{t('booking_available_count', { n: currentAvailability })}</p>}
                </div>
                {currentAvailability > 0 && (
                  <div className="form-grid booking-room-count">
                    <label>
                      <span>{t('booking_number_of_rooms')}</span>
                      <select value={form.numberOfRooms} onChange={e => updateField('numberOfRooms', Number(e.target.value))} disabled={!!hold}>
                        {Array.from({ length: currentAvailability }, (_, index) => index + 1).map(count => (
                          <option key={count} value={count}>{count}</option>
                        ))}
                      </select>
                    </label>
                  </div>
                )}
              </div>

              {hold && <div className="form-section">
                <p className="form-success">{t('booking_hold_notice', { minutes: Math.floor(secondsLeft / 60), seconds: String(secondsLeft % 60).padStart(2, '0') })}</p>
                <button type="button" className="btn-outline" onClick={releaseHold}>{t('booking_change_selection')}</button>
              </div>}

              {/* Guest Info */}
              {hold && secondsLeft > 0 && (
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
                    <span>{t('booking_date_of_birth')}</span>
                    <input type="date" max={today} value={form.dateOfBirth} onChange={e => updateField('dateOfBirth', e.target.value)} />
                  </label>
                  <label className="full-width">
                    <span>{t('booking_notes')}</span>
                    <textarea rows="3" value={form.notes} onChange={e => updateField('notes', e.target.value)}></textarea>
                  </label>
                </div>
              </div>
              )}
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
                      <div className="summary-row">
                        <span>{t('booking_number_of_rooms')}</span>
                        <span>{form.numberOfRooms}</span>
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

              {hold ? <button type="submit" className="btn-primary full" disabled={loading || secondsLeft <= 0}>
                {loading ? t('booking_confirming') : t('booking_confirm')}
              </button> : <button type="button" className="btn-primary full" onClick={handleHold}
                disabled={checking || currentAvailability === null || currentAvailability < 1}>
                {t('booking_hold_rooms')}
              </button>}
            </aside>
          </form>
        </section>
      </main>

      <Footer />
    </div>
  );
}
