package ngoctamhotel.ngoctamhotel.entity;

import ngoctamhotel.ngoctamhotel.entity.enums.RoomType;

import java.util.UUID;
import java.math.BigDecimal;

// Trước: @Entity/@Enumerated khiến model trông như được JPA quản lý.
// Sau: JdbcTemplate đọc room_type dạng chuỗi và ánh xạ RoomType trong repository.
public class RoomCategory {
    private UUID id;
    private String categoryName;
    private BigDecimal price;
    private String description;
    private int maxGuests;
    private String amenities;
    private RoomType roomType;

    public RoomCategory() {
    }

    public RoomCategory(UUID id, String categoryName, BigDecimal price, String description, int maxGuests, String amenities, RoomType roomType) {
        this.id = id;
        this.categoryName = categoryName;
        this.price = price;
        this.description = description;
        this.maxGuests = maxGuests;
        this.amenities = amenities;
        // Trước: constructor nhận roomType nhưng bỏ qua, FE không biết loại phòng để kiểm tra.
        // Sau: lưu roomType để response /rooms trả enum đúng với category.
        this.roomType = roomType;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxGuests() {
        return maxGuests;
    }

    public void setMaxGuests(int maxGuests) {
        this.maxGuests = maxGuests;
    }

    public String getAmenities() {
        return amenities;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

}
