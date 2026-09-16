import { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useLanguage } from '../contexts/LanguageContext.jsx';
import { useTheme } from '../contexts/ThemeContext.jsx';
import { useAuth } from '../contexts/AuthContext.jsx';

export default function Navbar() {
  const { t, lang, switchLang } = useLanguage();
  const { theme, toggleTheme } = useTheme();
  const { isAuthenticated, isAdmin, user, logout } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);
  const location = useLocation();

  useEffect(() => {
    function onScroll() {
      setScrolled(window.scrollY > 60);
    }
    window.addEventListener('scroll', onScroll);
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  useEffect(() => {
    setMenuOpen(false);
  }, [location.pathname]);

  const isHome = location.pathname === '/';

  return (
    <header className={`site-header ${scrolled || !isHome ? 'scrolled' : ''}`} id="site-header">
      <Link className="site-logo" to="/">
        <span className="logo-mark">NT</span>
        <div className="logo-text">
          <strong>Ngọc Tâm</strong>
          <small>HOTEL</small>
        </div>
      </Link>

      <button
        className="mobile-menu-btn"
        aria-label={t('nav_menu')}
        aria-expanded={menuOpen}
        onClick={() => setMenuOpen(!menuOpen)}
      >
        <span className={`hamburger ${menuOpen ? 'open' : ''}`}></span>
      </button>

      <nav className={`site-nav ${menuOpen ? 'open' : ''}`} aria-label="Main navigation">
        <Link to="/" className={location.pathname === '/' ? 'active' : ''}>{t('nav_home')}</Link>
        <Link to="/rooms" className={location.pathname.startsWith('/rooms') ? 'active' : ''}>{t('nav_rooms')}</Link>
        <Link to="/services" className={location.pathname === '/services' ? 'active' : ''}>{t('nav_services')}</Link>
        <Link to="/contact" className={location.pathname === '/contact' ? 'active' : ''}>{t('nav_contact')}</Link>
      </nav>

      <div className="header-actions">
        {/* Language toggle */}
        <button className="lang-toggle" onClick={() => switchLang(lang === 'vi' ? 'en' : 'vi')} title="Language">
          {lang === 'vi' ? 'EN' : 'VI'}
        </button>

        {/* Theme toggle */}
        <button className="theme-toggle" onClick={toggleTheme} title={theme === 'light' ? t('theme_dark') : t('theme_light')}>
          {theme === 'light' ? '🌙' : '☀️'}
        </button>

        {isAuthenticated ? (
          <div className="user-menu">
            <span className="user-name">{user?.username}</span>
            {isAdmin && <Link to="/admin" className="admin-link">{t('nav_admin')}</Link>}
            <button className="logout-btn" onClick={logout}>{t('nav_logout')}</button>
          </div>
        ) : (
          <div className="auth-links">
            <Link to="/login" className="login-link">{t('nav_login')}</Link>
            <Link to="/register" className="register-link">{t('nav_register')}</Link>
          </div>
        )}

        <Link to="/booking" className="header-book-btn">{t('nav_booking')}</Link>
      </div>
    </header>
  );
}
