package com.fastappoint.exception;

public class WhatsAppConnectionException extends RuntimeException {
    public WhatsAppConnectionException(String message) {
        super(message);
    }

    public WhatsAppConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
