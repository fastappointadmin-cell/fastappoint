package com.fastappoint.controller;

import com.fastappoint.dto.AuthResponse;
import com.fastappoint.dto.LoginRequest;
import com.fastappoint.dto.RegisterRequest;
import com.fastappoint.exception.AuthenticationFailedException;
import com.fastappoint.security.AuthPrincipal;
import com.fastappoint.security.RefreshTokenCookie;
import com.fastappoint.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookie refreshTokenCookie;

    public AuthController(AuthService authService, RefreshTokenCookie refreshTokenCookie) {
        this.authService = authService;
        this.refreshTokenCookie = refreshTokenCookie;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request, HttpServletResponse response) {
        AuthService.IssuedSession session = authService.register(request);
        attachRefreshCookie(response, session);
        return ResponseEntity.status(HttpStatus.CREATED).body(session.response());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        AuthService.IssuedSession session = authService.login(request);
        attachRefreshCookie(response, session);
        return ResponseEntity.ok(session.response());
    }

    /** Cookie rides along automatically on same-path cross-origin calls (SameSite=None); rotates it
     * on every call -- see RefreshTokenService for the reuse-detection this enables. */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = requireRefreshCookie(request);
        AuthService.IssuedSession session = authService.refresh(rawToken);
        attachRefreshCookie(response, session);
        return ResponseEntity.ok(session.response());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = findRefreshCookie(request);
        if (rawToken != null) {
            authService.logout(rawToken);
        }
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.clear().toString());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(authService.me(principal.userId()));
    }

    private void attachRefreshCookie(HttpServletResponse response, AuthService.IssuedSession session) {
        ResponseCookie cookie = refreshTokenCookie.build(session.rawRefreshToken(), session.refreshExpiresAt());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String requireRefreshCookie(HttpServletRequest request) {
        String value = findRefreshCookie(request);
        if (value == null) {
            throw new AuthenticationFailedException("Missing refresh token");
        }
        return value;
    }

    private String findRefreshCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (RefreshTokenCookie.COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
