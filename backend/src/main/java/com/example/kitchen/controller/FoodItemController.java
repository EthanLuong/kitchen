package com.example.kitchen.controller;

import com.example.kitchen.dto.FoodItemRequest;
import com.example.kitchen.dto.FoodItemResponse;
import com.example.kitchen.service.FoodItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@Tag(name = "Food Items", description = "Manage food items in the kitchen inventory")
@Validated
@RestController
@RequestMapping("/v1/items")
public class FoodItemController {

    private final FoodItemService service;

    public FoodItemController(FoodItemService service) {
        this.service = service;
    }

    @Operation(summary = "Get all active items", description = "Returns a paginated list of non-consumed, non-deleted items for the authenticated user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Items retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    })
    @GetMapping
    public ResponseEntity<Page<FoodItemResponse>> getAll(
            Principal principal,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(service.getAllItems(ControllerUtils.userId(principal), PageRequest.of(page, size)));
    }

    @Operation(summary = "Get a single item by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item found"),
        @ApiResponse(responseCode = "403", description = "Item belongs to another user"),
        @ApiResponse(responseCode = "404", description = "Item not found"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FoodItemResponse> getOne(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(service.getItem(ControllerUtils.userId(principal), id));
    }

    @Operation(summary = "Get items expiring soon", description = "Returns items expiring within the given number of days (default 7, max 365)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Items retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid days parameter"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    })
    @GetMapping("/expiring")
    public ResponseEntity<List<FoodItemResponse>> getExpiring(
            @RequestParam(defaultValue = "7") @Min(1) @Max(365) int days,
            Principal principal) {
        return ResponseEntity.ok(service.getExpiringSoon(ControllerUtils.userId(principal), days));
    }

    @Operation(summary = "Get items by location", description = "Filter active items by storage location (e.g. FRIDGE, FREEZER, PANTRY)")
    @ApiResponse(responseCode = "200", description = "Items retrieved successfully")
    @GetMapping("/location/{location}")
    public ResponseEntity<List<FoodItemResponse>> getByLocation(@PathVariable String location, Principal principal) {
        return ResponseEntity.ok(service.getByLocation(ControllerUtils.userId(principal), location));
    }

    @Operation(summary = "Get items by food type", description = "Filter active items by food type (e.g. DAIRY, MEAT, PRODUCE)")
    @ApiResponse(responseCode = "200", description = "Items retrieved successfully")
    @GetMapping("/type/{type}")
    public ResponseEntity<List<FoodItemResponse>> getByType(@PathVariable String type, Principal principal) {
        return ResponseEntity.ok(service.getByFoodType(ControllerUtils.userId(principal), type));
    }

    @Operation(summary = "Add a new food item")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Item created — Location header contains the new item URL"),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    })
    @PostMapping
    public ResponseEntity<FoodItemResponse> create(@Valid @RequestBody FoodItemRequest item, Principal principal) {
        FoodItemResponse created = service.addItem(ControllerUtils.userId(principal), item);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Update a food item")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "403", description = "Item belongs to another user"),
        @ApiResponse(responseCode = "404", description = "Item not found"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    })
    @PutMapping("/{id}")
    public ResponseEntity<FoodItemResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody FoodItemRequest item,
            Principal principal) {
        return ResponseEntity.ok(service.updateItem(ControllerUtils.userId(principal), id, item));
    }

    @Operation(summary = "Mark an item as consumed")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item marked as consumed"),
        @ApiResponse(responseCode = "403", description = "Item belongs to another user"),
        @ApiResponse(responseCode = "404", description = "Item not found"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    })
    @PatchMapping("/{id}/consume")
    public ResponseEntity<FoodItemResponse> consume(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(service.markConsumed(ControllerUtils.userId(principal), id));
    }

    @Operation(summary = "Delete a food item", description = "Soft-deletes the item — it will no longer appear in active item queries")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Item deleted"),
        @ApiResponse(responseCode = "403", description = "Item belongs to another user"),
        @ApiResponse(responseCode = "404", description = "Item not found"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        service.deleteItem(ControllerUtils.userId(principal), id);
        return ResponseEntity.noContent().build();
    }
}
