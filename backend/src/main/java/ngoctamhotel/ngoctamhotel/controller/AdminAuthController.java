package ngoctamhotel.ngoctamhotel.controller;

import java.util.Map;

import ngoctamhotel.ngoctamhotel.dto.request.LoginRequest;
import ngoctamhotel.ngoctamhotel.repository.AdminRepository;
import ngoctamhotel.ngoctamhotel.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {
    private final AdminRepository repository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AdminAuthController(AdminRepository repository, PasswordEncoder encoder, JwtService jwtService) {
        this.repository = repository;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thiếu tên đăng nhập hoặc mật khẩu"));
        }
        var admin = repository.findByName(request.username());
        if (admin.isEmpty() || !encoder.matches(request.password(), admin.get().password())) {
            return ResponseEntity.status(401).body(Map.of("message", "Sai tên đăng nhập hoặc mật khẩu"));
        }
        return ResponseEntity.ok(Map.of(
                "token", jwtService.createAdminToken(admin.get().name()),
                "tokenType", "Bearer",
                "expiresIn", jwtService.getExpirationSeconds(),
                "username", admin.get().name(),
                "role", "ADMIN"));
    }
}
