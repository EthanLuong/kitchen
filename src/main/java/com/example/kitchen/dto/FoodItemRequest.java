package com.example.kitchen.dto;

import com.example.kitchen.data.FoodItem;

import java.time.LocalDate;

public record FoodItemRequest(
        String name,
        String brand,
        FoodItem.FoodType foodType,
        Double quantity,
        FoodItem.Unit unit,
        FoodItem.Location location,
        LocalDate expirationDate,
        LocalDate purchaseDate,
        LocalDate openedAt,
        String notes
) {}
