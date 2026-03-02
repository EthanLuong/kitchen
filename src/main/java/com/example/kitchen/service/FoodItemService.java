package com.example.kitchen.service;

import com.example.kitchen.data.FoodItem;
import com.example.kitchen.data.User;
import com.example.kitchen.repository.FoodItemRepository;
import com.example.kitchen.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FoodItemService {

    private final FoodItemRepository foodRepo;
    private final UserRepository userRepo;

    public FoodItemService(FoodItemRepository foodRepo, UserRepository userRepo) {
        this.foodRepo = foodRepo;
        this.userRepo = userRepo;
    }

    // ── Create ─────────────────────────────────────────────────

    public FoodItem addItem(String username, FoodItem item) {
        User user = userRepo.findById(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        item.setUser(user);
        return foodRepo.save(item);
    }

    // ── Read ───────────────────────────────────────────────────

    public List<FoodItem> getAllItems(String username) {
        return foodRepo.findByUserUsernameAndDeletedAtIsNullAndConsumedFalse(username);
    }

    public FoodItem getItem(String username, Long id) {
        FoodItem item = foodRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        assertOwner(username, item);
        return item;
    }

    public List<FoodItem> getExpiringSoon(String username, int days) {
        LocalDate cutoff = LocalDate.now().plusDays(days);
        return foodRepo.findExpiringSoon(username, cutoff);
    }

    public List<FoodItem> getByLocation(String username, FoodItem.Location location) {
        return foodRepo.findByUserUsernameAndLocationAndDeletedAtIsNullAndConsumedFalse(username, location);
    }

    public List<FoodItem> getByFoodType(String username, FoodItem.FoodType type) {
        return foodRepo.findByUserUsernameAndFoodTypeAndDeletedAtIsNullAndConsumedFalse(username, type);
    }

    // ── Update ─────────────────────────────────────────────────

    public FoodItem updateItem(String username, Long id, FoodItem updated) {
        FoodItem existing = getItem(username, id);
        existing.setName(updated.getName());
        existing.setBrand(updated.getBrand());
        existing.setFoodType(updated.getFoodType());
        existing.setQuantity(updated.getQuantity());
        existing.setUnit(updated.getUnit());
        existing.setLocation(updated.getLocation());
        existing.setExpirationDate(updated.getExpirationDate());
        existing.setPurchaseDate(updated.getPurchaseDate());
        existing.setOpenedAt(updated.getOpenedAt());
        existing.setNotes(updated.getNotes());
        return foodRepo.save(existing);
    }

    public FoodItem markConsumed(String username, Long id) {
        FoodItem item = getItem(username, id);
        item.setConsumed(true);
        return foodRepo.save(item);
    }

    // ── Delete (soft) ──────────────────────────────────────────

    public void deleteItem(String username, Long id) {
        FoodItem item = getItem(username, id);
        item.setDeletedAt(LocalDateTime.now());
        foodRepo.save(item);
    }

    // ── Helpers ────────────────────────────────────────────────

    private void assertOwner(String username, FoodItem item) {
        if (!item.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }
}
