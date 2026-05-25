package com.smartcart.orderservice.security;

import com.smartcart.orderservice.exception.BadRequestException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtRoleService {
    private static final long SERVICE_TOKEN_EXPIRATION_MS = 5 * 60 * 1000;

    private final SecretKey key;

    public JwtRoleService(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public void requireAdmin(String authorizationHeader) {
        Claims claims = parseClaims(authorizationHeader, "Admin login token required. Send Authorization: Bearer <token>");
        if (!"ADMIN".equalsIgnoreCase(String.valueOf(claims.get("role")))) {
            throw new BadRequestException("Admin role required");
        }
    }

    public UserToken requireUser(String authorizationHeader) {
        Claims claims = parseClaims(authorizationHeader, "User login token required. Send Authorization: Bearer <token>");
        if (!"USER".equalsIgnoreCase(String.valueOf(claims.get("role")))) {
            throw new BadRequestException("User role required");
        }
        return new UserToken(getUserId(claims));
    }

    public String createServiceAdminAuthorizationHeader() {
        Date now = new Date();
        String token = Jwts.builder()
                .subject("order-service")
                .claim("userId", 0L)
                .claim("role", "ADMIN")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + SERVICE_TOKEN_EXPIRATION_MS))
                .signWith(key)
                .compact();
        return "Bearer " + token;
    }

    private Claims parseClaims(String authorizationHeader, String missingTokenMessage) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BadRequestException(missingTokenMessage);
        }

        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(authorizationHeader.substring(7))
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadRequestException("Invalid login token");
        }
    }

    private Long getUserId(Claims claims) {
        Object userId = claims.get("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        if (userId instanceof String value) {
            try {
                return Long.valueOf(value);
            } catch (NumberFormatException ex) {
                throw new BadRequestException("Invalid user id in token");
            }
        }
        throw new BadRequestException("User id missing from token");
    }

    public record UserToken(Long userId) {
    }
}
