package ngoctamhotel.ngoctamhotel.entity;

import ngoctamhotel.ngoctamhotel.entity.enums.BikeStatus;

import java.util.UUID;
import java.math.BigDecimal;
// Trước: JPA annotation không tham gia các query trong BikeRepository.
// Sau: model thuần Java; bike_status được repository đổi từ chuỗi sang enum.
public class Bike {
    private UUID id;
    private String name;
    private BikeStatus bikeStatus;
    private BigDecimal price;

    public Bike(UUID id, String name, BikeStatus bikeStatus, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.bikeStatus = bikeStatus;
        this.price = price;
    }

    public Bike() {

    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public BikeStatus getBikeStatus() { return bikeStatus; }
    public BigDecimal getPrice() { return price; }
}
