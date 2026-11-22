USE ngoc_tam_hotel;
-- Chạy một lần sau migration alignment. Sao lưu trước khi chạy.
-- Mã PayOS cũ được giữ nguyên; các payment chưa có mã được cấp số sau mã lớn nhất.
SET @next_payos_code = (SELECT COALESCE(MAX(payos_order_code), 0) FROM payments);
UPDATE payments SET payos_order_code = (@next_payos_code := @next_payos_code + 1)
WHERE payos_order_code IS NULL ORDER BY id;
ALTER TABLE payments MODIFY payos_order_code BIGINT NOT NULL AUTO_INCREMENT;
-- UNIQUE index đã có từ schema/alignment. Không chạy trên DB mới dùng schema mới.
