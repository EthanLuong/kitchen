package com.example.kitchen.controller;

import java.security.Principal;
import java.util.UUID;

public final class ControllerUtils {

    private ControllerUtils() {}

    public static UUID userId(Principal principal) {
        return UUID.fromString(principal.getName());
    }
}
