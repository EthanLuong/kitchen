package com.example.kitchen.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, FoodItemRequest> {

    @Override
    public boolean isValid(FoodItemRequest request, ConstraintValidatorContext context) {
        if (request.purchaseDate() == null || request.expirationDate() == null) {
            return true; // individual field nullability is handled by other constraints
        }
        return !request.purchaseDate().isAfter(request.expirationDate());
    }
}
