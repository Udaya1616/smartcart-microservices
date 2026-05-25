package com.smartcart.productservice.security;

import com.smartcart.productservice.exception.BadRequestException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtRoleService {
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

    public void requireUser(String authorizationHeader) {
        Claims claims = parseClaims(authorizationHeader, "User login token required. Send Authorization: Bearer <token>");
        if (!"USER".equalsIgnoreCase(String.valueOf(claims.get("role")))) {
            throw new BadRequestException("User role required");
        }
    }

    public void requireAuthenticated(String authorizationHeader) {
        parseClaims(authorizationHeader, "Login token required. Send Authorization: Bearer <token>");
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
}
