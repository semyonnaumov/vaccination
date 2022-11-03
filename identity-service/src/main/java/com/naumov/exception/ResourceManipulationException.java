package com.naumov.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ResourceManipulationException extends RuntimeException {
    public ResourceManipulationException(String message) {
        super(message);
    }
}
