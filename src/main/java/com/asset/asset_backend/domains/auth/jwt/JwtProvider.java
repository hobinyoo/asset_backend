package com.asset.asset_backend.domains.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    private static final long ACCESS_TOKEN_EXPIRATION_MS  = 15 * 60 * 1000L;           // 15분
    private static final long REFRESH_TOKEN_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L;  // 7일

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    private static final String CLAIM_USER_ID = "userId";

    public String generateAccessToken(String loginId, Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(loginId)
                .claim(CLAIM_USER_ID, userId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_MS))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String loginId) {
        return buildToken(loginId, REFRESH_TOKEN_EXPIRATION_MS);
    }

    public LocalDateTime getRefreshTokenExpiredAt() {
        return LocalDateTime.now().plusSeconds(REFRESH_TOKEN_EXPIRATION_MS / 1000);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JwtProvider] 유효하지 않은 토큰: {}", e.getMessage());
            return false;
        }
    }

    public String getLoginId(String token) {
        return getClaims(token).getSubject();
    }

    public Long getUserId(String token) {
        return getClaims(token).get(CLAIM_USER_ID, Long.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String buildToken(String loginId, long expirationMs) {
        Date now = new Date();
        return Jwts.builder()
                .subject(loginId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }
}
