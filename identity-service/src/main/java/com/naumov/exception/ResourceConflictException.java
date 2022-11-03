package com.naumov.exception;

public class ResourceConflictException extends ResourceManipulationException {
    public ResourceConflictException(String message) {
        super(message);
    }
}
