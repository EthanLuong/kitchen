package com.example.kitchen.service;

import com.example.kitchen.data.FoodItem;
import com.example.kitchen.data.User;
import com.example.kitchen.dto.FoodItemRequest;
import com.example.kitchen.dto.FoodItemResponse;
import com.example.kitchen.repository.FoodItemRepository;
import com.example.kitchen.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Transactional(readOnly = true)
@Service
public class FoodItemService {

    private final FoodItemRepository foodRepo;
    private final UserRepository userRepo;

    public FoodItemService(FoodItemRepository foodRepo, UserRepository userRepo) {
        this.foodRepo = foodRepo;
        this.userRepo = userRepo;
    }

    // ── Create ─────────────────────────────────────────────────
    @Transactional
    public FoodItemResponse addItem(String username, FoodItemRequest request) {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        FoodItem item = applyRequest(new FoodItem(), request);
        item.setUser(user);
        return FoodItemResponse.from(foodRepo.save(item));
    }

    // ── Read ───────────────────────────────────────────────────

    public List<FoodItemResponse> getAllItems(String username) {
        return foodRepo.findByUserUsernameAndDeletedAtIsNullAndConsumedFalse(username)
                .stream().map(FoodItemResponse::from).toList();
    }

    public FoodItemResponse getItem(String username, Long id) {
        return FoodItemResponse.from(findOwnedItem(username, id));
    }

    public List<FoodItemResponse> getExpiringSoon(String username, int days) {
        LocalDate cutoff = LocalDate.now().plusDays(days);
        return foodRepo.findExpiringSoon(username, cutoff)
                .stream().map(FoodItemResponse::from).toList();
    }

    public List<FoodItemResponse> getByLocation(String username, FoodItem.Location location) {
        return foodRepo.findByUserUsernameAndLocationAndDeletedAtIsNullAndConsumedFalse(username, location)
                .stream().map(FoodItemResponse::from).toList();
    }

    public List<FoodItemResponse> getByFoodType(String username, FoodItem.FoodType type) {
        return foodRepo.findByUserUsernameAndFoodTypeAndDeletedAtIsNullAndConsumedFalse(username, type)
                .stream().map(FoodItemResponse::from).toList();
    }

    // ── Update ─────────────────────────────────────────────────
    @Transactional
    public FoodItemResponse updateItem(String username, Long id, FoodItemRequest request) {
        FoodItem existing = findOwnedItem(username, id);
        applyRequest(existing, request);
        return FoodItemResponse.from(foodRepo.save(existing));
    }
    @Transactional
    public FoodItemResponse markConsumed(String username, Long id) {
        FoodItem item = findOwnedItem(username, id);
        item.setConsumed(true);
        return FoodItemResponse.from(foodRepo.save(item));
    }

    // ── Delete (soft) ──────────────────────────────────────────
    @Transactional
    public void deleteItem(String username, Long id) {
        FoodItem item = findOwnedItem(username, id);
        item.setDeletedAt(LocalDateTime.now());
        foodRepo.save(item);
    }

    // ── Helpers ────────────────────────────────────────────────

    private FoodItem findOwnedItem(String username, Long id) {
        FoodItem item = foodRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        if (!item.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return item;
    }

    private FoodItem applyRequest(FoodItem item, FoodItemRequest request) {
        item.setName(request.name());
        item.setBrand(request.brand());
        item.setFoodType(request.foodType());
        item.setQuantity(request.quantity());
        item.setUnit(request.unit());
        item.setLocation(request.location());
        item.setExpirationDate(request.expirationDate());
        item.setPurchaseDate(request.purchaseDate());
        item.setOpenedAt(request.openedAt());
        item.setNotes(request.notes());
        return item;
    }
}
