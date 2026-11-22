USE ngoc_tam_hotel;

-- Chạy một lần SAU 20261112_align_booking_customer_payment.sql trên DB cũ.
-- Sao lưu trước khi chạy: MySQL DDL tự commit. Không chạy trên DB mới tạo từ schema.sql.
-- Trước: các đơn tham chiếu hồ sơ khách chung.
-- Sau: mỗi đơn giữ thông tin khách tại thời điểm chuyển đổi, độc lập với đơn khác.
ALTER TABLE room_bookings
    ADD COLUMN full_name VARCHAR(150) NULL,
    ADD COLUMN phone VARCHAR(30) NULL,
    ADD COLUMN email VARCHAR(255) NULL,
    ADD COLUMN date_of_birth DATE NULL;
ALTER TABLE bike_bookings
    ADD COLUMN full_name VARCHAR(150) NULL,
    ADD COLUMN phone VARCHAR(30) NULL,
    ADD COLUMN email VARCHAR(255) NULL,
    ADD COLUMN room_booking_id CHAR(36) NULL;

UPDATE room_bookings b JOIN customer_information c ON c.id = b.customer_id
SET b.full_name = c.full_name, b.phone = c.phone, b.email = c.email,
    b.date_of_birth = c.date_of_birth;
UPDATE bike_bookings b JOIN customer_information c ON c.id = b.customer_id
SET b.full_name = c.full_name, b.phone = c.phone, b.email = c.email;

-- Dừng tại đây nếu còn NULL: phải đối chiếu và bổ sung trước khi chạy phần bên dưới.
SELECT id FROM room_bookings WHERE full_name IS NULL OR phone IS NULL;
SELECT id FROM bike_bookings WHERE full_name IS NULL OR phone IS NULL;

ALTER TABLE room_bookings
    MODIFY full_name VARCHAR(150) NOT NULL,
    MODIFY phone VARCHAR(30) NOT NULL,
    ADD INDEX idx_room_bookings_contact (phone, created_at);
ALTER TABLE bike_bookings
    MODIFY full_name VARCHAR(150) NOT NULL,
    MODIFY phone VARCHAR(30) NOT NULL,
    ADD INDEX idx_bike_bookings_contact (phone, created_at),
    ADD CONSTRAINT fk_bike_bookings_room_booking FOREIGN KEY (room_booking_id) REFERENCES room_bookings(id);

ALTER TABLE room_bookings DROP FOREIGN KEY fk_room_bookings_customer, DROP COLUMN customer_id;
ALTER TABLE bike_bookings DROP FOREIGN KEY fk_bike_bookings_customer, DROP COLUMN customer_id;
DROP TABLE customer_information;

-- Giữ room_id cũ trong DB đã có dữ liệu: không thể suy ra duy nhất một booking
-- từ mã phòng. Code mới chỉ dùng room_booking_id; dữ liệu cũ cần đối chiếu thủ công.
