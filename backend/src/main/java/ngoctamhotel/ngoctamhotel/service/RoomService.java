package ngoctamhotel.ngoctamhotel.service;

import ngoctamhotel.ngoctamhotel.dto.request.CheckRoomRequest;
import ngoctamhotel.ngoctamhotel.dto.request.RoomBookingRequest;
import ngoctamhotel.ngoctamhotel.dto.request.RoomHoldRequest;
import ngoctamhotel.ngoctamhotel.dto.response.RoomBookingResponse;
import ngoctamhotel.ngoctamhotel.dto.response.RoomHoldResponse;
import ngoctamhotel.ngoctamhotel.entity.Room;
import ngoctamhotel.ngoctamhotel.entity.RoomBooking;
import ngoctamhotel.ngoctamhotel.entity.RoomCategory;
import ngoctamhotel.ngoctamhotel.entity.enums.RoomBookingStatus;
import ngoctamhotel.ngoctamhotel.repository.RoomRepository;
import ngoctamhotel.ngoctamhotel.repository.RoomHoldRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class RoomService {
    private final RoomRepository repo;
    private final RoomHoldRepository holds;

    public RoomService(RoomRepository repo, RoomHoldRepository holds) {
        this.repo = repo;
        this.holds = holds;
    }

    public List<RoomCategory> getAllRoom() {
        return repo.findAllRoomCategories();
    }

    @Transactional
    public RoomBookingResponse createBooking(RoomBookingRequest roomBooking) {
        if (roomBooking == null || roomBooking.holdId() == null || roomBooking.ownerKey() == null || roomBooking.categoryId() == null || roomBooking.checkInDate() == null
                || roomBooking.checkOutDate() == null || roomBooking.numberOfRooms() < 1
                || roomBooking.adults() < 1 || roomBooking.children() < 0
                || roomBooking.fullName() == null || roomBooking.fullName().isBlank()
                || roomBooking.phone() == null || roomBooking.phone().isBlank()
                || !roomBooking.checkOutDate().isAfter(roomBooking.checkInDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin đặt phòng không hợp lệ");
        }

        holds.lockCategory(roomBooking.categoryId());
        var hold = holds.findForUpdate(roomBooking.holdId());
        if (hold == null || !"ACTIVE".equals(hold.status()) ||
                !hold.expiresAt().isAfter(LocalDateTime.now()) ||
                !hold.categoryId().equals(roomBooking.categoryId()) ||
                !hold.checkInDate().equals(roomBooking.checkInDate()) ||
                !hold.checkOutDate().equals(roomBooking.checkOutDate()) ||
                hold.numberOfRooms() != roomBooking.numberOfRooms()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Lượt giữ phòng đã hết hạn hoặc không khớp; vui lòng kiểm tra phòng lại");
        }
        if (!hold.ownerKey().equals(roomBooking.ownerKey())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lượt giữ phòng thuộc phiên khác");
        }
        List<Room> rooms = holds.rooms(roomBooking.holdId());
        if (rooms.size() != hold.numberOfRooms()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Lượt giữ phòng không hợp lệ");
        if (roomBooking.adults() + roomBooking.children() >
                repo.findCategoryMaxGuests(roomBooking.categoryId()) * rooms.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số khách vượt quá sức chứa phòng");
        }

        // Trước: tin totalPrice và createdAt do FE gửi lên.
        // Sau: lấy giá từ DB, tính số đêm và tạo thời điểm ở BE.
        BigDecimal pricePerNight = repo.findCategoryPrice(roomBooking.categoryId());
        long nights = ChronoUnit.DAYS.between(roomBooking.checkInDate(), roomBooking.checkOutDate());
        BigDecimal totalPrice = pricePerNight
                .multiply(BigDecimal.valueOf(nights))
                .multiply(BigDecimal.valueOf(rooms.size()));
        // Mỗi booking lưu thông tin khách vừa nhập, không tra cứu hồ sơ cũ.
        RoomBooking newBooking = new RoomBooking(
                null, roomBooking.checkInDate(), roomBooking.checkOutDate(),
                roomBooking.adults(), roomBooking.children(), totalPrice,
                RoomBookingStatus.PENDING, LocalDateTime.now(), roomBooking.note(),
                roomBooking.fullName().trim(), roomBooking.phone().trim(),
                roomBooking.email() == null || roomBooking.email().isBlank() ? null : roomBooking.email().trim(),
                roomBooking.dateOfBirth(), roomBooking.categoryId(), rooms);

        // Trước: save(request) sai kiểu và response lấy id null từ object chưa lưu.
        // Sau: lưu RoomBooking rồi dùng object đã có id để trả về FE.
        RoomBooking savedBooking = repo.save(newBooking);
        LocalDateTime paymentDeadline = LocalDateTime.now().plusHours(24);
        repo.setPaymentDeadline(savedBooking.getId(), paymentDeadline);
        holds.changeStatus(roomBooking.holdId(), "ACTIVE", "CONSUMED");

        return new RoomBookingResponse(
                savedBooking.getId(), savedBooking.getRoomCategoryId(),
                savedBooking.getCheckInDate(), savedBooking.getCheckOutDate(),
                savedBooking.getAdults(), savedBooking.getChildren(),
                savedBooking.getRooms().size(), savedBooking.getTotalPricce(),
                savedBooking.getRoomBookingStatus(), paymentDeadline,
                paymentDeadline.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(), savedBooking.getFullName(),
                savedBooking.getEmail(), savedBooking.getPhone(),
                savedBooking.getDateOfBirth()
        );
    }

    public Boolean updateBooking(RoomBooking roomBooking) {
        return null;
    }

    public List<Room> getRoomByRequest(RoomBookingRequest request) {
        return repo.findRoomsByRequest(request);
    }

    public int checkRoomByRequest(CheckRoomRequest request) {
        if (request == null || request.roomCateId() == null || request.roomType() == null
                || request.checkInDay() == null || request.checkOutDay() == null
                || !request.checkOutDay().isAfter(request.checkInDay())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày hoặc loại phòng không hợp lệ");
        }
        return repo.findRoomByRequest(request);
    }

    @Transactional
    public RoomHoldResponse createHold(RoomHoldRequest request) {
        if (request == null || request.categoryId() == null || request.ownerKey() == null || request.checkInDate() == null
                || request.checkOutDate() == null || request.numberOfRooms() < 1
                || !request.checkOutDate().isAfter(request.checkInDate())
                || request.checkInDate().isBefore(java.time.LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin giữ phòng không hợp lệ");
        }
        holds.lockCategory(request.categoryId());
        List<Room> rooms = holds.selectAvailable(request);
        if (rooms.size() != request.numberOfRooms()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Không còn đủ phòng cho khoảng ngày đã chọn");
        }
        UUID id = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);
        holds.save(id, request, expiresAt, rooms);
        return new RoomHoldResponse(id, expiresAt,
                expiresAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(), rooms.size());
    }

    @Transactional
    public void releaseHold(UUID id, UUID ownerKey) {
        var hold = holds.findForUpdate(id);
        if (hold == null) return;
        if (ownerKey == null || !hold.ownerKey().equals(ownerKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lượt giữ phòng thuộc phiên khác");
        }
        if ("ACTIVE".equals(hold.status())) holds.changeStatus(id, "ACTIVE", "RELEASED");
    }

}
