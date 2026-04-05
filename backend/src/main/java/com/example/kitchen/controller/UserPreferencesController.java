package com.example.kitchen.controller;

import com.example.kitchen.dto.ItemDefaultsResponse;
import com.example.kitchen.dto.PreferenceRequest;
import com.example.kitchen.dto.UserLocationResponse;
import com.example.kitchen.dto.UserTypeResponse;
import com.example.kitchen.service.ItemDefaultsService;
import com.example.kitchen.service.UserPreferencesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@Tag(name = "User Preferences", description = "Manage custom food types, storage locations, and item defaults")
@Slf4j
@RestController
@RequestMapping("/v1/user")
public class UserPreferencesController {

    private final UserPreferencesService preferencesService;
    private final ItemDefaultsService defaultsService;

    public UserPreferencesController(UserPreferencesService preferencesService, ItemDefaultsService defaultsService) {
        this.preferencesService = preferencesService;
        this.defaultsService = defaultsService;
    }

    @Operation(summary = "Get all food types for the current user")
    @ApiResponse(responseCode = "200", description = "Types retrieved successfully")
    @GetMapping("/types")
    public ResponseEntity<List<UserTypeResponse>> getTypes(Principal principal) {
        return ResponseEntity.ok(preferencesService.getUserTypes(ControllerUtils.userId(principal)));
    }

    @Operation(summary = "Get all storage locations for the current user")
    @ApiResponse(responseCode = "200", description = "Locations retrieved successfully")
    @GetMapping("/locations")
    public ResponseEntity<List<UserLocationResponse>> getLocations(Principal principal) {
        return ResponseEntity.ok(preferencesService.getUserLocations(ControllerUtils.userId(principal)));
    }

    @Operation(summary = "Add a custom food type")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Type created"),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "409", description = "Type already exists for this user")
    })
    @PostMapping("/types")
    public ResponseEntity<UserTypeResponse> addType(@RequestBody @Valid PreferenceRequest request, Principal principal) {
        UserTypeResponse created = preferencesService.addType(ControllerUtils.userId(principal), request.name());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Add a custom storage location")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Location created"),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "409", description = "Location already exists for this user")
    })
    @PostMapping("/locations")
    public ResponseEntity<UserLocationResponse> addLocation(@RequestBody @Valid PreferenceRequest request, Principal principal) {
        UserLocationResponse created = preferencesService.addLocation(ControllerUtils.userId(principal), request.name());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Delete a food type")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Type deleted"),
        @ApiResponse(responseCode = "403", description = "Type belongs to another user"),
        @ApiResponse(responseCode = "404", description = "Type not found")
    })
    @DeleteMapping("/types/{id}")
    public ResponseEntity<Void> deleteType(@PathVariable Long id, Principal principal) {
        preferencesService.deleteType(ControllerUtils.userId(principal), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a storage location")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Location deleted"),
        @ApiResponse(responseCode = "403", description = "Location belongs to another user"),
        @ApiResponse(responseCode = "404", description = "Location not found")
    })
    @DeleteMapping("/locations/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id, Principal principal) {
        preferencesService.deleteLocation(ControllerUtils.userId(principal), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all item defaults", description = "Returns saved defaults (type, unit, location, expiry days) for all previously added items")
    @ApiResponse(responseCode = "200", description = "Defaults retrieved successfully")
    @GetMapping("/item-defaults")
    public ResponseEntity<List<ItemDefaultsResponse>> getAllItemDefaults(Principal principal) {
        return ResponseEntity.ok(defaultsService.getAllDefaults(ControllerUtils.userId(principal)));
    }

    @Operation(summary = "Get item defaults by name", description = "Returns saved defaults for a specific item name, used for autocomplete pre-fill")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Defaults found"),
        @ApiResponse(responseCode = "404", description = "No defaults found for that item name")
    })
    @GetMapping("/item-defaults/{name}")
    public ResponseEntity<ItemDefaultsResponse> getItemDefaults(@PathVariable String name, Principal principal) {
        return ResponseEntity.ok(defaultsService.getDefaults(ControllerUtils.userId(principal), name));
    }
}
