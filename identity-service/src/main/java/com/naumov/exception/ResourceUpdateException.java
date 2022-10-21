package com.naumov.exception;

public class ResourceUpdateException extends RuntimeException {
    public ResourceUpdateException() {
    }

    public ResourceUpdateException(String message) {
        super(message);
    }
}
