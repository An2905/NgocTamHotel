package ngoctamhotel.ngoctamhotel.controller;

import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    // Helper tránh null crash trong Map.of()
    private static String safeMessage(Exception e, String fallback) {
        return Objects.requireNonNullElse(e.getMessage(), fallback);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, String> badRequest(IllegalArgumentException exception) {
        return Map.of("message", safeMessage(exception, "Yêu cầu không hợp lệ"));
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    Map<String, String> forbidden(IllegalStateException exception) {
        return Map.of("message", safeMessage(exception, "Truy cập bị từ chối"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, String> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst().map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Dữ liệu không hợp lệ");
        return Map.of("message", message);
    }

    // Fix #14: Catch-all để chặn stack trace rò rỉ ra client
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    Map<String, String> internalError(Exception exception) {
        // Log nội bộ — KHÔNG trả message gốc ra ngoài
        exception.printStackTrace(); // THÊM DÒNG NÀY ĐỂ IN LOG RA RAILWAY
        return Map.of("message", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
    }
}
