import { Link } from 'react-router-dom';
import { useLanguage } from '../contexts/LanguageContext.jsx';
import { HOTEL_PHONE } from '../config/contact.js';
import Navbar from '../components/Navbar.jsx';
import Footer from '../components/Footer.jsx';
import BikeBookingForm from '../components/BikeBookingForm.jsx';

const MOTORBIKE_IMAGE = 'https://images.unsplash.com/photo-1617018001536-e1a443326d43?auto=format&fit=crop&w=1600&q=85';
const RENTAL_PHOTOS = [
  {
    src: 'https://images.unsplash.com/photo-1617018001536-e1a443326d43?auto=format&fit=crop&w=1000&q=85',
    source: 'https://unsplash.com/photos/green-motor-scooter-parked-on-beach-shore-during-daytime-9m_MMQmRgEE',
  },
  {
    src: 'https://images.unsplash.com/photo-1685101206910-0a0510a12521?auto=format&fit=crop&w=1000&q=85',
    source: 'https://unsplash.com/photos/a-man-riding-a-scooter-down-a-road-next-to-the-ocean-DWwjSQQawjc',
  },
  {
    src: 'https://images.unsplash.com/photo-1635669295890-9483101eb0a6?auto=format&fit=crop&w=1000&q=85',
    source: 'https://unsplash.com/photos/a-scooter-parked-on-a-beach-near-the-ocean-CBBiqdZY2gw',
  },
];

export default function ServicesPage() {
  const { t } = useLanguage();

  return (
    <div className="public-site">
      <Navbar />

      <main className="page-content rental-page">
        <section className="rental-hero">
          <div className="rental-hero-photo" style={{ backgroundImage: `url(${MOTORBIKE_IMAGE})` }} role="img" aria-label={t('service_motorbike')} />
          <div className="rental-hero-inner container">
            <div className="rental-hero-copy">
              <span className="rental-eyebrow">{t('services_kicker')} / NGỌC TÂM HOTEL</span>
              <h1>{t('services_title')}</h1>
              <p>{t('service_rental_intro')}</p>
              <div className="rental-hero-actions">
                <a className="btn-primary" href="#bike-booking">{t('service_rental_book_online')} <span aria-hidden="true">↓</span></a>
                <Link className="rental-text-link" to="/contact">{t('service_rental_contact')} <span aria-hidden="true">→</span></Link>
              </div>
            </div>
            <div className="rental-hero-caption">
              <span className="rental-caption-line" />
              <span>{t('service_rental_caption')}</span>
            </div>
          </div>
        </section>

        <section className="rental-intro container">
          <div className="rental-section-heading">
            <div>
              <span className="kicker accent">{t('service_rental_section_kicker')}</span>
              <h2>{t('service_rental_section_title')}</h2>
            </div>
            <p>{t('service_motorbike_desc')}</p>
          </div>

          <div className="rental-steps">
            <article className="rental-step">
              <span className="rental-step-number">01</span>
              <span className="rental-step-icon" aria-hidden="true">☎</span>
              <h3>{t('service_rental_step1_title')}</h3>
              <p>{t('service_rental_step1_desc')}</p>
            </article>
            <article className="rental-step">
              <span className="rental-step-number">02</span>
              <span className="rental-step-icon" aria-hidden="true">◷</span>
              <h3>{t('service_rental_step2_title')}</h3>
              <p>{t('service_rental_step2_desc')}</p>
            </article>
            <article className="rental-step">
              <span className="rental-step-number">03</span>
              <span className="rental-step-icon" aria-hidden="true">↗</span>
              <h3>{t('service_rental_step3_title')}</h3>
              <p>{t('service_rental_step3_desc')}</p>
            </article>
          </div>

          <div className="rental-gallery">
            {RENTAL_PHOTOS.map((photo, index) => (
              <a href={photo.source} target="_blank" rel="noopener noreferrer" key={photo.src}
                aria-label={`${t('service_rental_gallery_alt')} ${index + 1}`}>
                <img src={photo.src} alt={`${t('service_rental_gallery_alt')} ${index + 1}`} loading="lazy" />
              </a>
            ))}
          </div>
          <p className="rental-gallery-note">{t('service_rental_gallery_note')}</p>
        </section>

        <BikeBookingForm />

        <section className="rental-contact container">
          <div>
            <span className="kicker">{t('service_rental_contact_kicker')}</span>
            <h2>{t('service_rental_contact_title')}</h2>
            <p>{t('service_rental_contact_desc')}</p>
          </div>
          <a className="rental-contact-button" href={`tel:${HOTEL_PHONE}`}>{HOTEL_PHONE} <span aria-hidden="true">↗</span></a>
        </section>
      </main>

      <Footer />
    </div>
  );
}
