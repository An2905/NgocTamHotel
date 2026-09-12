import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useLanguage } from '../contexts/LanguageContext.jsx';
import { apiRequest } from '../api/client.js';
import Navbar from '../components/Navbar.jsx';
import Footer from '../components/Footer.jsx';

function formatPrice(price) {
  return Number(price).toLocaleString('vi-VN');
}

export default function PaymentPage() {
  const { bookingId } = useParams();
  const { t } = useLanguage();
  const [method, setMethod] = useState('');
  const [status, setStatus] = useState('idle'); // idle, processing, success, failed
  const [payment, setPayment] = useState(null);
  const [error, setError] = useState('');

  async function handlePay() {
    if (!method) return;
    setStatus('processing');
    setError('');
    try {
      const result = await apiRequest('/payments', {
        method: 'POST',
        body: JSON.stringify({ bookingId: parseInt(bookingId), method }),
      });
      setPayment(result);

      if (method === 'VNPAY') {
        // Simulate VNPay processing delay
        await new Promise(resolve => setTimeout(resolve, 2000));
        const confirmed = await apiRequest(`/payments/${bookingId}/confirm`, { method: 'PUT' });
        setPayment(confirmed);
        setStatus('success');
      } else {
        setStatus('bank_info');
      }
    } catch (err) {
      setError(err.message);
      setStatus('failed');
    }
  }

  async function confirmBankTransfer() {
    setStatus('processing');
    try {
      const confirmed = await apiRequest(`/payments/${bookingId}/confirm`, { method: 'PUT' });
      setPayment(confirmed);
      setStatus('success');
    } catch (err) {
      setError(err.message);
      setStatus('failed');
    }
  }

  // Success screen
  if (status === 'success') {
    return (
      <div className="public-site">
        <Navbar />
        <main className="page-content">
          <div className="payment-result container">
            <div className="result-card success">
              <div className="result-icon">✓</div>
              <h1>{t('payment_success')}</h1>
              <p>{t('payment_success_msg')}</p>
              {payment && <p className="ref-code">Ref: {payment.transactionRef}</p>}
              <Link to="/" className="btn-primary">{t('booking_back_home')}</Link>
            </div>
          </div>
        </main>
        <Footer />
      </div>
    );
  }

  // Failed screen
  if (status === 'failed') {
    return (
      <div className="public-site">
        <Navbar />
        <main className="page-content">
          <div className="payment-result container">
            <div className="result-card failed">
              <div className="result-icon fail">✕</div>
              <h1>{t('payment_failed')}</h1>
              <p>{error || t('payment_failed_msg')}</p>
              <button className="btn-primary" onClick={() => setStatus('idle')}>{t('payment_retry')}</button>
            </div>
          </div>
        </main>
        <Footer />
      </div>
    );
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
          {status === 'processing' ? (
            <div className="payment-processing">
              <div className="spinner"></div>
              <p>{t('payment_processing')}</p>
            </div>
          ) : status === 'bank_info' ? (
            <div className="bank-transfer-info">
              <h2>{t('payment_bank')}</h2>
              <div className="bank-details">
                <div className="bank-row">
                  <span>{t('payment_bank_name')}</span>
                  <strong>Vietcombank</strong>
                </div>
                <div className="bank-row">
                  <span>{t('payment_bank_account')}</span>
                  <strong>1234 5678 9012</strong>
                </div>
                <div className="bank-row">
                  <span>{t('payment_bank_holder')}</span>
                  <strong>NGOC TAM HOTEL CO LTD</strong>
                </div>
                <div className="bank-row">
                  <span>{t('payment_bank_content')}</span>
                  <strong>{payment?.transactionRef}</strong>
                </div>
                <div className="bank-row total">
                  <span>{t('payment_amount')}</span>
                  <strong>{payment ? formatPrice(payment.amount) : '0'}đ</strong>
                </div>
              </div>
              <button className="btn-primary full" onClick={confirmBankTransfer}>
                {t('payment_confirm_transfer')}
              </button>
            </div>
          ) : (
            <div className="payment-methods">
              <h2>{t('payment_method')}</h2>
              <div className="method-grid">
                <button
                  className={`method-card ${method === 'VNPAY' ? 'selected' : ''}`}
                  onClick={() => setMethod('VNPAY')}
                >
                  <span className="method-icon">💳</span>
                  <strong>{t('payment_vnpay')}</strong>
                  <p>{t('payment_vnpay_desc')}</p>
                </button>
                <button
                  className={`method-card ${method === 'BANK_TRANSFER' ? 'selected' : ''}`}
                  onClick={() => setMethod('BANK_TRANSFER')}
                >
                  <span className="method-icon">🏦</span>
                  <strong>{t('payment_bank')}</strong>
                  <p>{t('payment_bank_desc')}</p>
                </button>
              </div>
              <button className="btn-primary full" onClick={handlePay} disabled={!method}>
                {t('payment_pay')}
              </button>
            </div>
          )}
        </section>
      </main>

      <Footer />
    </div>
  );
}
