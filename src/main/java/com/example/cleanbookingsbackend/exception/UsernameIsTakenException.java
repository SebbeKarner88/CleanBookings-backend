package com.example.cleanbookingsbackend.exception;

public class UsernameIsTakenException extends Exception {
    public UsernameIsTakenException(String errorMessage) {
        super(errorMessage);
    }
}
