package com.example.kitchen.dto;

import com.example.kitchen.data.FoodItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FoodItemRequest(
        @NotBlank String name,
        @NotNull FoodItem.FoodType foodType,
        Double quantity,
        FoodItem.Unit unit,
        @NotNull FoodItem.Location location,
        LocalDate expirationDate,
        LocalDate purchaseDate,
        LocalDate openedAt,
        String notes
) {}
