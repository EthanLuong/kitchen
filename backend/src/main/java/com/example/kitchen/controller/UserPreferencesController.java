package com.example.kitchen.controller;

import com.example.kitchen.dto.ItemDefaultsResponse;
import com.example.kitchen.dto.PreferenceRequest;
import com.example.kitchen.dto.UserLocationResponse;
import com.example.kitchen.dto.UserTypeResponse;
import com.example.kitchen.exception.DefaultsNotFoundException;
import com.example.kitchen.service.ItemDefaultsService;
import com.example.kitchen.service.UserPreferencesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.usertype.UserType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/user")
@Slf4j
public class UserPreferencesController {

    private final UserPreferencesService preferencesService;
    private final ItemDefaultsService defaultsService;

    public UserPreferencesController(UserPreferencesService preferencesService, ItemDefaultsService defaultsService){
    this.preferencesService = preferencesService;
    this.defaultsService = defaultsService;
}

    @GetMapping("/types")
    public ResponseEntity<List<UserTypeResponse>> getTypes(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        return ResponseEntity.ok(preferencesService.getUserTypes(userId));
    }

    @GetMapping("/locations")
    public ResponseEntity<List<UserLocationResponse>> getLocations(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        return ResponseEntity.ok(preferencesService.getUserLocations(userId));
    }

    @PostMapping("/types")
    public ResponseEntity<UserTypeResponse> addType(@RequestBody @Valid PreferenceRequest request,
                                               Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(preferencesService.addType(userId, request.name()));
    }

    @PostMapping("/locations")
    public ResponseEntity<UserLocationResponse> addLocation(@RequestBody @Valid PreferenceRequest request,
                                                       Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(preferencesService.addLocation(userId, request.name()));
    }

    @DeleteMapping("/types/{id}")
    public ResponseEntity<Void> deleteType(@PathVariable Long id, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        preferencesService.deleteType(userId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/locations/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        preferencesService.deleteLocation(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/item-defaults")
    public ResponseEntity<List<ItemDefaultsResponse>> getAllItemDefaults(Principal principal){
        UUID userId = UUID.fromString(principal.getName());
        return ResponseEntity.ok().body(defaultsService.getAllDefaults(userId));
    }

    @GetMapping("/item-defaults/{name}")
    public ResponseEntity<ItemDefaultsResponse> getItemDefaults(@PathVariable String name, Principal principal){
        try{
            return ResponseEntity.ok(defaultsService.getDefaults(UUID.fromString(principal.getName()),name));
        } catch (DefaultsNotFoundException ex){
            return ResponseEntity.noContent().build();
        }

    }
    }