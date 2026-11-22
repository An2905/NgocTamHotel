ALTER TABLE room_bookings ADD COLUMN payment_deadline DATETIME NULL AFTER created_at;
UPDATE room_bookings SET payment_deadline = DATE_ADD(created_at, INTERVAL 24 HOUR)
WHERE room_booking_status = 'PENDING' AND payment_deadline IS NULL;

CREATE TABLE room_holds (
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
CREATE TABLE room_hold_rooms (
    hold_id CHAR(36) NOT NULL,
    room_id CHAR(36) NOT NULL,
    PRIMARY KEY (hold_id, room_id),
    CONSTRAINT fk_room_hold_rooms_hold FOREIGN KEY (hold_id) REFERENCES room_holds(id),
    CONSTRAINT fk_room_hold_rooms_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    INDEX idx_room_hold_rooms_room (room_id, hold_id)
);
