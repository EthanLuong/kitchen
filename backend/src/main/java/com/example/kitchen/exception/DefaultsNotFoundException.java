package com.example.kitchen.exception;

public class DefaultsNotFoundException extends RuntimeException {
    public DefaultsNotFoundException(String message) {
        super(message);
    }
}
