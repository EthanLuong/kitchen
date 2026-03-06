package com.example.kitchen.repository;

import com.example.kitchen.data.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    // All active (non-deleted, non-consumed) items for a user
    List<FoodItem> findByUserUsernameAndDeletedAtIsNullAndConsumedFalse(String username);

    // Expiring within N days for a user
    @Query("SELECT f FROM FoodItem f WHERE f.user.username = :username " +
           "AND f.deletedAt IS NULL AND f.consumed = false " +
           "AND f.expirationDate BETWEEN CURRENT_DATE AND :cutoff")
    List<FoodItem> findExpiringSoon(@Param("username") String username,
                                    @Param("cutoff") LocalDate cutoff);

    // Filter by location
    List<FoodItem> findByUserUsernameAndLocationAndDeletedAtIsNullAndConsumedFalse(
            String username, FoodItem.Location location);

    // Filter by food type
    List<FoodItem> findByUserUsernameAndFoodTypeAndDeletedAtIsNullAndConsumedFalse(
            String username, FoodItem.FoodType foodType);
}
