package com.example.kitchen.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank
        @Schema(description = "Name of the user", example = "Ethan")
        String username,
        @Schema(description = "User's password", example = "password")
        @NotBlank String password
) {}
