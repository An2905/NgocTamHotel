package ngoctamhotel.ngoctamhotel.controller;

import ngoctamhotel.ngoctamhotel.dto.request.CheckRoomRequest;
import ngoctamhotel.ngoctamhotel.dto.request.RoomBookingRequest;
import ngoctamhotel.ngoctamhotel.dto.request.RoomHoldRequest;
import ngoctamhotel.ngoctamhotel.dto.response.RoomHoldResponse;
import ngoctamhotel.ngoctamhotel.dto.response.RoomBookingResponse;
import ngoctamhotel.ngoctamhotel.entity.RoomCategory;
import ngoctamhotel.ngoctamhotel.entity.enums.RoomType;
import ngoctamhotel.ngoctamhotel.service.RoomService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class RoomController {
    private final RoomService service;

    public RoomController(RoomService service) {
        this.service = service;
    }

    @GetMapping("/rooms")
    public List<RoomCategory> listRooms() {
        return service.getAllRoom();
    }

    @GetMapping("/check-rooms-booking")
    // Trước: GET dùng @RequestBody, trình duyệt không gửi được body cho GET.
    // Sau: FE gửi các trường CheckRoomRequest dưới dạng query parameters.
    public int checkRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDay,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDay,
            @RequestParam RoomType roomType,
            @RequestParam UUID roomCateId) {
       return service.checkRoomByRequest(new CheckRoomRequest(checkInDay, checkOutDay, roomType, roomCateId));
    }

    @PostMapping("/new-booking")
    public RoomBookingResponse createRoomBooking(@RequestBody RoomBookingRequest roomBookingRequest) {
        return service.createBooking(roomBookingRequest);
    }

    @PostMapping("/room-holds")
    public RoomHoldResponse holdRooms(@RequestBody RoomHoldRequest request) {
        return service.createHold(request);
    }

    @DeleteMapping("/room-holds/{holdId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void releaseHold(@PathVariable UUID holdId, @RequestParam UUID ownerKey) {
        service.releaseHold(holdId, ownerKey);
    }
}
