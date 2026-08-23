USE ngoc_tam_hotel;

INSERT INTO room_types (name, description, base_price, max_adults, max_children)
VALUES
    ('Standard', 'Comfortable room for short stays', 450000, 2, 1),
    ('Deluxe', 'Spacious room with upgraded amenities', 700000, 2, 2),
    ('Family', 'Large room suitable for families', 1000000, 4, 2);

INSERT INTO rooms (room_number, room_type_id, floor_number)
VALUES
    ('101', 1, 1),
    ('102', 1, 1),
    ('201', 2, 2),
    ('301', 3, 3);

