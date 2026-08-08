package com.fastappoint.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * The refresh token travels ONLY as an httpOnly cookie, never in JS-readable storage.
 *
 * <p>{@code secure}/{@code SameSite} are configurable (app.jwt.cookie-secure) rather than hardcoded,
 * because the right combination depends on deployment topology: a cross-origin frontend/API split
 * (different ports or domains) needs {@code SameSite=None}, which mandates {@code Secure} -- fine over
 * real HTTPS, but silently dropped by browsers over plain HTTP even on localhost (that "localhost is a
 * secure context" exemption does NOT reliably extend to the cookie {@code Secure} attribute). Locally,
 * the Angular dev server proxies API calls so the browser sees everything as same-origin instead (see
 * proxy.conf.json), which only needs {@code SameSite=Lax} and no {@code Secure} -- avoiding that
 * unreliable browser behavior entirely rather than depending on it.
 */
@Component
public class RefreshTokenCookie {

    public static final String COOKIE_NAME = "refresh_token";
    private static final String PATH = "/api/auth";

    private final boolean secure;

    public RefreshTokenCookie(@Value("${app.cookie.secure}") boolean secure) {
        this.secure = secure;
    }

    public ResponseCookie build(String rawValue, LocalDateTime expiresAt) {
        long seconds = Math.max(Duration.between(LocalDateTime.now(), expiresAt).getSeconds(), 0);
        return ResponseCookie.from(COOKIE_NAME, rawValue)
                .httpOnly(true)
                .secure(secure)
                .sameSite(secure ? "None" : "Lax")
                .path(PATH)
                .maxAge(seconds)
                .build();
    }

    public ResponseCookie clear() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(secure ? "None" : "Lax")
                .path(PATH)
                .maxAge(0)
                .build();
    }
}
