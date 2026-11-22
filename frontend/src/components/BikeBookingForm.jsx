import { useState } from 'react';
import { useLanguage } from '../contexts/LanguageContext.jsx';
import { apiRequest } from '../api/client.js';

export default function BikeBookingForm() {
  const { t } = useLanguage();
  const today = new Date().toISOString().slice(0, 10);
  const [form, setForm] = useState({
    startDate: '', endDate: '', numberOfBikes: 1,
    fullName: '', phone: '', email: '',
  });
  const [availability, setAvailability] = useState(null);
  const [checking, setChecking] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [booking, setBooking] = useState(null);
  const dateKey = `${form.startDate}|${form.endDate}`;
  const availableCount = availability?.key === dateKey ? availability.count : null;

  function updateField(field, value) {
    setForm(previous => ({ ...previous, [field]: value }));
  }

  async function checkAvailability() {
    setError('');
    setAvailability(null);
    if (!form.startDate || !form.endDate || form.endDate < form.startDate) {
      setError(t('service_rental_invalid_dates'));
      return;
    }
    setChecking(true);
    try {
      const params = new URLSearchParams({ startDate: form.startDate, endDate: form.endDate });
      const count = await apiRequest(`/bikes/availability?${params}`);
      if (!Number.isInteger(count) || count < 0) throw new Error(t('service_rental_check_error'));
      setAvailability({ key: dateKey, count });
      updateField('numberOfBikes', 1);
    } catch (requestError) {
      setError(requestError.message || t('service_rental_check_error'));
    } finally {
      setChecking(false);
    }
  }

  async function submitBooking(event) {
    event.preventDefault();
    setError('');
    if (availableCount === null || availableCount < 1
        || !Number.isInteger(Number(form.numberOfBikes))
        || Number(form.numberOfBikes) < 1
        || Number(form.numberOfBikes) > availableCount) {
      setError(t('service_rental_check_first'));
      return;
    }
    setSubmitting(true);
    try {
      const result = await apiRequest('/bike-bookings', {
        method: 'POST',
        body: JSON.stringify({
          ...form,
          numberOfBikes: Number(form.numberOfBikes),
          fullName: form.fullName.trim(),
          phone: form.phone.trim(),
          email: form.email.trim() || null,
        }),
      });
      setBooking(result);
    } catch (requestError) {
      setError(requestError.message || t('service_rental_submit_error'));
      setAvailability(null);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="bike-booking-section container" id="bike-booking">
      <div className="bike-booking-heading">
        <span className="kicker accent">{t('service_rental_form_kicker')}</span>
        <h2>{t('service_rental_form_title')}</h2>
        <p>{t('service_rental_form_desc')}</p>
      </div>

      {booking ? (
        <div className="bike-booking-success" role="status">
          <span className="bike-booking-success-mark">✓</span>
          <h3>{t('service_rental_success_title')}</h3>
          <p>{t('service_rental_success_desc')}</p>
          <p>{t('service_rental_bike_count')}: <strong>{booking.numberOfBikes}</strong></p>
          <p>{t('service_rental_dates')}: <strong>{booking.startDate} — {booking.endDate}</strong></p>
          {booking.totalPrice != null && <p>{t('service_rental_total')}: <strong>{Number(booking.totalPrice).toLocaleString('vi-VN')}đ</strong></p>}
          <p className="bike-booking-codes">{t('service_rental_booking_code')}: {booking.bookingIds?.join(', ')}</p>
        </div>
      ) : (
        <form className="bike-booking-form" onSubmit={submitBooking}>
          <div className="bike-booking-panel">
            <h3><span>01</span> {t('service_rental_dates_title')}</h3>
            <div className="form-grid">
              <label>
                <span>{t('service_rental_start_date')} *</span>
                <input type="date" min={today} value={form.startDate}
                  onChange={event => updateField('startDate', event.target.value)} required />
              </label>
              <label>
                <span>{t('service_rental_end_date')} *</span>
                <input type="date" min={form.startDate || today} value={form.endDate}
                  onChange={event => updateField('endDate', event.target.value)} required />
              </label>
            </div>
            <button type="button" className="btn-outline bike-check-button" onClick={checkAvailability} disabled={checking}>
              {checking ? t('service_rental_checking') : t('service_rental_check_button')}
            </button>
            {availableCount === 0 && <p className="form-error">{t('service_rental_no_bikes')}</p>}
            {availableCount > 0 && <p className="form-success">{t('service_rental_available_count', { n: availableCount })}</p>}
          </div>

          {availableCount > 0 && <div className="bike-booking-panel">
            <h3><span>02</span> {t('service_rental_guest_title')}</h3>
            <div className="form-grid">
              <label>
                <span>{t('service_rental_bike_count')} *</span>
                <input type="number" min="1" max={availableCount} step="1" value={form.numberOfBikes}
                  onChange={event => updateField('numberOfBikes', event.target.value)} required />
              </label>
              <label>
                <span>{t('booking_fullname')} *</span>
                <input type="text" value={form.fullName} onChange={event => updateField('fullName', event.target.value)} required />
              </label>
              <label>
                <span>{t('booking_phone')} *</span>
                <input type="tel" value={form.phone} onChange={event => updateField('phone', event.target.value)} required />
              </label>
              <label>
                <span>{t('booking_email')}</span>
                <input type="email" value={form.email} onChange={event => updateField('email', event.target.value)} />
              </label>
            </div>
            <p className="bike-booking-price-note">{t('service_rental_price_note')}</p>
            {error && <p className="form-error" role="alert">{error}</p>}
            <button type="submit" className="btn-primary bike-submit-button" disabled={submitting}>
              {submitting ? t('service_rental_submitting') : t('service_rental_submit')}
            </button>
          </div>}
          {error && !availableCount && <p className="form-error" role="alert">{error}</p>}
        </form>
      )}
    </section>
  );
}
