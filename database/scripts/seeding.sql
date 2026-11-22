USE ngoc_tam_hotel;

-- Giá và tiện nghi theo Doc/RoomType.txt. Mỗi giường tính tối đa 2 người lớn
-- cùng 1 trẻ dưới 10 tuổi; max_guests chỉ lưu tổng người, chưa kiểm tra tuổi.
-- Upsert để chạy lại seed sẽ cập nhật mô tả/giá thay vì giữ dữ liệu mẫu cũ.
INSERT INTO room_categories
    (id, category_name, price, description, max_guests, amenities, room_type)
VALUES
    ('00000000-0000-4000-8000-000000000101', 'Phòng Đơn', 350000,
     'Phòng 1 giường gọn gàng, phù hợp tối đa 2 người lớn và 1 trẻ dưới 10 tuổi. Có 2 chai nước lọc miễn phí.', 3,
     'Bàn ăn uống,Phòng tắm nước nóng lạnh,2 chai nước lọc miễn phí,Mượn máy sấy tóc tại lễ tân,Mượn ấm đun nước tại lễ tân', 'SINGLE'),
    ('00000000-0000-4000-8000-000000000102', 'Phòng Đôi Thường', 550000,
     'Phòng 2 giường thoải mái cho gia đình hoặc nhóm bạn. Có 4 chai nước lọc miễn phí.', 6,
     'Bàn ăn uống,Phòng tắm nước nóng lạnh,4 chai nước lọc miễn phí,Mượn máy sấy tóc tại lễ tân,Mượn ấm đun nước tại lễ tân', 'DOUBLE_CITY_VIEW'),
    ('00000000-0000-4000-8000-000000000103', 'Phòng Đôi View Biển', 600000,
     'Phòng 2 giường hướng biển, thích hợp nghỉ ngơi cùng gia đình. Có 4 chai nước lọc miễn phí.', 6,
     'View biển,Bàn ăn uống,Phòng tắm nước nóng lạnh,4 chai nước lọc miễn phí,Mượn máy sấy tóc tại lễ tân,Mượn ấm đun nước tại lễ tân', 'DOUBLE_SEA_VIEW'),
    ('00000000-0000-4000-8000-000000000104', 'Phòng Ba', 750000,
     'Phòng 3 giường rộng rãi cho nhóm đông người. Có 6 chai nước lọc miễn phí.', 9,
     '3 giường,Bàn ăn uống,Phòng tắm nước nóng lạnh,6 chai nước lọc miễn phí,Mượn máy sấy tóc tại lễ tân,Mượn ấm đun nước tại lễ tân', 'TRIPLE')
ON DUPLICATE KEY UPDATE
    category_name = VALUES(category_name),
    price = VALUES(price),
    description = VALUES(description),
    max_guests = VALUES(max_guests),
    amenities = VALUES(amenities),
    room_type = VALUES(room_type);

INSERT IGNORE INTO rooms (id, room_number, room_status, category_id) VALUES
    ('00000000-0000-4000-8000-000000000201', 101, 'AVAILABLE', '00000000-0000-4000-8000-000000000101'),
    ('00000000-0000-4000-8000-000000000202', 102, 'AVAILABLE', '00000000-0000-4000-8000-000000000101'),
    ('00000000-0000-4000-8000-000000000203', 201, 'AVAILABLE', '00000000-0000-4000-8000-000000000102'),
    ('00000000-0000-4000-8000-000000000204', 202, 'AVAILABLE', '00000000-0000-4000-8000-000000000102'),
    ('00000000-0000-4000-8000-000000000205', 301, 'AVAILABLE', '00000000-0000-4000-8000-000000000103'),
    ('00000000-0000-4000-8000-000000000206', 302, 'AVAILABLE', '00000000-0000-4000-8000-000000000103'),
    ('00000000-0000-4000-8000-000000000207', 401, 'AVAILABLE', '00000000-0000-4000-8000-000000000104'),
    ('00000000-0000-4000-8000-000000000208', 402, 'AVAILABLE', '00000000-0000-4000-8000-000000000104');

INSERT IGNORE INTO room_bookings
    (id, check_in_date, check_out_date, adults, children, total_pricce,
     room_booking_status, created_at, note, full_name, phone, email, date_of_birth, category_id)
VALUES
    ('00000000-0000-4000-8000-000000000401', '2026-11-15', '2026-11-17',
     1, 0, 700000, 'CONFIRMED', '2026-11-07 10:20:00', NULL,
     'Nguyễn Văn An', '0901234567', 'an@example.com', '1995-05-12', '00000000-0000-4000-8000-000000000101');

-- Trước: phòng mẫu nằm ngay trong room_bookings.room_id.
-- Sau: một booking có thể chứa nhiều room_id trong booking_rooms.
INSERT IGNORE INTO booking_rooms (booking_id, room_id) VALUES
    ('00000000-0000-4000-8000-000000000401', '00000000-0000-4000-8000-000000000201');

-- Trước: seed ghi status/description không có trong bảng payments và dùng TIME.
-- Sau: dùng đúng các cột hiện tại; tiền cọc mẫu vẫn là 100.000đ.
INSERT INTO payments
    (id, booking_id, payment_status, method, amount, created_at, paid_at)
VALUES
    ('00000000-0000-4000-8000-000000000501',
     '00000000-0000-4000-8000-000000000401', 'PAID',
     'BANK_TRANSFER', 100000, '2026-11-07 10:30:00', '2026-11-07 10:35:00')
ON DUPLICATE KEY UPDATE
    method = VALUES(method),
    amount = VALUES(amount),
    payment_status = VALUES(payment_status),
    created_at = VALUES(created_at),
    paid_at = VALUES(paid_at);

-- Giá xe chưa được cung cấp: 0 chỉ là giá chưa xác nhận, FE không hiển thị là miễn phí.
INSERT IGNORE INTO bikes (id, name, bike_status, price) VALUES
    ('00000000-0000-4000-8000-000000000601', 'Xe máy 01', 'AVAILABLE', 0);
