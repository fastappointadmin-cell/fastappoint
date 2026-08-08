package com.fastappoint.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Split out of SecurityConfig deliberately: SecurityConfig's constructor pulls in
 * OAuth2LoginSuccessHandler, which depends on AuthService, which depends on PasswordEncoder -- if
 * PasswordEncoder were a @Bean method on SecurityConfig itself, Spring would need to fully construct
 * SecurityConfig to satisfy AuthService's dependency, while also needing AuthService (transitively) to
 * construct SecurityConfig. A separate, dependency-free config class breaks that cycle.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
