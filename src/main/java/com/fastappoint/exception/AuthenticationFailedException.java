package com.fastappoint.exception;

/** Bad credentials at login, or an invalid/expired/reused refresh token at /api/auth/refresh. */
public class AuthenticationFailedException extends RuntimeException {
    public AuthenticationFailedException(String message) {
        super(message);
    }
}
