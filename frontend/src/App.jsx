import { Routes, Route } from 'react-router-dom';

import HomePage from './pages/HomePage.jsx';
import RoomsPage from './pages/RoomsPage.jsx';
import RoomDetailPage from './pages/RoomDetailPage.jsx';
import BookingPage from './pages/BookingPage.jsx';
import PaymentPage from './pages/PaymentPage.jsx';
import ServicesPage from './pages/ServicesPage.jsx';
import ContactPage from './pages/ContactPage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import ForgotPasswordPage from './pages/ForgotPasswordPage.jsx';
import AdminDashboard from './pages/admin/AdminDashboard.jsx';

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/rooms" element={<RoomsPage />} />
      <Route path="/rooms/:id" element={<RoomDetailPage />} />
      <Route path="/booking" element={<BookingPage />} />
      <Route path="/payment/:bookingId" element={<PaymentPage />} />
      <Route path="/services" element={<ServicesPage />} />
      <Route path="/contact" element={<ContactPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/admin" element={<AdminDashboard />} />
    </Routes>
  );
}
