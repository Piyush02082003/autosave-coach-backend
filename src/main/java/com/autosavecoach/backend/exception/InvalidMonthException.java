package com.autosavecoach.backend.exception;

public class InvalidMonthException extends RuntimeException {
    public InvalidMonthException(String value) {
        super("Invalid month format: " + value + ". Expected YYYY-MM");
    }
}