package com.fastappoint.security;

import java.util.UUID;

/** The authenticated identity extracted from a valid access token -- set in the SecurityContext by
 * {@link JwtAuthenticationFilter}, retrieved in controllers via {@code @AuthenticationPrincipal}. */
public record AuthPrincipal(UUID userId, String email) {
}
