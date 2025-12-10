package com.spring.searchGasStation.core.exception;

public class CustomNoResourcesJwtException extends RuntimeException {
    public CustomNoResourcesJwtException(String message) {
        super(message);
    }
}
