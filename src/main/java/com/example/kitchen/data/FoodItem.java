package com.example.kitchen.data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "food_items")
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owner
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid", nullable = false)
    private User user;

    // Basic info
    @Column(nullable = false)
    private String name;

    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FoodType foodType;

    // Quantity
    private Double quantity;

    @Enumerated(EnumType.STRING)
    private Unit unit;

    // Location
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Location location;

    // Dates
    private LocalDate expirationDate;
    private LocalDate purchaseDate;
    private LocalDate openedAt;

    // Extra
    @Column(columnDefinition = "TEXT")
    private String notes;

    private boolean consumed = false;

    // Audit
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt; // soft delete — null means active


    // ── Enums ──────────────────────────────────────────────────

    public enum FoodType {
        DAIRY, MEAT, PRODUCE, GRAIN, BEVERAGE, FROZEN, CONDIMENT, SNACK, OTHER
    }

    public enum Unit {
        OZ, LBS, ML, L, G, KG, COUNT
    }

    public enum Location {
        FRIDGE, FREEZER, PANTRY, CABINET, COUNTER, OTHER
    }
}
