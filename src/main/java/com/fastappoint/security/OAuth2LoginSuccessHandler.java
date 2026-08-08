package com.fastappoint.security;

import com.fastappoint.domain.OAuthProvider;
import com.fastappoint.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Runs after Spring Security's OAuth2 login dance completes. Resolves-or-creates our own AppUser
 * from the provider's profile, issues our own access+refresh token pair (never the provider's
 * token), sets the refresh cookie, and redirects the BROWSER back to the frontend with the access
 * token in the URL fragment (never sent to the server, read once by auth-callback-page then discarded).
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final RefreshTokenCookie refreshTokenCookie;
    private final String frontendBaseUrl;

    public OAuth2LoginSuccessHandler(AuthService authService, RefreshTokenCookie refreshTokenCookie,
                                      @Value("${app.frontend-base-url}") String frontendBaseUrl) {
        this.authService = authService;
        this.refreshTokenCookie = refreshTokenCookie;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuthProvider provider = resolveProvider(request);

        Object sub = oAuth2User.getAttributes().getOrDefault("sub", oAuth2User.getAttributes().get("id"));
        String providerUserId = String.valueOf(sub);
        String email = (String) oAuth2User.getAttributes().get("email");
        String name = (String) oAuth2User.getAttributes().get("name");

        AuthService.IssuedSession session = authService.loginWithOAuth(provider, providerUserId, email, name);

        ResponseCookie cookie = refreshTokenCookie.build(session.rawRefreshToken(), session.refreshExpiresAt());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        String token = URLEncoder.encode(session.response().getAccessToken(), StandardCharsets.UTF_8);
        response.sendRedirect(frontendBaseUrl + "/auth/callback#accessToken=" + token);
    }

    private OAuthProvider resolveProvider(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.contains("google")) return OAuthProvider.GOOGLE;
        if (uri.contains("facebook")) return OAuthProvider.FACEBOOK;
        throw new IllegalStateException("Unknown OAuth provider for callback: " + uri);
    }
}
