package com.example.kitchen.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponse(
        @Schema(description = "JWT Bearer token")
        String accessToken,
        String tokenType,
        @Schema(description = "Expiration time of token")
        long expiresIn,
        String refreshToken
) {}
