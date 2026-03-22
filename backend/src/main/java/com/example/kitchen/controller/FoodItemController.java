package com.example.kitchen.controller;

import com.example.kitchen.data.FoodItem;
import com.example.kitchen.dto.FoodItemRequest;
import com.example.kitchen.dto.FoodItemResponse;
import com.example.kitchen.service.FoodItemService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/items")
public class FoodItemController {

    private final FoodItemService service;

    public FoodItemController(FoodItemService service) {
        this.service = service;
    }

    // GET /items — all active items for the logged-in user
    @GetMapping
    public ResponseEntity<Page<FoodItemResponse>> getAll(Principal principal, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(service.getAllItems(userId(principal), PageRequest.of(page, size)));
    }

    // GET /items/{id}
    @GetMapping("/{id}")
    public ResponseEntity<FoodItemResponse> getOne(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(service.getItem(userId(principal), id));
    }

    // GET /items/expiring?days=7
    @GetMapping("/expiring")
    public ResponseEntity<List<FoodItemResponse>> getExpiring(@RequestParam(defaultValue = "7") int days, Principal principal) {
        return ResponseEntity.ok(service.getExpiringSoon(userId(principal), days));
    }

    // GET /items/location/{location}  e.g. FRIDGE, FREEZER, PANTRY
    @GetMapping("/location/{location}")
    public ResponseEntity<List<FoodItemResponse>> getByLocation(@PathVariable FoodItem.Location location, Principal principal) {
        return ResponseEntity.ok(service.getByLocation(userId(principal), location));
    }

    // GET /items/type/{type}  e.g. DAIRY, MEAT, PRODUCE
    @GetMapping("/type/{type}")
    public ResponseEntity<List<FoodItemResponse>> getByType(@PathVariable FoodItem.FoodType type, Principal principal) {
        return ResponseEntity.ok(service.getByFoodType(userId(principal), type));
    }

    // POST /items
    @PostMapping
    public ResponseEntity<FoodItemResponse> create(@Valid @RequestBody FoodItemRequest item, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addItem(userId(principal), item));
    }

    // PUT /items/{id}
    @PutMapping("/{id}")
    public ResponseEntity<FoodItemResponse> update(@PathVariable Long id, @Valid @RequestBody FoodItemRequest item, Principal principal) {
        return ResponseEntity.ok(service.updateItem(userId(principal), id, item));
    }

    // PATCH /items/{id}/consume
    @PatchMapping("/{id}/consume")
    public ResponseEntity<FoodItemResponse> consume(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(service.markConsumed(userId(principal), id));
    }

    // DELETE /items/{id}  (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        service.deleteItem(userId(principal), id);
        return ResponseEntity.noContent().build();
    }

    private static UUID userId(Principal principal) {
        return UUID.fromString(principal.getName());
    }
}
