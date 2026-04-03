package com.example.kitchen.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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


    @Column(nullable = false)
    private String foodType;

    // Quantity
    private Double quantity;

    @Enumerated(EnumType.STRING)
    private Unit unit;

    // Location
    @Column(nullable = false)
    private String location;

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

    public enum Unit {
        OZ, LBS, ML, L, G, KG, COUNT
    }


}
