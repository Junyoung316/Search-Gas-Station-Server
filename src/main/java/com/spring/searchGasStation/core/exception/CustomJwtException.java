package com.spring.searchGasStation.core.exception;

public class CustomJwtException extends RuntimeException {
    public CustomJwtException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomJwtException(String message) {
        super(message);
    }
}

