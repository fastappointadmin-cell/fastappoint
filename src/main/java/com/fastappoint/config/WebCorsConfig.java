package com.fastappoint.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Single source of truth for CORS, consumed by {@code SecurityConfig}'s {@code http.cors(...)}.
 * Spring Security's filter chain runs before MVC dispatch, so this has to be the enforcement point
 * now that Security is in the picture -- a separate {@code WebMvcConfigurer}-level CORS mapping
 * would be redundant (and preflight requests would never reach it if Security rejected them first).
 * {@code allowCredentials(true)} + an explicit (never wildcard) origin is required for the httpOnly
 * refresh-token cookie to be accepted cross-origin.
 */
@Configuration
public class WebCorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:}") String allowedOrigins,
            @Value("${app.cors.allowed-origin:http://localhost:4200}") String allowedOrigin) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(resolveAllowedOriginPatterns(allowedOrigins, allowedOrigin));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private List<String> resolveAllowedOriginPatterns(String allowedOrigins, String allowedOrigin) {
        List<String> patterns = new ArrayList<>();
        if (StringUtils.hasText(allowedOrigins)) {
            for (String candidate : allowedOrigins.split(",")) {
                String trimmed = candidate.trim();
                if (!trimmed.isEmpty()) {
                    patterns.add(trimmed);
                }
            }
        }
        if (patterns.isEmpty()) {
            patterns.add(allowedOrigin.trim());
        }
        return patterns;
    }
}
