package com.example.cleanbookingsbackend.exception;

public class UnauthorizedCallException extends Exception{
    public UnauthorizedCallException(String message) {
        super(message);
    }
}
