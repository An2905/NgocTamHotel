-- Chạy trên database được chọn trong datasource. Đồng bộ với database/scripts/schema.sql.
CREATE TABLE IF NOT EXISTS admin (
    name VARCHAR(100) PRIMARY KEY,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS room_categories (
    id CHAR(36) PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    price DECIMAL(12, 2) NOT NULL,
    description TEXT,
    max_guests INT NOT NULL,
    amenities TEXT,
    room_type VARCHAR(30) NOT NULL,
    CONSTRAINT chk_room_category_price CHECK (price >= 0),
    CONSTRAINT chk_room_category_guests CHECK (max_guests > 0),
    CONSTRAINT chk_room_category_type CHECK (room_type IN
        ('SINGLE', 'DOUBLE_SEA_VIEW', 'DOUBLE_CITY_VIEW', 'TRIPLE'))
);

CREATE TABLE IF NOT EXISTS rooms (
    id CHAR(36) PRIMARY KEY,
    room_number VARCHAR(10) NOT NULL UNIQUE,
    room_status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    category_id CHAR(36) NOT NULL,
    CONSTRAINT fk_rooms_category FOREIGN KEY (category_id) REFERENCES room_categories (id),
    CONSTRAINT chk_rooms_status CHECK (room_status IN
        ('AVAILABLE', 'CLEANING', 'OCCUPIED', 'MAINTENANCE', 'INACTIVE'))
);

CREATE TABLE IF NOT EXISTS room_bookings (
    id CHAR(36) PRIMARY KEY,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    adults INT NOT NULL,
    children INT NOT NULL DEFAULT 0,
    total_pricce DECIMAL(12, 2) NOT NULL DEFAULT 0,
    room_booking_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    -- Trước: DATE làm mất giờ tạo booking; sau: DATETIME lưu đủ LocalDateTime.
    created_at DATETIME NOT NULL,
    payment_deadline DATETIME,
    note TEXT,
    -- Thông tin khách tại thời điểm đặt, không cần tài khoản/hồ sơ khách chung.
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    email VARCHAR(255),
    category_id CHAR(36) NOT NULL,
    date_of_birth DATE,
    INDEX idx_room_bookings_contact (phone, created_at),
    CONSTRAINT fk_room_bookings_category FOREIGN KEY (category_id) REFERENCES room_categories (id),
    CONSTRAINT chk_room_bookings_dates CHECK (check_out_date > check_in_date),
    CONSTRAINT chk_room_bookings_guests CHECK (adults > 0 AND children >= 0),
    CONSTRAINT chk_room_bookings_total CHECK (total_pricce >= 0),
    CONSTRAINT chk_room_bookings_status CHECK (room_booking_status IN
        ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED')),
    INDEX idx_room_bookings_dates (check_in_date, check_out_date, room_booking_status)
);

-- Trước: room_bookings.room_id chỉ lưu được một phòng trong một booking.
-- Sau: mỗi dòng ở booking_rooms gắn một phòng vào cùng booking_id.
CREATE TABLE IF NOT EXISTS booking_rooms (
    booking_id CHAR(36) NOT NULL,
    room_id CHAR(36) NOT NULL,
    PRIMARY KEY (booking_id, room_id),
    CONSTRAINT fk_booking_rooms_booking FOREIGN KEY (booking_id) REFERENCES room_bookings (id),
    CONSTRAINT fk_booking_rooms_room FOREIGN KEY (room_id) REFERENCES rooms (id),
    INDEX idx_booking_rooms_room (room_id, booking_id)
);

CREATE TABLE IF NOT EXISTS room_holds (
    id CHAR(36) PRIMARY KEY,
    category_id CHAR(36) NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    number_of_rooms INT NOT NULL,
    owner_key CHAR(36) NOT NULL,
    expires_at DATETIME NOT NULL,
    status VARCHAR(12) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_room_holds_category FOREIGN KEY (category_id) REFERENCES room_categories(id),
    INDEX idx_room_holds_dates (category_id, check_in_date, check_out_date, status, expires_at)
);

CREATE TABLE IF NOT EXISTS room_hold_rooms (
    hold_id CHAR(36) NOT NULL,
    room_id CHAR(36) NOT NULL,
    PRIMARY KEY (hold_id, room_id),
    CONSTRAINT fk_room_hold_rooms_hold FOREIGN KEY (hold_id) REFERENCES room_holds(id),
    CONSTRAINT fk_room_hold_rooms_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    INDEX idx_room_hold_rooms_room (room_id, hold_id)
);

CREATE TABLE IF NOT EXISTS bikes (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    bike_status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT chk_bikes_status CHECK (bike_status IN ('IN_USE', 'DAMAGED', 'AVAILABLE'))
);

CREATE TABLE IF NOT EXISTS bike_bookings (
    id CHAR(36) PRIMARY KEY,
    bike_booking_status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
    -- Trước: chỉ biết ngày tạo; sau: giữ cả giờ tạo đơn thuê xe.
    created_at DATETIME NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    bike_id CHAR(36) NOT NULL,
    -- Thông tin khách tại thời điểm đặt, không cần tài khoản/hồ sơ khách chung.
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    email VARCHAR(255),
    room_booking_id CHAR(36),
    CONSTRAINT fk_bike_bookings_bike FOREIGN KEY (bike_id) REFERENCES bikes (id),
    INDEX idx_bike_bookings_contact (phone, created_at),
    CONSTRAINT fk_bike_bookings_room_booking FOREIGN KEY (room_booking_id) REFERENCES room_bookings (id),
    CONSTRAINT chk_bike_bookings_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_bike_bookings_status CHECK (bike_booking_status IN
        ('IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE TABLE IF NOT EXISTS payments (
    id CHAR(36) PRIMARY KEY,
    -- Trước: UNIQUE(booking_id) chặn lần thử thanh toán mới của cùng booking.
    -- Sau: mỗi lần thử có payment id riêng; các lần thử vẫn cùng booking_id.
    booking_id CHAR(36) NOT NULL,
    payment_status VARCHAR(30) NOT NULL DEFAULT 'UNPAID',
    method VARCHAR(30),
    amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    -- Trước: chỉ TIME và thiếu mã/link PayOS; sau: lưu mốc thời gian đầy đủ
    -- cùng mã đối soát và checkout URL cho từng lần thử thanh toán.
    payos_order_code BIGINT NOT NULL AUTO_INCREMENT UNIQUE,
    payos_payment_link_id VARCHAR(255),
    checkout_url TEXT,
    created_at DATETIME,
    paid_at DATETIME,
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES room_bookings (id),
    CONSTRAINT chk_payments_amount CHECK (amount >= 0),
    CONSTRAINT chk_payments_status CHECK (payment_status IN
        ('UNPAID', 'PAID', 'CANCELLED', 'PROCESSING')),
    INDEX idx_payments_booking (booking_id)
);
