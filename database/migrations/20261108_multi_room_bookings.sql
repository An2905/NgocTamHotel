USE ngoc_tam_hotel;

-- Trước: mỗi booking giữ một room_id; dữ liệu cũ cần được giữ lại khi nâng cấp.
-- Sau: category_id nằm ở booking, còn danh sách phòng nằm ở booking_rooms.
ALTER TABLE room_bookings ADD COLUMN category_id CHAR(36) NULL AFTER customer_id;

UPDATE room_bookings b
JOIN rooms r ON r.id = b.room_id
SET b.category_id = r.category_id;

ALTER TABLE room_bookings
    MODIFY COLUMN category_id CHAR(36) NOT NULL,
    ADD CONSTRAINT fk_room_bookings_category
        FOREIGN KEY (category_id) REFERENCES room_categories (id);

CREATE TABLE booking_rooms (
    booking_id CHAR(36) NOT NULL,
    room_id CHAR(36) NOT NULL,
    PRIMARY KEY (booking_id, room_id),
    CONSTRAINT fk_booking_rooms_booking FOREIGN KEY (booking_id) REFERENCES room_bookings (id),
    CONSTRAINT fk_booking_rooms_room FOREIGN KEY (room_id) REFERENCES rooms (id),
    INDEX idx_booking_rooms_room (room_id, booking_id)
);

INSERT INTO booking_rooms (booking_id, room_id)
SELECT id, room_id FROM room_bookings;

ALTER TABLE room_bookings
    DROP FOREIGN KEY fk_room_bookings_room,
    DROP INDEX idx_room_bookings_dates,
    DROP COLUMN room_id,
    ADD INDEX idx_room_bookings_dates (check_in_date, check_out_date, room_booking_status);
