package com.example.kitchen.dto;

import com.example.kitchen.data.FoodItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

@ValidDateRange
public record FoodItemRequest(
        @NotBlank String name,
        @NotBlank String foodType,
        @Positive Double quantity,
        FoodItem.Unit unit,
        @NotBlank String location,
        LocalDate expirationDate,
        LocalDate purchaseDate,
        LocalDate openedAt,
        String notes
) {}
