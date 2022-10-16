package com.naumov.exception;

public class PersonConsistencyException extends RuntimeException {
    public PersonConsistencyException() {
    }

    public PersonConsistencyException(String message) {
        super(message);
    }
}
