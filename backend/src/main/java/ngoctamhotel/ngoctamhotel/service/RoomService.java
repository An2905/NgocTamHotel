package ngoctamhotel.ngoctamhotel.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import ngoctamhotel.ngoctamhotel.dto.response.RoomTypeResponse;
import ngoctamhotel.ngoctamhotel.model.RoomType;
import ngoctamhotel.ngoctamhotel.repository.RoomRepository;
import ngoctamhotel.ngoctamhotel.repository.RoomTypeRepository;

@Service
public class RoomService {
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;

    public RoomService(RoomTypeRepository roomTypeRepository, RoomRepository roomRepository) {
        this.roomTypeRepository = roomTypeRepository;
        this.roomRepository = roomRepository;
    }

    public List<RoomTypeResponse> getAllRoomTypes() {
        return roomTypeRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public RoomTypeResponse getRoomTypeById(Long id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại phòng"));
        return toResponse(roomType);
    }

    private RoomTypeResponse toResponse(RoomType rt) {
        List<String> amenitiesList = rt.amenities() != null && !rt.amenities().isBlank()
                ? Arrays.asList(rt.amenities().split(","))
                : Collections.emptyList();
        int total = roomRepository.countByRoomTypeId(rt.id());
        int available = roomRepository.countAvailableByRoomTypeId(rt.id());
        return new RoomTypeResponse(
                rt.id(), rt.name(), rt.description(), rt.basePrice(),
                rt.maxAdults(), rt.maxChildren(), amenitiesList,
                rt.viewType(), rt.imageUrl(), total, available);
    }
}
