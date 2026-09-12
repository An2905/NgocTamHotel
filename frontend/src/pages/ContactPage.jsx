import { useState } from 'react';
import { useLanguage } from '../contexts/LanguageContext.jsx';
import { apiRequest } from '../api/client.js';
import Navbar from '../components/Navbar.jsx';
import Footer from '../components/Footer.jsx';

export default function ContactPage() {
  const { t } = useLanguage();
  const [form, setForm] = useState({ fullName: '', email: '', phone: '', subject: '', message: '' });
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  function updateField(field, value) {
    setForm(prev => ({ ...prev, [field]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      await apiRequest('/contact', {
        method: 'POST',
        body: JSON.stringify(form),
      });
      setSuccess(t('contact_form_success'));
      setForm({ fullName: '', email: '', phone: '', subject: '', message: '' });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="public-site">
      <Navbar />

      <main className="page-content">
        <section className="page-hero contact-hero">
          <div className="page-hero-overlay"></div>
          <div className="page-hero-content">
            <p className="kicker">{t('contact_kicker')}</p>
            <h1>{t('contact_title')}</h1>
            <p>{t('contact_subtitle')}</p>
          </div>
        </section>

        <section className="contact-page container">
          <div className="contact-grid">
            {/* Contact Info */}
            <div className="contact-info-side">
              <div className="contact-info-card">
                <div className="contact-item">
                  <span className="contact-icon">📍</span>
                  <div>
                    <h4>{t('contact_address')}</h4>
                    <p>{t('contact_address_value')}</p>
                  </div>
                </div>
                <div className="contact-item">
                  <span className="contact-icon">📞</span>
                  <div>
                    <h4>{t('contact_hotline')}</h4>
                    <p><a href="tel:+842973846000">{t('contact_hotline_value')}</a></p>
                  </div>
                </div>
                <div className="contact-item">
                  <span className="contact-icon">✉️</span>
                  <div>
                    <h4>{t('contact_email')}</h4>
                    <p><a href="mailto:info@ngoctamhotel.vn">{t('contact_email_value')}</a></p>
                  </div>
                </div>
              </div>

              {/* Google Maps */}
              <div className="contact-map">
                <iframe
                  title="Ngọc Tâm Hotel Location"
                  src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3919.6!2d103.96!3d10.22!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x0%3A0x0!2zMTDCsDEzJzEyLjAiTiAxMDPCsDU3JzM2LjAiRQ!5e0!3m2!1svi!2s!4v1"
                  width="100%"
                  height="300"
                  style={{ border: 0, borderRadius: '12px' }}
                  allowFullScreen=""
                  loading="lazy"
                  referrerPolicy="no-referrer-when-downgrade"
                ></iframe>
              </div>
            </div>

            {/* Contact Form */}
            <div className="contact-form-side">
              <form className="contact-form" onSubmit={handleSubmit}>
                <h2>{t('contact_kicker')}</h2>
                <label>
                  <span>{t('contact_form_name')} *</span>
                  <input type="text" value={form.fullName} onChange={e => updateField('fullName', e.target.value)} required />
                </label>
                <label>
                  <span>{t('contact_form_email')} *</span>
                  <input type="email" value={form.email} onChange={e => updateField('email', e.target.value)} required />
                </label>
                <label>
                  <span>{t('contact_form_phone')}</span>
                  <input type="tel" value={form.phone} onChange={e => updateField('phone', e.target.value)} />
                </label>
                <label>
                  <span>{t('contact_form_subject')}</span>
                  <input type="text" value={form.subject} onChange={e => updateField('subject', e.target.value)} />
                </label>
                <label>
                  <span>{t('contact_form_message')} *</span>
                  <textarea rows="5" value={form.message} onChange={e => updateField('message', e.target.value)} required></textarea>
                </label>

                {error && <p className="form-error">{error}</p>}
                {success && <p className="form-success">{success}</p>}

                <button type="submit" className="btn-primary full" disabled={loading}>
                  {loading ? t('contact_form_sending') : t('contact_form_submit')}
                </button>
              </form>
            </div>
          </div>
        </section>
      </main>

      <Footer />
    </div>
  );
}
