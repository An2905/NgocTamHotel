import { useState, useEffect, useRef } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { useLanguage } from '../../contexts/LanguageContext.jsx';
import { useTheme } from '../../contexts/ThemeContext.jsx';
import { useAuth } from '../../contexts/AuthContext.jsx';
import { apiRequest } from '../../api/client.js';
import * as XLSX from 'xlsx';

function formatPrice(price) {
  return Number(price).toLocaleString('vi-VN');
}

function formatDate(dateStr) {
  return new Date(dateStr).toLocaleDateString('vi-VN');
}

const STATUS_LABELS = {
  PENDING: { label: 'Chờ xử lý', cls: 'pending' },
  CONFIRMED: { label: 'Đã xác nhận', cls: 'confirmed' },
  CHECKED_IN: { label: 'Đã nhận phòng', cls: 'checkedin' },
  CHECKED_OUT: { label: 'Đã trả phòng', cls: 'checkedout' },
  CANCELLED: { label: 'Đã hủy', cls: 'cancelled' },
};

export default function AdminDashboard() {
  const { t, lang } = useLanguage();
  const { theme } = useTheme();
  const { isAuthenticated, isAdmin } = useAuth();
  const [bookings, setBookings] = useState([]);
  const [revenue, setRevenue] = useState(null);
  const [filterType, setFilterType] = useState('month');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('dashboard');
  const chartRef = useRef(null);

  // Redirect non-admin
  if (!isAuthenticated || !isAdmin) {
    return <Navigate to="/login" replace />;
  }

  function getFilterDates(type) {
    const now = new Date();
    let from, to;
    if (type === 'month') {
      from = new Date(now.getFullYear(), now.getMonth(), 1);
      to = new Date(now.getFullYear(), now.getMonth() + 1, 0);
    } else if (type === 'quarter') {
      const q = Math.floor(now.getMonth() / 3);
      from = new Date(now.getFullYear(), q * 3, 1);
      to = new Date(now.getFullYear(), q * 3 + 3, 0);
    } else if (type === 'year') {
      from = new Date(now.getFullYear(), 0, 1);
      to = new Date(now.getFullYear(), 11, 31);
    }
    return {
      from: from.toISOString().slice(0, 10),
      to: to.toISOString().slice(0, 10),
    };
  }

  useEffect(() => {
    loadData();
  }, []);

  async function loadData(customFrom, customTo) {
    setLoading(true);
    try {
      const dates = customFrom && customTo
        ? { from: customFrom, to: customTo }
        : getFilterDates(filterType);

      setDateFrom(dates.from);
      setDateTo(dates.to);

      const [bookingsData, revenueData] = await Promise.all([
        apiRequest(`/admin/bookings?from=${dates.from}&to=${dates.to}`),
        apiRequest(`/admin/revenue?from=${dates.from}&to=${dates.to}`),
      ]);
      setBookings(bookingsData);
      setRevenue(revenueData);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  }

  function handleFilterChange(type) {
    setFilterType(type);
    if (type !== 'custom') {
      const dates = getFilterDates(type);
      loadData(dates.from, dates.to);
    }
  }

  function handleCustomFilter() {
    if (dateFrom && dateTo) {
      loadData(dateFrom, dateTo);
    }
  }

  async function handleStatusChange(bookingId, newStatus) {
    try {
      await apiRequest(`/admin/bookings/${bookingId}/status`, {
        method: 'PUT',
        body: JSON.stringify({ status: newStatus }),
      });
      loadData(dateFrom, dateTo);
    } catch (err) {
      alert(err.message);
    }
  }

  function exportBookingsXLSX() {
    const data = bookings.map(b => ({
      'Mã ĐP': b.bookingCode,
      'Khách hàng': b.guestName,
      'SĐT': b.guestPhone,
      'Email': b.guestEmail,
      'Phòng': b.roomNumber,
      'Loại phòng': b.roomTypeName,
      'Ngày nhận': b.checkInDate,
      'Ngày trả': b.checkOutDate,
      'Người lớn': b.adults,
      'Trẻ em': b.children,
      'Tổng tiền (VNĐ)': Number(b.totalAmount),
      'Trạng thái': STATUS_LABELS[b.status]?.label || b.status,
      'Thanh toán': b.paymentStatus,
    }));
    const ws = XLSX.utils.json_to_sheet(data);
    ws['!cols'] = [
      { wch: 18 }, { wch: 22 }, { wch: 14 }, { wch: 26 },
      { wch: 10 }, { wch: 22 }, { wch: 12 }, { wch: 12 },
      { wch: 10 }, { wch: 8 }, { wch: 16 }, { wch: 14 }, { wch: 14 },
    ];
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Danh sách đặt phòng');
    XLSX.writeFile(wb, `bookings-${dateFrom}-${dateTo}.xlsx`);
  }

  function exportRevenueXLSX() {
    if (!revenue) return;
    const data = [{
      'Kỳ': revenue.period,
      'Doanh thu phòng (VNĐ)': Number(revenue.roomRevenue),
      'Tổng doanh thu (VNĐ)': Number(revenue.totalRevenue),
      'Tổng đặt phòng': revenue.totalBookings,
      'Hoàn thành': revenue.completedBookings,
    }];
    const ws = XLSX.utils.json_to_sheet(data);
    ws['!cols'] = [
      { wch: 28 }, { wch: 22 }, { wch: 22 }, { wch: 16 }, { wch: 12 },
    ];
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Doanh thu');
    XLSX.writeFile(wb, `revenue-${dateFrom}-${dateTo}.xlsx`);
  }

  // Draw chart
  useEffect(() => {
    if (!chartRef.current || !bookings.length) return;
    const canvas = chartRef.current;
    const ctx = canvas.getContext('2d');
    const dpr = window.devicePixelRatio || 1;
    canvas.width = canvas.offsetWidth * dpr;
    canvas.height = canvas.offsetHeight * dpr;
    ctx.scale(dpr, dpr);
    const w = canvas.offsetWidth;
    const h = canvas.offsetHeight;

    // Group bookings by date
    const dailyRevenue = {};
    bookings.forEach(b => {
      const date = b.checkInDate;
      dailyRevenue[date] = (dailyRevenue[date] || 0) + Number(b.totalAmount);
    });
    const dates = Object.keys(dailyRevenue).sort();
    const values = dates.map(d => dailyRevenue[d]);
    const maxVal = Math.max(...values, 1);

    // Clear
    const isDark = theme === 'dark';
    ctx.fillStyle = isDark ? '#1a2332' : '#fff';
    ctx.fillRect(0, 0, w, h);

    if (dates.length === 0) {
      ctx.fillStyle = isDark ? '#8899aa' : '#999';
      ctx.font = '14px Inter, sans-serif';
      ctx.textAlign = 'center';
      ctx.fillText('Không có dữ liệu', w / 2, h / 2);
      return;
    }

    const padding = { top: 20, right: 20, bottom: 40, left: 70 };
    const chartW = w - padding.left - padding.right;
    const chartH = h - padding.top - padding.bottom;

    // Grid lines
    ctx.strokeStyle = isDark ? '#2a3a4a' : '#eee';
    ctx.lineWidth = 1;
    for (let i = 0; i <= 4; i++) {
      const y = padding.top + (chartH / 4) * i;
      ctx.beginPath();
      ctx.moveTo(padding.left, y);
      ctx.lineTo(w - padding.right, y);
      ctx.stroke();

      ctx.fillStyle = isDark ? '#8899aa' : '#999';
      ctx.font = '11px Inter, sans-serif';
      ctx.textAlign = 'right';
      const val = maxVal - (maxVal / 4) * i;
      ctx.fillText(formatPrice(val), padding.left - 8, y + 4);
    }

    // Bars
    const barW = Math.min(40, chartW / dates.length - 8);
    const gap = (chartW - barW * dates.length) / (dates.length + 1);

    dates.forEach((date, i) => {
      const x = padding.left + gap + i * (barW + gap);
      const barH = (values[i] / maxVal) * chartH;
      const y = padding.top + chartH - barH;

      const gradient = ctx.createLinearGradient(x, y, x, y + barH);
      gradient.addColorStop(0, '#c9a367');
      gradient.addColorStop(1, '#9a6d26');
      ctx.fillStyle = gradient;

      // Rounded top
      const radius = Math.min(4, barW / 2);
      ctx.beginPath();
      ctx.moveTo(x + radius, y);
      ctx.lineTo(x + barW - radius, y);
      ctx.quadraticCurveTo(x + barW, y, x + barW, y + radius);
      ctx.lineTo(x + barW, y + barH);
      ctx.lineTo(x, y + barH);
      ctx.lineTo(x, y + radius);
      ctx.quadraticCurveTo(x, y, x + radius, y);
      ctx.fill();

      // Date label
      ctx.fillStyle = isDark ? '#8899aa' : '#999';
      ctx.font = '10px Inter, sans-serif';
      ctx.textAlign = 'center';
      ctx.fillText(date.slice(5), x + barW / 2, h - padding.bottom + 16);
    });
  }, [bookings, theme]);

  const stats = {
    totalRevenue: revenue?.totalRevenue || 0,
    totalBookings: revenue?.totalBookings || 0,
    completed: revenue?.completedBookings || 0,
    pending: bookings.filter(b => b.status === 'PENDING' || b.status === 'CONFIRMED').length,
  };

  return (
    <div className="admin-shell">
      {/* Sidebar */}
      <aside className="admin-sidebar">
        <Link className="site-logo" to="/">
          <span className="logo-mark">NT</span>
          <div className="logo-text">
            <strong>Ngọc Tâm</strong>
            <small>HOTEL</small>
          </div>
        </Link>
        <p className="admin-label">ADMIN PANEL</p>
        <nav className="admin-nav">
          <button className={activeTab === 'dashboard' ? 'active' : ''} onClick={() => setActiveTab('dashboard')}>
            📊 {t('admin_dashboard')}
          </button>
          <button className={activeTab === 'bookings' ? 'active' : ''} onClick={() => setActiveTab('bookings')}>
            📋 {t('admin_bookings')}
          </button>
        </nav>
        <Link to="/" className="admin-back">{t('admin_back')}</Link>
      </aside>

      {/* Main content */}
      <main className="admin-main">
        {/* Filter bar */}
        <div className="admin-toolbar">
          <div className="filter-tabs">
            {['month', 'quarter', 'year', 'custom'].map(type => (
              <button key={type} className={filterType === type ? 'active' : ''}
                onClick={() => handleFilterChange(type)}>
                {t(`admin_filter_${type}`)}
              </button>
            ))}
          </div>
          {filterType === 'custom' && (
            <div className="custom-dates">
              <input type="date" value={dateFrom} onChange={e => setDateFrom(e.target.value)} />
              <input type="date" value={dateTo} onChange={e => setDateTo(e.target.value)} />
              <button className="btn-primary small" onClick={handleCustomFilter}>{t('admin_apply')}</button>
            </div>
          )}
          <div className="export-buttons">
            <button className="btn-outline small" onClick={exportRevenueXLSX}>📥 {t('admin_export_revenue')}</button>
            <button className="btn-outline small" onClick={exportBookingsXLSX}>📥 {t('admin_export_bookings')}</button>
          </div>
        </div>

        {loading ? (
          <div className="loading-state"><p>{t('common_loading')}</p></div>
        ) : (
          <>
            {/* Stats Cards */}
            <div className="admin-stats">
              <div className="stat-card revenue">
                <p>{t('admin_total_revenue')}</p>
                <strong>{formatPrice(stats.totalRevenue)}đ</strong>
              </div>
              <div className="stat-card">
                <p>{t('admin_total_bookings')}</p>
                <strong>{stats.totalBookings}</strong>
              </div>
              <div className="stat-card">
                <p>{t('admin_completed')}</p>
                <strong>{stats.completed}</strong>
              </div>
              <div className="stat-card">
                <p>{t('admin_pending')}</p>
                <strong>{stats.pending}</strong>
              </div>
            </div>

            {/* Revenue Chart */}
            {activeTab === 'dashboard' && (
              <div className="admin-chart-panel">
                <h3>{t('admin_revenue')}</h3>
                <canvas ref={chartRef} className="revenue-chart"></canvas>
              </div>
            )}

            {/* Bookings Table */}
            <div className="admin-table-panel">
              <h3>{t('admin_bookings')}</h3>
              <div className="table-scroll">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Mã ĐP</th>
                      <th>{t('admin_guest')}</th>
                      <th>{t('admin_room')}</th>
                      <th>{t('admin_checkin')}</th>
                      <th>{t('admin_checkout')}</th>
                      <th>{t('admin_amount')}</th>
                      <th>{t('admin_status')}</th>
                      <th>{t('admin_actions')}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {bookings.length === 0 ? (
                      <tr><td colSpan="8" className="empty-row">Không có dữ liệu</td></tr>
                    ) : bookings.map(b => (
                      <tr key={b.id}>
                        <td className="code-cell">{b.bookingCode}</td>
                        <td>{b.guestName}</td>
                        <td>{b.roomNumber} <small>({b.roomTypeName})</small></td>
                        <td>{formatDate(b.checkInDate)}</td>
                        <td>{formatDate(b.checkOutDate)}</td>
                        <td className="amount-cell">{formatPrice(b.totalAmount)}đ</td>
                        <td>
                          <span className={`status-badge ${STATUS_LABELS[b.status]?.cls || ''}`}>
                            {STATUS_LABELS[b.status]?.label || b.status}
                          </span>
                        </td>
                        <td className="actions-cell">
                          {b.status === 'CONFIRMED' && (
                            <button className="action-btn checkin" onClick={() => handleStatusChange(b.id, 'CHECKED_IN')}>
                              Check-in
                            </button>
                          )}
                          {b.status === 'CHECKED_IN' && (
                            <button className="action-btn checkout" onClick={() => handleStatusChange(b.id, 'CHECKED_OUT')}>
                              Check-out
                            </button>
                          )}
                          {(b.status === 'PENDING') && (
                            <>
                              <button className="action-btn confirm" onClick={() => handleStatusChange(b.id, 'CONFIRMED')}>
                                Xác nhận
                              </button>
                              <button className="action-btn cancel" onClick={() => handleStatusChange(b.id, 'CANCELLED')}>
                                Hủy
                              </button>
                            </>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </>
        )}
      </main>
    </div>
  );
}
