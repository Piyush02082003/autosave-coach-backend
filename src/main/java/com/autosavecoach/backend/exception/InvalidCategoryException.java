package com.autosavecoach.backend.exception;

public class InvalidCategoryException extends RuntimeException {

    public InvalidCategoryException(String category) {
        super(category);
    }
}
