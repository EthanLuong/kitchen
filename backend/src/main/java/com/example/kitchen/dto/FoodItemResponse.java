package com.example.kitchen.dto;

import com.example.kitchen.data.FoodItem;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FoodItemResponse(
        Long id,
        String name,
        String foodType,
        Double quantity,
        FoodItem.Unit unit,
        String location,
        LocalDate expirationDate,
        LocalDate purchaseDate,
        LocalDate openedAt,
        String notes,
        boolean consumed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FoodItemResponse from(FoodItem item) {
        return new FoodItemResponse(
                item.getId(),
                item.getName(),
                item.getFoodType(),
                item.getQuantity(),
                item.getUnit(),
                item.getLocation(),
                item.getExpirationDate(),
                item.getPurchaseDate(),
                item.getOpenedAt(),
                item.getNotes(),
                item.isConsumed(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
