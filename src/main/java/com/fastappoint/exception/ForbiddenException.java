package com.fastappoint.exception;

/** The caller is authenticated but has no membership on the business they're trying to access. */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
