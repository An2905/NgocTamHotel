package ngoctamhotel.ngoctamhotel.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import ngoctamhotel.ngoctamhotel.dto.request.LoginRequest;
import ngoctamhotel.ngoctamhotel.dto.request.RegisterRequest;
import ngoctamhotel.ngoctamhotel.dto.request.UpdateRequest;
import ngoctamhotel.ngoctamhotel.dto.response.AuthResponse;
import ngoctamhotel.ngoctamhotel.dto.response.UserResponse;
import ngoctamhotel.ngoctamhotel.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PutMapping("/update")
    public AuthResponse updateCurrentUser(Authentication authentication,
            @Valid @RequestBody UpdateRequest request) {
        return authService.updateUser(authentication.getName(), request);
    }
}
