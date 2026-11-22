import { Link } from 'react-router-dom';
import { useLanguage } from '../contexts/LanguageContext.jsx';
import { HOTEL_EMAIL, HOTEL_FACEBOOK_URL, HOTEL_MAP_URL, HOTEL_PHONE } from '../config/contact.js';

export default function Footer() {
  const { t } = useLanguage();

  return (
    <footer className="site-footer" id="site-footer">
      <div className="footer-inner">
        <div className="footer-brand">
          <Link className="site-logo" to="/">
            <span className="logo-mark">NT</span>
            <div className="logo-text">
              <strong>Ngọc Tâm</strong>
              <small>HOTEL</small>
            </div>
          </Link>
          <p className="footer-tagline">{t('about_desc')}</p>
        </div>

        <div className="footer-links">
          <h4>{t('footer_quick_links')}</h4>
          <Link to="/">{t('nav_home')}</Link>
          <Link to="/rooms">{t('nav_rooms')}</Link>
          <Link to="/services">{t('nav_services')}</Link>
          <Link to="/booking">{t('nav_booking')}</Link>
        </div>

        <div className="footer-contact">
          <h4>{t('footer_contact')}</h4>
          <p><a href={HOTEL_MAP_URL} target="_blank" rel="noopener noreferrer">{t('contact_address_value')}</a></p>
          <p><a href={`tel:${HOTEL_PHONE}`}>{t('contact_hotline_value')}</a></p>
          <p><a href={`mailto:${HOTEL_EMAIL}`}>{t('contact_email_value')}</a></p>
        </div>

        <div className="footer-social">
          <h4>{t('footer_follow')}</h4>
          <div className="social-icons">
            <a href={HOTEL_FACEBOOK_URL} target="_blank" rel="noopener noreferrer" aria-label="Facebook" className="facebook-button">
              <svg viewBox="0 0 16 16" fill="currentColor" width="25" height="25" aria-hidden="true" xmlns="http://www.w3.org/2000/svg">
                <path d="M16 8.049c0-4.446-3.582-8.05-8-8.05C3.58 0-.002 3.603-.002 8.05c0 4.017 2.926 7.347 6.75 7.951v-5.625h-2.03V8.05H6.75V6.275c0-2.017 1.195-3.131 3.022-3.131.876 0 1.791.157 1.791.157v1.98h-1.009c-.993 0-1.303.621-1.303 1.258v1.51h2.218l-.354 2.326H9.25V16c3.824-.604 6.75-3.934 6.75-7.951z" />
              </svg>
              <span>Facebook</span>
            </a>
          </div>
        </div>
      </div>

      <div className="footer-bottom">
        <p>{t('footer_copyright')}</p>
      </div>
    </footer>
  );
}
