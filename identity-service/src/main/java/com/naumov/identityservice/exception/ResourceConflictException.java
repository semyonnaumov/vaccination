package com.naumov.identityservice.exception;

public class ResourceConflictException extends ResourceManipulationException {
    public ResourceConflictException(String message) {
        super(message);
    }
}
