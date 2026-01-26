package com.autosavecoach.backend.exception;

public class InvalidMonthException extends RuntimeException {
    public InvalidMonthException(String message) {
        super(message);
    }
}