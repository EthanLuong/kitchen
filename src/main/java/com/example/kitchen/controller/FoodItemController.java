package com.example.kitchen.controller;

import com.example.kitchen.data.FoodItem;
import com.example.kitchen.service.FoodItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
public class FoodItemController {

    private final FoodItemService service;

    public FoodItemController(FoodItemService service) {
        this.service = service;
    }

    // GET /items — all active items for the logged-in user
    @GetMapping
    public ResponseEntity<List<FoodItem>> getAll() {
        return ResponseEntity.ok(service.getAllItems(getUsername()));
    }

    // GET /items/{id}
    @GetMapping("/{id}")
    public ResponseEntity<FoodItem> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.getItem(getUsername(), id));
    }

    // GET /items/expiring?days=7
    @GetMapping("/expiring")
    public ResponseEntity<List<FoodItem>> getExpiring(@RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(service.getExpiringSoon(getUsername(), days));
    }

    // GET /items/location/{location}  e.g. FRIDGE, FREEZER, PANTRY
    @GetMapping("/location/{location}")
    public ResponseEntity<List<FoodItem>> getByLocation(@PathVariable FoodItem.Location location) {
        return ResponseEntity.ok(service.getByLocation(getUsername(), location));
    }

    // GET /items/type/{type}  e.g. DAIRY, MEAT, PRODUCE
    @GetMapping("/type/{type}")
    public ResponseEntity<List<FoodItem>> getByType(@PathVariable FoodItem.FoodType type) {
        return ResponseEntity.ok(service.getByFoodType(getUsername(), type));
    }

    // POST /items
    @PostMapping
    public ResponseEntity<FoodItem> create(@RequestBody FoodItem item) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addItem(getUsername(), item));
    }

    // PUT /items/{id}
    @PutMapping("/{id}")
    public ResponseEntity<FoodItem> update(@PathVariable Long id, @RequestBody FoodItem item) {
        return ResponseEntity.ok(service.updateItem(getUsername(), id, item));
    }

    // PATCH /items/{id}/consume
    @PatchMapping("/{id}/consume")
    public ResponseEntity<FoodItem> consume(@PathVariable Long id) {
        return ResponseEntity.ok(service.markConsumed(getUsername(), id));
    }

    // DELETE /items/{id}  (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteItem(getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    // ── Helper ─────────────────────────────────────────────────

    private String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
