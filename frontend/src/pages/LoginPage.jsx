import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useLanguage } from '../contexts/LanguageContext.jsx';
import { useAuth } from '../contexts/AuthContext.jsx';
import Navbar from '../components/Navbar.jsx';

export default function LoginPage() {
  const { t } = useLanguage();
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const auth = await login(form);
      const dest = location.state?.from || (auth.role === 'ADMIN' ? '/admin' : '/');
      navigate(dest, { replace: true });
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
            <h1>{t('auth_login_title')}</h1>
            <p>{t('auth_login_subtitle')}</p>
          </div>
          <form onSubmit={handleSubmit}>
            <label>
              <span>{t('auth_username')}</span>
              <input type="text" autoComplete="username" value={form.username}
                onChange={e => setForm({ ...form, username: e.target.value })} required />
            </label>
            <label>
              <span>{t('auth_password')}</span>
              <input type="password" autoComplete="current-password" value={form.password}
                onChange={e => setForm({ ...form, password: e.target.value })} required />
            </label>
            {error && <p className="form-error">{error}</p>}
            <button type="submit" className="btn-primary full" disabled={loading}>
              {loading ? t('auth_logging_in') : t('auth_login_btn')}
            </button>
          </form>
          <div className="auth-footer">
            <Link to="/forgot-password">{t('auth_forgot_password')}</Link>
            <p>{t('auth_no_account')} <Link to="/register">{t('auth_register_btn')}</Link></p>
          </div>
        </div>
      </main>
    </div>
  );
}
