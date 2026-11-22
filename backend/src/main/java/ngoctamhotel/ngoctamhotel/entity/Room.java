package ngoctamhotel.ngoctamhotel.entity;

import ngoctamhotel.ngoctamhotel.entity.enums.RoomStatus;

import java.util.UUID;
// Trước: @Entity/@Column trong khi RoomRepository ánh xạ cột bằng JdbcTemplate.
// Sau: POJO, category_id vẫn được repository đọc vào UUID roomCategory.
public class Room {
    private UUID id;
    private String roomNumber;
    private RoomStatus roomStatus;


    // Khóa ngoại category_id là UUID, không phải một RoomCategory được JPA tải tự động.
    private UUID roomCategory;

    public Room() {}

    public Room(UUID id, String roomNumber, RoomStatus roomStatus, UUID roomCategory) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomStatus = roomStatus;
        this.roomCategory = roomCategory;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomStatus getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(RoomStatus roomStatus) {
        this.roomStatus = roomStatus;
    }

    public UUID getRoomCategory() {
        return roomCategory;
    }

    public void setRoomCategory(UUID roomCategory) {
        this.roomCategory = roomCategory;
    }
}
