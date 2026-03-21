package com.example.kitchen.service;

import com.example.kitchen.data.FoodItem;
import com.example.kitchen.data.User;
import com.example.kitchen.dto.FoodItemRequest;
import com.example.kitchen.dto.FoodItemResponse;
import com.example.kitchen.repository.FoodItemRepository;
import com.example.kitchen.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Transactional(readOnly = true)
@Slf4j
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
    public FoodItemResponse addItem(UUID userId, FoodItemRequest request) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        FoodItem item = applyRequest(new FoodItem(), request);
        item.setUser(user);

        return FoodItemResponse.from(foodRepo.save(item));
    }

    

    // ── Read ───────────────────────────────────────────────────

    public List<FoodItemResponse> getAllItems(UUID userId, Pageable pageable) {

        return foodRepo.findByUserUseridAndDeletedAtIsNullAndConsumedFalse(userId, pageable)
                .stream().map(FoodItemResponse::from).toList();
    }

    public FoodItemResponse getItem(UUID userId, Long id) {
        return FoodItemResponse.from(findOwnedItem(userId, id));
    }

    public List<FoodItemResponse> getExpiringSoon(UUID userId, int days) {
        LocalDate cutoff = LocalDate.now().plusDays(days);
        return foodRepo.findExpiringSoon(userId, cutoff)
                .stream().map(FoodItemResponse::from).toList();
    }

    public List<FoodItemResponse> getByLocation(UUID userId, FoodItem.Location location) {
        return foodRepo.findByUserUseridAndLocationAndDeletedAtIsNullAndConsumedFalse(userId, location)
                .stream().map(FoodItemResponse::from).toList();
    }

    public List<FoodItemResponse> getByFoodType(UUID userId, FoodItem.FoodType type) {
        return foodRepo.findByUserUseridAndFoodTypeAndDeletedAtIsNullAndConsumedFalse(userId, type)
                .stream().map(FoodItemResponse::from).toList();
    }

    // ── Update ─────────────────────────────────────────────────
    @Transactional
    public FoodItemResponse updateItem(UUID userId, Long id, FoodItemRequest request) {
        FoodItem existing = findOwnedItem(userId, id);
        applyRequest(existing, request);
        return FoodItemResponse.from(foodRepo.save(existing));
    }
    
    @Transactional
    public FoodItemResponse markConsumed(UUID userId, Long id) {
        FoodItem item = findOwnedItem(userId, id);
        item.setConsumed(true);
        return FoodItemResponse.from(foodRepo.save(item));
    }

    // ── Delete (soft) ──────────────────────────────────────────
    @Transactional
    public void deleteItem(UUID userId, Long id) {
        FoodItem item = findOwnedItem(userId, id);
        item.setDeletedAt(LocalDateTime.now());
        foodRepo.save(item);
    }

    // ── Helpers ────────────────────────────────────────────────

    private FoodItem findOwnedItem(UUID userId, Long id) {
        FoodItem item = foodRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        if (!item.getUser().getUserid().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return item;
    }

    private FoodItem applyRequest(FoodItem item, FoodItemRequest request) {
        item.setName(request.name());
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
