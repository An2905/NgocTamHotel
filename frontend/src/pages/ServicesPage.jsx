import { useLanguage } from '../contexts/LanguageContext.jsx';
import Navbar from '../components/Navbar.jsx';
import Footer from '../components/Footer.jsx';

const SERVICE_IMAGES = {
  motorbike: 'https://images.unsplash.com/photo-1558618666-fcd25c85f82e?auto=format&fit=crop&w=900&q=80',
  electric_cart: 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&w=900&q=80',
  camping: 'https://images.unsplash.com/photo-1504851149312-7a075b496cc7?auto=format&fit=crop&w=900&q=80',
  party: 'https://images.unsplash.com/photo-1533174072545-7a4b6ad7a6c3?auto=format&fit=crop&w=900&q=80',
  food: 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?auto=format&fit=crop&w=900&q=80',
};

const SERVICES = [
  { key: 'motorbike', icon: '🏍️' },
  { key: 'electric_cart', icon: '🚗' },
  { key: 'camping', icon: '🔥' },
  { key: 'party', icon: '🎉' },
  { key: 'food', icon: '🍽️' },
];

export default function ServicesPage() {
  const { t } = useLanguage();

  return (
    <div className="public-site">
      <Navbar />

      <main className="page-content">
        <section className="page-hero services-hero">
          <div className="page-hero-overlay"></div>
          <div className="page-hero-content">
            <p className="kicker">{t('services_kicker')}</p>
            <h1>{t('services_title')}</h1>
          </div>
        </section>

        <section className="services-page container">
          <div className="services-full-grid">
            {SERVICES.map((svc, i) => (
              <article className={`service-full-card ${i % 2 === 1 ? 'reverse' : ''}`} key={svc.key}>
                <div className="service-full-img" style={{ backgroundImage: `url(${SERVICE_IMAGES[svc.key]})` }}>
                </div>
                <div className="service-full-body">
                  <span className="service-icon-large">{svc.icon}</span>
                  <h2>{t(`service_${svc.key}`)}</h2>
                  <p>{t(`service_${svc.key}_desc`)}</p>
                </div>
              </article>
            ))}
          </div>
        </section>
      </main>

      <Footer />
    </div>
  );
}
