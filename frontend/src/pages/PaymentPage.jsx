import { useState, useEffect } from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { useLanguage } from '../contexts/LanguageContext.jsx';
import { apiRequest } from '../api/client.js';
import Navbar from '../components/Navbar.jsx';
import Footer from '../components/Footer.jsx';

export default function PaymentPage() {
  const { bookingId } = useParams();
  const [searchParams] = useSearchParams();
  const { t } = useLanguage();
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState('');
  const [status, setStatus] = useState(null);
  const [refresh, setRefresh] = useState(0);
  useEffect(() => {
    if (!bookingId) return undefined;
    let stopped = false;
    let timer;
    let attempts = 0;
    async function poll() {
      try {
        const result = await apiRequest('/payments/booking/' + bookingId);
        if (stopped) return;
        setStatus(result);
        setError('');
        if (result.bookingStatus === 'PENDING' && ++attempts < 30)
          timer = setTimeout(poll, 2000);
      } catch (err) { if (!stopped) setError(err.message); }
    }
    poll();
    return () => { stopped = true; clearTimeout(timer); };
  }, [bookingId, refresh]);
  const returnedFromGateway = searchParams.has('code') || searchParams.has('status');

  async function handlePay() {
    if (!bookingId) return;
    setProcessing(true);
    setError('');
    try {
      const payment = await apiRequest('/payments', {
        method: 'POST',
        body: JSON.stringify({ bookingId, method: 'PAYOS' }),
      });
      if (!payment?.checkoutUrl) throw new Error(t('payment_link_error'));
      const checkoutUrl = new URL(payment.checkoutUrl);
      if (checkoutUrl.protocol !== 'https:') throw new Error(t('payment_link_error'));
      window.location.assign(checkoutUrl.href);
    } catch (err) {
      setError(err.message || t('payment_failed_msg'));
      setProcessing(false);
    }
  }

  return (
    <div className="public-site">
      <Navbar />
      <main className="page-content">
        <section className="page-hero payment-hero">
          <div className="page-hero-overlay"></div>
          <div className="page-hero-content">
            <p className="kicker">{t('payment_title').toUpperCase()}</p>
            <h1>{t('payment_title')}</h1>
            <p>{t('payment_subtitle')}</p>
          </div>
        </section>

        <section className="payment-page container">
          <div className="booking-summary">
            <h2>{t('payment_booking_title')}</h2>
            <p className="payment-booking-id">{bookingId}</p>
            {status?.bookingStatus === 'CONFIRMED' ? (
              <p className="form-success">{t('payment_success')}</p>
            ) : status?.bookingStatus === 'CANCELLED' ? (
              <p className="form-error">{t('payment_booking_expired')}</p>
            ) : (
              <>
                <p className="payment-help">{returnedFromGateway || status?.needsReview
                  ? t('payment_pending_verification') : t('payment_payos_desc')}</p>
                <button type="button" className="btn-primary full" onClick={handlePay}
                  disabled={processing || !status?.canPay}>
                  {processing ? t('payment_processing') : t('payment_pay_payos')}
                </button>
              </>
            )}
            {error && <p className="form-error">{error}</p>}
            <button type="button" className="btn-outline" onClick={() => setRefresh(n => n + 1)}>
              {t('payment_check_status')}
            </button>
            <Link to="/" className="payment-back-link">{t('booking_back_home')}</Link>
          </div>
        </section>
      </main>
      <Footer />
    </div>
  );
}
