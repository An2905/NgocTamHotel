import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useLanguage } from '../contexts/LanguageContext.jsx';
import { useAuth } from '../contexts/AuthContext.jsx';
import Navbar from '../components/Navbar.jsx';

export default function RegisterPage() {
  const { t } = useLanguage();
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', email: '', password: '', confirmPassword: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (form.password !== form.confirmPassword) {
      setError(t('auth_password_mismatch'));
      return;
    }

    setLoading(true);
    try {
      await register({ username: form.username, email: form.email, password: form.password });
      setSuccess(t('auth_register_success'));
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="public-site">
      <Navbar />
      <main className="auth-page">
        <div className="auth-card">
          <div className="auth-header">
            <h1>{t('auth_register_title')}</h1>
            <p>{t('auth_register_subtitle')}</p>
          </div>
          <form onSubmit={handleSubmit}>
            <label>
              <span>{t('auth_username')}</span>
              <input type="text" autoComplete="username" value={form.username}
                onChange={e => setForm({ ...form, username: e.target.value })} required />
            </label>
            <label>
              <span>{t('auth_email')}</span>
              <input type="email" autoComplete="email" value={form.email}
                onChange={e => setForm({ ...form, email: e.target.value })} required />
            </label>
            <label>
              <span>{t('auth_password')}</span>
              <input type="password" autoComplete="new-password" value={form.password}
                onChange={e => setForm({ ...form, password: e.target.value })} required />
            </label>
            <label>
              <span>{t('auth_confirm_password')}</span>
              <input type="password" autoComplete="new-password" value={form.confirmPassword}
                onChange={e => setForm({ ...form, confirmPassword: e.target.value })} required />
            </label>
            {error && <p className="form-error">{error}</p>}
            {success && <p className="form-success">{success}</p>}
            <button type="submit" className="btn-primary full" disabled={loading}>
              {loading ? t('auth_registering') : t('auth_register_btn')}
            </button>
          </form>
          <div className="auth-footer">
            <p>{t('auth_has_account')} <Link to="/login">{t('auth_login_btn')}</Link></p>
          </div>
        </div>
      </main>
    </div>
  );
}
