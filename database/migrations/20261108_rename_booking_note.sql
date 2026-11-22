USE ngoc_tam_hotel;

-- Trước: cột ghi chú trong room_bookings tên là mote.
-- Sau: đổi thành note, giữ nguyên dữ liệu cũ và cho phép NULL.
ALTER TABLE room_bookings RENAME COLUMN mote TO note;
