import { useState } from "react";
import {
  Link,
  Navigate,
  Route,
  Routes,
  useLocation,
  useNavigate,
} from "react-router-dom";

import { isAuthenticated, login, saveSession } from "./api/auth.js";

const rooms = [
  {
    name: "Phòng Standard",
    price: "450.000đ",
    meta: "2 khách · 1 giường đôi",
    image: "standard",
  },
  {
    name: "Phòng Deluxe",
    price: "700.000đ",
    meta: "2 khách · Ban công",
    image: "deluxe",
  },
  {
    name: "Phòng Family",
    price: "1.000.000đ",
    meta: "4 khách · 2 giường",
    image: "family",
  },
];

function Logo({ light = false }) {
  return (
    <Link className={light ? "public-logo light" : "public-logo"} to="/">
      <span>NT</span>
      <div>
        <strong>Ngọc Tâm</strong>
        <small>HOTEL</small>
      </div>
    </Link>
  );
}

function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      const auth = await login(form);
      saveSession(auth);
      navigate(location.state?.from || "/pms", { replace: true });
    } catch (exception) {
      setError(exception.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="login-page">
      <section className="login-card">
        <Logo />
        <p className="login-kicker">HỆ THỐNG NỘI BỘ</p>
        <h1>Đăng nhập quản trị</h1>
        <p className="login-intro">
          Sử dụng tài khoản nhân viên để truy cập PMS và CMS.
        </p>
        <form onSubmit={submit}>
          <label>
            Tên đăng nhập
            <input
              autoComplete="username"
              value={form.username}
              onChange={(event) =>
                setForm({ ...form, username: event.target.value })
              }
              required
            />
          </label>
          <label>
            Mật khẩu
            <input
              type="password"
              autoComplete="current-password"
              value={form.password}
              onChange={(event) =>
                setForm({ ...form, password: event.target.value })
              }
              required
            />
          </label>
          {error && (
            <p className="login-error" role="alert">
              {error}
            </p>
          )}
          <button disabled={loading}>
            {loading ? "Đang đăng nhập…" : "Đăng nhập"}
          </button>
        </form>
        <Link className="login-back" to="/">
          ← Quay lại website
        </Link>
      </section>
    </main>
  );
}

import { updateProfile } from "./api/auth.js";

function UpdatePage() {
  const navigate = useNavigate();
  const currentUser = JSON.parse(sessionStorage.getItem("currentUser") || "{}");

  const [form, setForm] = useState({
    username: currentUser.username || "",
    email: currentUser.email || "",
    password: "",
  });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);
    try {
      const updated = await updateProfile(form);
      sessionStorage.setItem(
        "currentUser",
        JSON.stringify({
          id: currentUser.id,
          username: updated.username ?? form.username,
          email: updated.email ?? form.email,
        }),
      );
      setSuccess("Cập nhật thông tin thành công.");
      setForm((prev) => ({ ...prev, password: "" }));
    } catch (exception) {
      setError(exception.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="login-page">
      <section className="login-card">
        <Logo />
        <p className="login-kicker">HỆ THỐNG NỘI BỘ</p>
        <h1>Cập nhật thông tin</h1>
        <p className="login-intro">Chỉnh sửa thông tin tài khoản của bạn.</p>
        <form onSubmit={submit}>
          <label>
            Tên đăng nhập
            <input
              autoComplete="username"
              value={form.username}
              onChange={(event) =>
                setForm({ ...form, username: event.target.value })
              }
              required
            />
          </label>
          <label>
            Email
            <input
              type="email"
              autoComplete="email"
              value={form.email}
              onChange={(event) =>
                setForm({ ...form, email: event.target.value })
              }
              required
            />
          </label>
          <label>
            Mật khẩu mới (để trống nếu không đổi)
            <input
              type="password"
              autoComplete="new-password"
              value={form.password}
              onChange={(event) =>
                setForm({ ...form, password: event.target.value })
              }
            />
          </label>
          {error && (
            <p className="login-error" role="alert">
              {error}
            </p>
          )}
          {success && (
            <p className="booking-message" role="status">
              {success}
            </p>
          )}
          <button disabled={loading}>
            {loading ? "Đang lưu…" : "Lưu thay đổi"}
          </button>
        </form>
        <Link className="login-back" to="/pms">
          ← Quay lại PMS
        </Link>
      </section>
    </main>
  );
}

function ProtectedRoute({ children }) {
  const location = useLocation();
  return isAuthenticated() ? (
    children
  ) : (
    <Navigate to="/login" replace state={{ from: location.pathname }} />
  );
}

function PublicHome() {
  const [menuOpen, setMenuOpen] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const today = new Date().toISOString().slice(0, 10);

  function submitBooking(event) {
    event.preventDefault();
    setSubmitted(true);
  }

  return (
    <div className="public-site">
      <header className="public-header">
        <Logo light />
        <button
          className="public-menu"
          aria-label="Mở điều hướng"
          aria-expanded={menuOpen}
          onClick={() => setMenuOpen(!menuOpen)}
        >
          Menu
        </button>
        <nav
          className={menuOpen ? "public-nav open" : "public-nav"}
          aria-label="Điều hướng website"
        >
          <a href="#about">Giới thiệu</a>
          <a href="#rooms">Hạng phòng</a>
          <a href="#services">Dịch vụ</a>
          <a href="#contact">Liên hệ</a>
        </nav>
        <a className="header-cta" href="#booking">
          Đặt phòng
        </a>
      </header>

      <main>
        <section className="hotel-hero">
          <div className="hero-content">
            <p className="kicker">TRẢI NGHIỆM LƯU TRÚ ẤM ÁP</p>
            <h1>Một chốn bình yên giữa lòng thành phố</h1>
            <p>
              Ngọc Tâm Hotel mang đến không gian gần gũi, tiện nghi và dịch vụ
              tận tâm cho mọi hành trình.
            </p>
            <a className="gold-button" href="#booking">
              Đặt kỳ nghỉ của bạn
            </a>
          </div>
          <div className="hero-note">
            <span>24/7</span>
            <p>Lễ tân luôn sẵn sàng hỗ trợ bạn</p>
          </div>
        </section>

        <section className="booking-wrap" id="booking">
          <form className="booking-form" onSubmit={submitBooking}>
            <label>
              Ngày nhận phòng
              <input type="date" min={today} required />
            </label>
            <label>
              Ngày trả phòng
              <input type="date" min={today} required />
            </label>
            <label>
              Số khách
              <select defaultValue="2">
                <option value="1">1 khách</option>
                <option value="2">2 khách</option>
                <option value="3">3 khách</option>
                <option value="4">4 khách</option>
              </select>
            </label>
            <button type="submit">Kiểm tra phòng trống</button>
          </form>
          {submitted && (
            <p className="booking-message" role="status">
              Cảm ơn bạn! Các hạng phòng phù hợp đang sẵn sàng bên dưới.
            </p>
          )}
        </section>

        <section className="about-section public-container" id="about">
          <div>
            <p className="kicker dark">VỀ NGỌC TÂM HOTEL</p>
            <h2>Sự chân thành trong từng trải nghiệm</h2>
          </div>
          <div>
            <p>
              Chúng tôi tin rằng một kỳ nghỉ đáng nhớ bắt đầu từ những điều giản
              dị: căn phòng sạch sẽ, giấc ngủ êm ái và nụ cười thân thiện khi
              bạn trở về.
            </p>
            <div className="about-facts">
              <span>
                <strong>32</strong>Phòng nghỉ
              </span>
              <span>
                <strong>4.8/5</strong>Đánh giá
              </span>
              <span>
                <strong>24/7</strong>Hỗ trợ
              </span>
            </div>
          </div>
        </section>

        <section className="rooms-section" id="rooms">
          <div className="public-container">
            <div className="section-title">
              <div>
                <p className="kicker dark">KHÔNG GIAN NGHỈ DƯỠNG</p>
                <h2>Chọn căn phòng dành cho bạn</h2>
              </div>
              <p>Thiết kế ấm cúng, đầy đủ tiện nghi và mức giá minh bạch.</p>
            </div>
            <div className="public-room-grid">
              {rooms.map((room) => (
                <article className="public-room" key={room.name}>
                  <div
                    className={`room-image ${room.image}`}
                    role="img"
                    aria-label={room.name}
                  ></div>
                  <div className="room-info">
                    <span>{room.meta}</span>
                    <h3>{room.name}</h3>
                    <div>
                      <p>
                        Từ <strong>{room.price}</strong> / đêm
                      </p>
                      <a href="#booking">Đặt phòng</a>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          </div>
        </section>

        <section className="services-section public-container" id="services">
          <div className="service-intro">
            <p className="kicker dark">TIỆN ÍCH</p>
            <h2>Mọi điều bạn cần cho một kỳ nghỉ thoải mái</h2>
          </div>
          <div className="service-grid">
            <article>
              <span>01</span>
              <h3>Bữa sáng mỗi ngày</h3>
              <p>Thực đơn Việt Nam nhẹ nhàng, phục vụ từ 6:30 đến 9:30.</p>
            </article>
            <article>
              <span>02</span>
              <h3>Đưa đón sân bay</h3>
              <p>Dịch vụ đặt trước giúp hành trình của bạn thuận tiện hơn.</p>
            </article>
            <article>
              <span>03</span>
              <h3>Wi-Fi tốc độ cao</h3>
              <p>Kết nối ổn định tại phòng nghỉ và mọi khu vực chung.</p>
            </article>
          </div>
        </section>

        <section className="contact-banner" id="contact">
          <p className="kicker">SẴN SÀNG CHO CHUYẾN ĐI?</p>
          <h2>Hãy để Ngọc Tâm chào đón bạn</h2>
          <p>
            Liên hệ <a href="tel:+842812345678">(028) 1234 5678</a> hoặc đặt
            phòng trực tuyến ngay hôm nay.
          </p>
          <a className="gold-button" href="#booking">
            Đặt phòng ngay
          </a>
        </section>
      </main>
      <footer className="public-footer">
        <Logo light />
        <p>© 2026 Ngọc Tâm Hotel. Trải nghiệm lưu trú chân thành.</p>
        <div>
          <Link to="/pms">PMS</Link>
          <Link to="/cms">CMS</Link>
        </div>
      </footer>
    </div>
  );
}

const roomStatus = [
  ["101", "Trống"],
  ["102", "Đang ở"],
  ["201", "Đã đặt"],
  ["202", "Cần dọn"],
  ["301", "Bảo trì"],
  ["302", "Trống"],
];

function InternalLayout({ type, children }) {
  return (
    <div className="internal-shell">
      <aside className="internal-sidebar">
        <Logo light />
        <p className="system-label">{type}</p>
        <nav aria-label={`Điều hướng ${type}`}>
          <a className="active" href="#dashboard">
            Tổng quan
          </a>
          <a href="#management">Quản lý</a>
          <a href="#reports">Báo cáo</a>
        </nav>
        <Link className="back-site" to="/">
          ← Về website
        </Link>
      </aside>
      <main className="internal-main">{children}</main>
    </div>
  );
}

function PmsPage() {
  return (
    <InternalLayout type="PROPERTY MANAGEMENT SYSTEM">
      <header className="internal-header">
        <div>
          <p>Chủ nhật, 23 tháng 8</p>
          <h1>Vận hành khách sạn</h1>
        </div>
        <button>Tạo đặt phòng</button>
      </header>
      <section className="metric-grid">
        <article>
          <p>Tổng số phòng</p>
          <strong>32</strong>
          <span>4 hạng phòng</span>
        </article>
        <article>
          <p>Đang có khách</p>
          <strong>18</strong>
          <span>56% công suất</span>
        </article>
        <article>
          <p>Check-in hôm nay</p>
          <strong>07</strong>
          <span>5 đã xác nhận</span>
        </article>
        <article>
          <p>Phòng cần dọn</p>
          <strong>04</strong>
          <span>Ưu tiên trong ngày</span>
        </article>
      </section>
      <section className="internal-panel">
        <div className="panel-title">
          <div>
            <p>TÌNH TRẠNG PHÒNG</p>
            <h2>Sơ đồ phòng</h2>
          </div>
          <button>Xem tất cả</button>
        </div>
        <div className="pms-room-grid">
          {roomStatus.map(([room, status]) => (
            <article key={room}>
              <span>Tầng {room[0]}</span>
              <strong>{room}</strong>
              <em>{status}</em>
            </article>
          ))}
        </div>
      </section>
    </InternalLayout>
  );
}

function CmsPage() {
  return (
    <InternalLayout type="CONTENT MANAGEMENT SYSTEM">
      <header className="internal-header">
        <div>
          <p>Nội dung website</p>
          <h1>Quản lý nội dung</h1>
        </div>
        <button>Tạo bài viết</button>
      </header>
      <section className="cms-grid">
        <article>
          <span>TRANG CHỦ</span>
          <h2>Banner giới thiệu</h2>
          <p>Cập nhật tiêu đề, mô tả và nút đặt phòng hiển thị đầu trang.</p>
          <button>Chỉnh sửa</button>
        </article>
        <article>
          <span>HẠNG PHÒNG</span>
          <h2>Thông tin phòng</h2>
          <p>Quản lý hình ảnh, tiện nghi, sức chứa và giá hiển thị.</p>
          <button>Quản lý phòng</button>
        </article>
        <article>
          <span>DỊCH VỤ</span>
          <h2>Tiện ích khách sạn</h2>
          <p>Cập nhật bữa sáng, đưa đón, Wi-Fi và các dịch vụ khác.</p>
          <button>Chỉnh sửa</button>
        </article>
        <article>
          <span>LIÊN HỆ</span>
          <h2>Thông tin khách sạn</h2>
          <p>Quản lý số điện thoại, địa chỉ, bản đồ và mạng xã hội.</p>
          <button>Chỉnh sửa</button>
        </article>
      </section>
    </InternalLayout>
  );
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<PublicHome />} />
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/pms"
        element={
          <ProtectedRoute>
            <PmsPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/cms"
        element={
          <ProtectedRoute>
            <CmsPage />
          </ProtectedRoute>
        }
      />
      <Route path="/update" element={<ProtectedRoute><UpdatePage /></ProtectedRoute>}/>
    </Routes>
  );
}
