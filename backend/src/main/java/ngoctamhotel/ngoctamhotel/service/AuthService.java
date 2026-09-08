package ngoctamhotel.ngoctamhotel.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ngoctamhotel.ngoctamhotel.dto.request.LoginRequest;
import ngoctamhotel.ngoctamhotel.dto.request.RegisterRequest;
import ngoctamhotel.ngoctamhotel.dto.request.UpdateRequest;
import ngoctamhotel.ngoctamhotel.dto.response.AuthResponse;
import ngoctamhotel.ngoctamhotel.dto.response.UserResponse;
import ngoctamhotel.ngoctamhotel.model.User;
import ngoctamhotel.ngoctamhotel.repository.UserRepository;
import ngoctamhotel.ngoctamhotel.security.JwtService;

@Service
public class AuthService {
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final boolean registrationEnabled;

        public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                        @Value("${app.auth.registration-enabled}") boolean registrationEnabled) {
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
                this.jwtService = jwtService;
                this.registrationEnabled = registrationEnabled;
        }

        public UserResponse register(RegisterRequest request) {
                if (!registrationEnabled) {
                        throw new IllegalStateException("Chức năng đăng ký đang bị tắt");
                }
                User user = userRepository.create(
                                request.username().trim(),
                                passwordEncoder.encode(request.password()),
                                request.email().trim().toLowerCase());
                return new UserResponse(user.id(), user.username(), user.email(), user.createdAt());
        }

        public AuthResponse login(LoginRequest request) {
                User user = userRepository.findByUsername(request.username().trim())
                                .orElseThrow(() -> new IllegalArgumentException("Sai username hoặc password"));
                if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
                        throw new IllegalArgumentException("Sai username hoặc password");
                }
                return new AuthResponse(
                                jwtService.createToken(user.id(), user.username()),
                                "Bearer",
                                jwtService.getExpirationSeconds(),
                                user.id(),
                                user.username(),
                                user.email());
        }

        public AuthResponse updateUser(String currentUsername, UpdateRequest request) {
                User current = userRepository.findByUsername(currentUsername)
                                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));

                String newUsername = (request.username() == null || request.username().isBlank())
                                ? current.username()
                                : request.username().trim();
                String newEmail = (request.email() == null || request.email().isBlank())
                                ? current.email()
                                : request.email().trim().toLowerCase();
                String newPasswordHash = (request.password() == null || request.password().isBlank())
                                ? current.passwordHash()
                                : passwordEncoder.encode(request.password());

                User updated = userRepository.update(current.id(), newUsername, newEmail, newPasswordHash);
                return new AuthResponse(
                                jwtService.createToken(updated.id(), updated.username()),
                                "Bearer",
                                jwtService.getExpirationSeconds(), 
                                updated.id(),
                                updated.username(), 
                                updated.email());
        }
}
