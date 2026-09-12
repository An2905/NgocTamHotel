import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useLanguage } from '../contexts/LanguageContext.jsx';
import Navbar from '../components/Navbar.jsx';

export default function ForgotPasswordPage() {
  const { t } = useLanguage();
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');

  function handleSubmit(e) {
    e.preventDefault();
    setLoading(true);
    // Simulate sending reset email
    setTimeout(() => {
      setSuccess(t('auth_forgot_success'));
      setLoading(false);
    }, 1500);
  }

  return (
    <div className="public-site">
      <Navbar />
      <main className="auth-page">
        <div className="auth-card">
          <div className="auth-header">
            <h1>{t('auth_forgot_title')}</h1>
            <p>{t('auth_forgot_subtitle')}</p>
          </div>
          <form onSubmit={handleSubmit}>
            <label>
              <span>{t('auth_email')}</span>
              <input type="email" value={email} onChange={e => setEmail(e.target.value)} required />
            </label>
            {success && <p className="form-success">{success}</p>}
            <button type="submit" className="btn-primary full" disabled={loading}>
              {loading ? t('auth_forgot_sending') : t('auth_forgot_btn')}
            </button>
          </form>
          <div className="auth-footer">
            <Link to="/login">{t('auth_back_home')}</Link>
          </div>
        </div>
      </main>
    </div>
  );
}
