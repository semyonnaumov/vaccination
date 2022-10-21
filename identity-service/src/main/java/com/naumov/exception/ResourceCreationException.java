package com.naumov.exception;

public class ResourceCreationException extends RuntimeException {
    public ResourceCreationException() {
    }

    public ResourceCreationException(String message) {
        super(message);
    }
}
