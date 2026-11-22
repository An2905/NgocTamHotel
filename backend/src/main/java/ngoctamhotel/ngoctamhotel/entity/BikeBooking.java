package ngoctamhotel.ngoctamhotel.entity;

import ngoctamhotel.ngoctamhotel.entity.enums.BikeBookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// Mỗi đơn xe lưu thông tin khách; roomBookingId liên kết tùy chọn tới đơn phòng.
public class BikeBooking {
    private UUID id;
    private BikeBookingStatus bikeBookingStatus;
    private LocalDateTime createdAt;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID bikeId;
    // Thông tin liên hệ riêng của đơn thuê xe.
    private String fullName;
    private String phone;
    private String email;
    private UUID roomBookingId;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public BikeBookingStatus getBikeBookingStatus() { return bikeBookingStatus; }
    public void setBikeBookingStatus(BikeBookingStatus bikeBookingStatus) { this.bikeBookingStatus = bikeBookingStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public UUID getBikeId() { return bikeId; }
    public void setBikeId(UUID bikeId) { this.bikeId = bikeId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public UUID getRoomBookingId() { return roomBookingId; }
    public void setRoomBookingId(UUID roomBookingId) { this.roomBookingId = roomBookingId; }
}
