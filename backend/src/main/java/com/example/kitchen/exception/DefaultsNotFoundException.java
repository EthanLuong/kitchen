package com.example.kitchen.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DefaultsNotFoundException extends RuntimeException {
    public DefaultsNotFoundException(String message) {
        super(message);
    }
}
