package ngoctamhotel.ngoctamhotel.entity;


import ngoctamhotel.ngoctamhotel.entity.enums.RoomBookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class RoomBooking {
    // Trước: @GeneratedValue không chạy khi JdbcTemplate INSERT.
    // Sau: repository tạo UUID và gán lại vào object sau khi lưu.
    private UUID id;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int adults;
    private int children;
    private BigDecimal totalPricce;
    private RoomBookingStatus roomBookingStatus;
    private LocalDateTime createdAt;
    private String note;

    // Trước: tham chiếu hồ sơ khách; sau: lưu thông tin tại thời điểm đặt đơn.
    private String fullName;
    private String phone;
    private String email;
    private LocalDate dateOfBirth;

    private UUID roomCategoryId;

    // Trước: @ManyToOne không thể đặt trên List<Room>.
    // Sau: danh sách phòng được lưu trong bảng booking_rooms bằng JdbcTemplate.
    private List<Room> rooms;

    private List<Payment> payment;

    public RoomBooking(UUID id, LocalDate checkInDate, LocalDate checkOutDate, int adults, int children, BigDecimal totalPricce, RoomBookingStatus roomBookingStatus, LocalDateTime createdAt, String note, String fullName, String phone, String email, LocalDate dateOfBirth, UUID roomCategoryId, List<Room> rooms) {
        this.id = id;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.adults = adults;
        this.children = children;
        this.totalPricce = totalPricce;
        this.roomBookingStatus = roomBookingStatus;
        this.createdAt = createdAt;
        this.note = note;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.roomCategoryId = roomCategoryId;
        this.rooms = rooms;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public int getAdults() { return adults; }
    public void setAdults(int adults) { this.adults = adults; }
    public int getChildren() { return children; }
    public void setChildren(int children) { this.children = children; }
    public BigDecimal getTotalPricce() { return totalPricce; }
    public void setTotalPricce(BigDecimal totalPricce) { this.totalPricce = totalPricce; }
    public RoomBookingStatus getRoomBookingStatus() { return roomBookingStatus; }
    public void setRoomBookingStatus(RoomBookingStatus roomBookingStatus) { this.roomBookingStatus = roomBookingStatus; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Payment> getPayment() {
        return payment;
    }

    public void setPayment(List<Payment> payment) {
        this.payment = payment;
    }

    public UUID getRoomCategoryId() {
        return roomCategoryId;
    }

    public void setRoomCategoryId(UUID roomCategoryId) {
        this.roomCategoryId = roomCategoryId;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }


}
