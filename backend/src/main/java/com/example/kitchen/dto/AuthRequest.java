package com.example.kitchen.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @NotBlank
        @Schema(description = "Name of the user", example = "Ethan")
        @Size(min=3)
        String username,
        @Schema(description = "User's password", example = "Str0ngP4ssw0rd!")
        @NotBlank
        @Size(min = 8)
        String password
) {}
