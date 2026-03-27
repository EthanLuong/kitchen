package com.example.kitchen.dto;

import java.util.UUID;

public record LoginResult(AuthResponse authResponse, String refreshToken) {
}
