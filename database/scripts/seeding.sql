USE ngoc_tam_hotel;

-- Room types matching FR02 scope
INSERT INTO room_types (name, description, base_price, max_adults, max_children, amenities, view_type, image_url)
VALUES
    ('Phòng Đơn', 'Phòng đơn ấm cúng, phù hợp cho khách đi một mình hoặc công tác ngắn ngày. Trang bị đầy đủ tiện nghi cơ bản với không gian yên tĩnh, giúp bạn có giấc ngủ trọn vẹn.', 350000, 1, 1,
     'Wi-Fi miễn phí,Điều hòa,TV màn hình phẳng,Minibar,Phòng tắm riêng,Máy sấy tóc', 'standard',
     'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&w=900&q=80'),
    ('Phòng Đôi Thường', 'Phòng đôi rộng rãi với giường đôi lớn, thích hợp cho cặp đôi hoặc bạn bè. Thiết kế hiện đại, thoáng mát với đầy đủ tiện nghi cao cấp.', 550000, 2, 1,
     'Wi-Fi miễn phí,Điều hòa,TV màn hình phẳng,Minibar,Phòng tắm riêng,Máy sấy tóc,Bàn làm việc,Két an toàn', 'standard',
     'https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&w=900&q=80'),
    ('Phòng Đôi View Biển', 'Phòng đôi cao cấp với tầm nhìn hướng biển tuyệt đẹp. Thức dậy mỗi sáng với ánh nắng và gió biển, tận hưởng kỳ nghỉ đáng nhớ bên người thân yêu.', 600000, 2, 1,
     'Wi-Fi miễn phí,Điều hòa,TV màn hình phẳng,Minibar,Phòng tắm riêng,Máy sấy tóc,Ban công view biển,Két an toàn,Áo choàng tắm', 'sea_view',
     'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=900&q=80'),
    ('Phòng Ba', 'Phòng ba giường rộng rãi, lý tưởng cho nhóm bạn hoặc gia đình nhỏ. Không gian sinh hoạt chung thoải mái với đầy đủ tiện nghi cho mọi người.', 750000, 3, 2,
     'Wi-Fi miễn phí,Điều hòa,TV màn hình phẳng,Minibar,Phòng tắm riêng,Máy sấy tóc,Két an toàn,Bàn làm việc,Tủ quần áo lớn', 'standard',
     'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=900&q=80');

-- 32 rooms across 4 floors
INSERT INTO rooms (room_number, room_type_id, floor_number) VALUES
    -- Floor 1: Phòng Đơn (8 rooms)
    ('101', 1, 1), ('102', 1, 1), ('103', 1, 1), ('104', 1, 1),
    ('105', 1, 1), ('106', 1, 1), ('107', 1, 1), ('108', 1, 1),
    -- Floor 2: Phòng Đôi Thường (8 rooms)
    ('201', 2, 2), ('202', 2, 2), ('203', 2, 2), ('204', 2, 2),
    ('205', 2, 2), ('206', 2, 2), ('207', 2, 2), ('208', 2, 2),
    -- Floor 3: Phòng Đôi View Biển (8 rooms)
    ('301', 3, 3), ('302', 3, 3), ('303', 3, 3), ('304', 3, 3),
    ('305', 3, 3), ('306', 3, 3), ('307', 3, 3), ('308', 3, 3),
    -- Floor 4: Phòng Ba (8 rooms)
    ('401', 4, 4), ('402', 4, 4), ('403', 4, 4), ('404', 4, 4),
    ('405', 4, 4), ('406', 4, 4), ('407', 4, 4), ('408', 4, 4);

-- Sample guests for demo bookings
INSERT INTO guests (full_name, phone, email, identity_number) VALUES
    ('Nguyễn Văn An', '0901234567', 'nguyenvanan@gmail.com', '079123456789'),
    ('Trần Thị Bình', '0912345678', 'tranthibinh@gmail.com', '079234567890'),
    ('Lê Hoàng Cường', '0923456789', 'lehoangcuong@gmail.com', '079345678901'),
    ('Phạm Minh Đức', '0934567890', 'phamminhduc@gmail.com', '079456789012'),
    ('Võ Thị Hoa', '0945678901', 'vothihoa@gmail.com', '079567890123');

-- Sample bookings for admin dashboard demo
INSERT INTO bookings (booking_code, guest_id, room_id, check_in_date, check_out_date, adults, children, total_amount, status) VALUES
    ('BK-2026-0001', 1, 1, '2026-09-01', '2026-09-03', 1, 0, 700000, 'CHECKED_OUT'),
    ('BK-2026-0002', 2, 5, '2026-09-05', '2026-09-08', 2, 0, 1650000, 'CHECKED_OUT'),
    ('BK-2026-0003', 3, 9, '2026-09-08', '2026-09-10', 2, 1, 1200000, 'CHECKED_IN'),
    ('BK-2026-0004', 4, 17, '2026-09-10', '2026-09-13', 2, 0, 1800000, 'CONFIRMED'),
    ('BK-2026-0005', 5, 25, '2026-09-12', '2026-09-15', 3, 1, 2250000, 'PENDING');

-- Sample payments
INSERT INTO payments (booking_id, method, amount, status, transaction_ref, paid_at) VALUES
    (1, 'VNPAY', 700000, 'COMPLETED', 'VNP-20260901-001', '2026-09-01 10:30:00'),
    (2, 'BANK_TRANSFER', 1650000, 'COMPLETED', 'BT-20260905-001', '2026-09-05 14:15:00'),
    (3, 'VNPAY', 1200000, 'COMPLETED', 'VNP-20260908-001', '2026-09-08 09:45:00');
