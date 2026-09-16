package ngoctamhotel.ngoctamhotel.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ngoctamhotel.ngoctamhotel.dto.response.RoomTypeResponse;
import ngoctamhotel.ngoctamhotel.service.RoomService;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<RoomTypeResponse> listRoomTypes() {
        return roomService.getAllRoomTypes();
    }

    @GetMapping("/{id}")
    public RoomTypeResponse getRoomType(@PathVariable Long id) {
        return roomService.getRoomTypeById(id);
    }
}
