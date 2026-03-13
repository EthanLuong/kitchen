package com.example.kitchen.repository;

import com.example.kitchen.data.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    // All active (non-deleted, non-consumed) items for a user
    List<FoodItem> findByUserUseridAndDeletedAtIsNullAndConsumedFalse(UUID userid);

    // Expiring within N days for a user
    @Query("SELECT f FROM FoodItem f WHERE f.user.userid = :userid " +
           "AND f.deletedAt IS NULL AND f.consumed = false " +
           "AND f.expirationDate BETWEEN CURRENT_DATE AND :cutoff")
    List<FoodItem> findExpiringSoon(@Param("userid") UUID userid,
                                    @Param("cutoff") LocalDate cutoff);

    // Filter by location
    List<FoodItem> findByUserUseridAndLocationAndDeletedAtIsNullAndConsumedFalse(
            UUID userid, FoodItem.Location location);

    // Filter by food type
    List<FoodItem> findByUserUseridAndFoodTypeAndDeletedAtIsNullAndConsumedFalse(
            UUID userid, FoodItem.FoodType foodType);
}
