package com.naumov.exception;

public class ResourceNotFoundException extends ResourceManipulationException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
