package com.example.kitchen.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresIn) {}
