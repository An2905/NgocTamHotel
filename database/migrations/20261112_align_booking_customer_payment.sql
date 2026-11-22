USE ngoc_tam_hotel;

-- Chạy một lần trên DB cũ sau các migration 20261108; sao lưu DB trước khi chạy.
-- Trước: view room_categorries chỉ bù cho query sai chính tả đã được sửa.
-- Sau: bỏ view vì repository hiện đọc thẳng room_categories.
DROP VIEW IF EXISTS room_categorries;

-- Trước: ngày sinh có cả giờ, booking chỉ lưu ngày tạo, phone không có index.
-- Sau: kiểu dữ liệu khớp với ý nghĩa từng trường và hỗ trợ tìm khách cũ theo phone.
-- Index không tự gộp bản ghi khách trùng; repository/service phải quyết định cách ghép.
ALTER TABLE customer_information
    MODIFY COLUMN date_of_birth DATE NULL,
    ADD INDEX idx_customer_information_phone (phone);

-- DATE cũ sẽ thành 00:00:00; không thể khôi phục giờ tạo đã mất trước migration.
ALTER TABLE room_bookings
    MODIFY COLUMN created_at DATETIME NOT NULL;

ALTER TABLE bike_bookings
    MODIFY COLUMN created_at DATETIME NOT NULL;

-- Trước: payments chỉ lưu giờ và chặn payment thứ hai cho cùng booking.
-- Sau: giữ timestamp đầy đủ, dữ liệu PayOS và nhiều lần thử thanh toán.
-- Dùng ngày tạo booking để khôi phục phần ngày cho các TIME payment cũ.
ALTER TABLE payments
    ADD COLUMN payos_order_code BIGINT NULL,
    ADD COLUMN payos_payment_link_id VARCHAR(255) NULL,
    ADD COLUMN checkout_url TEXT NULL,
    ADD COLUMN created_at_datetime DATETIME NULL,
    ADD COLUMN paid_at_datetime DATETIME NULL;

UPDATE payments p
JOIN room_bookings b ON b.id = p.booking_id
SET p.created_at_datetime = CASE
        WHEN p.created_at IS NULL THEN NULL
        ELSE TIMESTAMP(DATE(b.created_at), p.created_at)
    END,
    p.paid_at_datetime = CASE
        WHEN p.paid_at IS NULL THEN NULL
        ELSE TIMESTAMP(DATE(b.created_at), p.paid_at)
    END;

ALTER TABLE payments
    DROP COLUMN created_at,
    DROP COLUMN paid_at,
    CHANGE COLUMN created_at_datetime created_at DATETIME NULL,
    CHANGE COLUMN paid_at_datetime paid_at DATETIME NULL,
    ADD UNIQUE INDEX uq_payments_payos_order_code (payos_order_code);

-- Giữ index cho FK trước khi bỏ ràng buộc một payment/booking.
ALTER TABLE payments ADD INDEX idx_payments_booking (booking_id);
ALTER TABLE payments DROP INDEX booking_id;

-- paid_at cũ không chứa ngày: ngày gán ở trên là ngày tạo booking, cần đối chiếu
-- với chứng từ thanh toán nếu giao dịch thực tế xảy ra vào ngày khác.
