package ngoctamhotel.ngoctamhotel.security;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ngoctamhotel.ngoctamhotel.model.User;
import ngoctamhotel.ngoctamhotel.repository.UserRepository;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    // Band-aid cache: giảm DB hit per-request (TTL ~60s per entry)
    // TODO Task riêng: nhúng role vào JWT claims để bỏ hoàn toàn DB lookup
    private final Map<String, CachedAuth> authCache = new ConcurrentHashMap<>();
    private record CachedAuth(String role, long expiresAt) {}

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);
                String username = jwtService.getValidUsername(token);

                // Kiểm tra cache trước, chỉ query DB khi cache miss/expired
                String role = getFromCacheOrDb(token, username);
                if (role != null) {
                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority(role));
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(username, null, authorities));
                }
            } catch (Exception ignored) {
                // Fix #16b: Bắt Exception tổng quát (bao gồm cả ExpiredJwtException, IllegalArgumentException...)
                // Không set authentication → request sẽ bị reject bởi SecurityConfig
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getFromCacheOrDb(String token, String username) {
        long now = System.currentTimeMillis();
        CachedAuth cached = authCache.get(token);
        if (cached != null && cached.expiresAt() > now) {
            return cached.role();
        }
        // Cache miss: query DB, cache kết quả 60 giây
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            authCache.put(token, new CachedAuth(user.role(), now + 60_000));
            // Dọn cache cũ để tránh memory leak (chỉ xóa entry quá hạn khi cache > 1000)
            if (authCache.size() > 1000) {
                authCache.entrySet().removeIf(e -> e.getValue().expiresAt() <= now);
            }
            return user.role();
        }
        return null;
    }
}
