package com.fastappoint.exception;

public class ContactDeliveryException extends RuntimeException {
    public ContactDeliveryException(String message) {
        super(message);
    }

    public ContactDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
