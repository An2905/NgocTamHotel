package ngoctamhotel.ngoctamhotel.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final Base64.Encoder BASE64_URL = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private final byte[] secret;
    private final long expirationSeconds;

    public JwtService(
            @Value("${app.auth.jwt-secret}") String secret,
            @Value("${app.auth.jwt-expiration-seconds}") long expirationSeconds) {
        if (secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret phải có ít nhất 32 ký tự");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public String createToken(UUID userId, String username) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + expirationSeconds;
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode("{\"sub\":\"" + escape(username) + "\",\"uid\":\"" + userId
                + "\",\"iat\":" + issuedAt + ",\"exp\":" + expiresAt + "}");
        String unsignedToken = header + "." + payload;
        return unsignedToken + "." + BASE64_URL.encodeToString(sign(unsignedToken));
    }

    public String getValidUsername(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3 || !constantTimeEquals(sign(parts[0] + "." + parts[1]), decode(parts[2]))) {
            throw new IllegalArgumentException("Token không hợp lệ");
        }
        String payload = new String(BASE64_URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);
        long expiration = Long.parseLong(extractJsonValue(payload, "exp"));
        if (Instant.now().getEpochSecond() >= expiration) {
            throw new IllegalArgumentException("Token đã hết hạn");
        }
        return extractJsonValue(payload, "sub");
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    private String encode(String value) {
        return BASE64_URL.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] decode(String value) {
        return BASE64_URL_DECODER.decode(value);
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Không thể ký JWT", exception);
        }
    }

    private boolean constantTimeEquals(byte[] expected, byte[] actual) {
        return java.security.MessageDigest.isEqual(expected, actual);
    }

    private String extractJsonValue(String json, String key) {
        String marker = "\"" + key + "\":";
        int start = json.indexOf(marker);
        if (start < 0) throw new IllegalArgumentException("Token thiếu dữ liệu");
        start += marker.length();
        if (json.charAt(start) == '"') {
            int end = json.indexOf('"', start + 1);
            return json.substring(start + 1, end);
        }
        int end = json.indexOf(',', start);
        if (end < 0) end = json.indexOf('}', start);
        return json.substring(start, end);
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

