package com.naumov.exception;

public class PersonCreationException extends RuntimeException {
    public PersonCreationException() {
    }

    public PersonCreationException(String message) {
        super(message);
    }
}
