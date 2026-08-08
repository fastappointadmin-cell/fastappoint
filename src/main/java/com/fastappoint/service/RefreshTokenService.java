package com.fastappoint.service;

import com.fastappoint.domain.AppUser;
import com.fastappoint.domain.RefreshToken;
import com.fastappoint.exception.AuthenticationFailedException;
import com.fastappoint.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Backs the httpOnly refresh-token cookie. Only a SHA-256 hash of the raw token is ever persisted
 * (same principle as password hashing) -- the raw value exists only in the cookie. Every use rotates
 * the token (old one revoked, new one issued); presenting an already-revoked token is treated as a
 * theft signal and revokes every active token the user has.
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokens;
    private final long refreshTokenTtlDays;
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokens,
            @Value("${app.jwt.refresh-token-ttl-days}") long refreshTokenTtlDays) {
        this.refreshTokens = refreshTokens;
        this.refreshTokenTtlDays = refreshTokenTtlDays;
    }

    public record IssuedToken(AppUser user, String rawValue, LocalDateTime expiresAt) {
    }

    @Transactional
    public IssuedToken issue(AppUser user) {
        String raw = generateRawToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(refreshTokenTtlDays);
        refreshTokens.save(new RefreshToken(user, hash(raw), expiresAt));
        return new IssuedToken(user, raw, expiresAt);
    }

    @Transactional
    public IssuedToken rotate(String rawToken) {
        RefreshToken existing = refreshTokens.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new AuthenticationFailedException("Invalid refresh token"));

        if (existing.getRevokedAt() != null) {
            refreshTokens.revokeAllActiveForUser(existing.getUser().getId(), LocalDateTime.now());
            throw new AuthenticationFailedException("Refresh token reuse detected -- all sessions revoked");
        }
        if (!existing.isActive()) {
            throw new AuthenticationFailedException("Refresh token expired");
        }

        existing.revoke();
        return issue(existing.getUser());
    }

    @Transactional
    public void revoke(String rawToken) {
        refreshTokens.findByTokenHash(hash(rawToken)).ifPresent(RefreshToken::revoke);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
