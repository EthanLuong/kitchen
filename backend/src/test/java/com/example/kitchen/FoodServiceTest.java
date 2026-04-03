package com.example.kitchen;

import com.example.kitchen.data.FoodItem;
import com.example.kitchen.dto.FoodItemRequest;
import com.example.kitchen.dto.FoodItemResponse;
import com.example.kitchen.dto.UserLocationResponse;
import com.example.kitchen.dto.UserTypeResponse;
import com.example.kitchen.repository.FoodItemRepository;
import com.example.kitchen.repository.UserRepository;
import com.example.kitchen.service.FoodItemService;
import com.example.kitchen.service.UserPreferencesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.kitchen.data.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FoodServiceTest {
    @Mock
    private FoodItemRepository foodRepo;
    @Mock
    private UserRepository userRepo;
    @Mock
    private UserPreferencesService userPreferencesService;

    @InjectMocks
    FoodItemService service;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID OTHER_USER_ID = UUID.randomUUID();

    private User makeUser(UUID id) {
        User user = new User();
        user.setUserid(id);
        user.setUsername("testuser");
        user.setPassword("encoded");
        return user;
    }

    private FoodItem makeItem(User owner) {
        FoodItem item = new FoodItem();
        item.setId(1L);
        item.setUser(owner);
        item.setName("Milk");
        item.setFoodType("DAIRY");
        item.setQuantity(1.0);
        item.setUnit(FoodItem.Unit.L);
        item.setLocation("FRIDGE");
        item.setExpirationDate(LocalDate.now().plusDays(7));
        return item;
    }

    @BeforeEach
    public void setup(){

    }

    // ── getAllItems ──────────────────────────────────────────

    @Test
    public void getAllItems_returnsList() {
        FoodItem item = makeItem(makeUser(USER_ID));
        List<FoodItem> list = new ArrayList<>();
        list.add(item);
        Page<FoodItem> page = new PageImpl<>(list);
        when(foodRepo.findByUserUseridAndDeletedAtIsNullAndConsumedFalse(any(), any())).thenReturn(page);
        Page<FoodItemResponse> responseList = service.getAllItems(USER_ID, PageRequest.of(0,50));
        assertEquals(list.get(0).getId(), responseList.toList().get(0).id());
    }

    // ── getItem ─────────────────────────────────────────────

    @Test
    public void getItem_found() {
        User currentUser = makeUser(USER_ID);
        FoodItem item = makeItem(currentUser);
        when(foodRepo.findById(any())).thenReturn(Optional.of(item));
        assertEquals(item.getId(), service.getItem(USER_ID, item.getId()).id());
    }

    @Test
    public void getItem_notFound_throws404() {

        when(foodRepo.findById(any())).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, ()-> service.getItem(USER_ID, 1L));
    }

    @Test
    public void getItem_wrongOwner_throws403() {
        User ownerUser = makeUser(USER_ID);
        FoodItem item = makeItem(ownerUser);
        when(foodRepo.findById(any())).thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, ()-> service.getItem(OTHER_USER_ID, item.getId()));

    }

    // ── getExpiringSoon ─────────────────────────────────────

    @Test
    public void getExpiringSoon_returnsList() {
        FoodItem item = makeItem(makeUser(USER_ID));
        when(foodRepo.findExpiringSoon(any(), any())).thenReturn(List.of(item));
        List<FoodItemResponse> result = service.getExpiringSoon(USER_ID, 7);
        assertEquals(1, result.size());
        assertEquals(item.getId(), result.get(0).id());
    }

    // ── getByLocation ───────────────────────────────────────

    @Test
    public void getByLocation_returnsList() {
        FoodItem item = makeItem(makeUser(USER_ID));
        when(foodRepo.findByUserUseridAndLocationAndDeletedAtIsNullAndConsumedFalse(any(), any())).thenReturn(List.of(item));
        List<FoodItemResponse> result = service.getByLocation(USER_ID, "FRIDGE");
        assertEquals(1, result.size());
        assertEquals(item.getId(), result.get(0).id());
    }

    // ── getByFoodType ───────────────────────────────────────

    @Test
    public void getByFoodType_returnsList() {
        FoodItem item = makeItem(makeUser(USER_ID));
        when(foodRepo.findByUserUseridAndFoodTypeAndDeletedAtIsNullAndConsumedFalse(any(), any())).thenReturn(List.of(item));
        List<FoodItemResponse> result = service.getByFoodType(USER_ID, "DAIRY");
        assertEquals(1, result.size());
        assertEquals(item.getId(), result.get(0).id());
    }

    // ── addItem ─────────────────────────────────────────────

    @Test
    public void addItem_success() {
        List<UserTypeResponse> types = new ArrayList<>();
        List<UserLocationResponse> locations = new ArrayList<>();
        types.add(new UserTypeResponse(1L, "DAIRY"));
        locations.add(new UserLocationResponse(1L, "FRIDGE"));

        when(userPreferencesService.getUserLocations(any())).thenReturn(locations);
        when(userPreferencesService.getUserTypes(any())).thenReturn(types);

        User user = makeUser(USER_ID);
        FoodItem saved = makeItem(user);
        FoodItemRequest request = new FoodItemRequest("Milk", "DAIRY", 1.0, FoodItem.Unit.L,
                "FRIDGE", LocalDate.now().plusDays(7), null, null, null);
        when(userRepo.findById(USER_ID)).thenReturn(Optional.of(user));
        when(foodRepo.save(any())).thenReturn(saved);
        FoodItemResponse response = service.addItem(USER_ID, request);
        assertEquals(saved.getId(), response.id());
        assertEquals("Milk", response.name());
    }

    @Test
    public void addItem_userNotFound_throws404() {
        List<UserTypeResponse> types = new ArrayList<>();
        List<UserLocationResponse> locations = new ArrayList<>();
        types.add(new UserTypeResponse(1L, "DAIRY"));
        locations.add(new UserLocationResponse(1L, "FRIDGE"));

        when(userPreferencesService.getUserLocations(any())).thenReturn(locations);
        when(userPreferencesService.getUserTypes(any())).thenReturn(types);

        FoodItemRequest request = new FoodItemRequest("Milk", "DAIRY", 1.0, FoodItem.Unit.L,
                "FRIDGE", null, null, null, null);
        when(userRepo.findById(USER_ID)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.addItem(USER_ID, request));
    }

    // ── updateItem ──────────────────────────────────────────

    @Test
    public void updateItem_success() {
        List<UserTypeResponse> types = new ArrayList<>();
        List<UserLocationResponse> locations = new ArrayList<>();
        types.add(new UserTypeResponse(1L, "DAIRY"));
        locations.add(new UserLocationResponse(1L, "FRIDGE"));

        when(userPreferencesService.getUserLocations(any())).thenReturn(locations);
        when(userPreferencesService.getUserTypes(any())).thenReturn(types);

        User user = makeUser(USER_ID);
        FoodItem existing = makeItem(user);
        FoodItem updated = makeItem(user);
        updated.setName("Oat Milk");
        FoodItemRequest request = new FoodItemRequest("Oat Milk", "DAIRY", 1.0, FoodItem.Unit.L,
                "FRIDGE", LocalDate.now().plusDays(7), null, null, null);
        when(foodRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(foodRepo.save(any())).thenReturn(updated);
        FoodItemResponse response = service.updateItem(USER_ID, 1L, request);
        assertEquals("Oat Milk", response.name());
    }

    @Test
    public void updateItem_notFound_throws404() {
        List<UserTypeResponse> types = new ArrayList<>();
        List<UserLocationResponse> locations = new ArrayList<>();
        types.add(new UserTypeResponse(1L, "DAIRY"));
        locations.add(new UserLocationResponse(1L, "FRIDGE"));

        when(userPreferencesService.getUserLocations(any())).thenReturn(locations);
        when(userPreferencesService.getUserTypes(any())).thenReturn(types);
        FoodItemRequest request = new FoodItemRequest("Milk", "DAIRY", 1.0, FoodItem.Unit.L,
                "FRIDGE", null, null, null, null);
        when(foodRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.updateItem(USER_ID, 1L, request));
    }

    @Test
    public void updateItem_wrongOwner_throws403() {
        List<UserTypeResponse> types = new ArrayList<>();
        List<UserLocationResponse> locations = new ArrayList<>();
        types.add(new UserTypeResponse(1L, "DAIRY"));
        locations.add(new UserLocationResponse(1L, "FRIDGE"));

        when(userPreferencesService.getUserLocations(any())).thenReturn(locations);
        when(userPreferencesService.getUserTypes(any())).thenReturn(types);
        User owner = makeUser(USER_ID);
        FoodItem item = makeItem(owner);
        FoodItemRequest request = new FoodItemRequest("Milk", "DAIRY", 1.0, FoodItem.Unit.L,
                "FRIDGE", null, null, null, null);
        when(foodRepo.findById(1L)).thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, () -> service.updateItem(OTHER_USER_ID, 1L, request));
    }

    // ── markConsumed ────────────────────────────────────────

    @Test
    public void markConsumed_success() {
        User user = makeUser(USER_ID);
        FoodItem item = makeItem(user);
        FoodItem consumed = makeItem(user);
        consumed.setConsumed(true);
        when(foodRepo.findById(1L)).thenReturn(Optional.of(item));
        when(foodRepo.save(any())).thenReturn(consumed);
        FoodItemResponse response = service.markConsumed(USER_ID, 1L);
        assertTrue(response.consumed());
    }

    @Test
    public void markConsumed_notFound_throws404() {
        when(foodRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.markConsumed(USER_ID, 1L));
    }

    @Test
    public void markConsumed_wrongOwner_throws403() {
        User owner = makeUser(USER_ID);
        FoodItem item = makeItem(owner);
        when(foodRepo.findById(1L)).thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, () -> service.markConsumed(OTHER_USER_ID, 1L));
    }

    // ── deleteItem ──────────────────────────────────────────

    @Test
    public void deleteItem_success() {
        User user = makeUser(USER_ID);
        FoodItem item = makeItem(user);
        when(foodRepo.findById(1L)).thenReturn(Optional.of(item));
        service.deleteItem(USER_ID, 1L);
        verify(foodRepo).save(item);
        assertNotNull(item.getDeletedAt());
    }

    @Test
    public void deleteItem_notFound_throws404() {
        when(foodRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.deleteItem(USER_ID, 1L));
    }

    @Test
    public void deleteItem_wrongOwner_throws403() {
        User owner = makeUser(USER_ID);
        FoodItem item = makeItem(owner);
        when(foodRepo.findById(1L)).thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class, () -> service.deleteItem(OTHER_USER_ID, 1L));
    }
}
