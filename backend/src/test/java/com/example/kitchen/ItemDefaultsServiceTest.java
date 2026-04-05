package com.example.kitchen;

import com.example.kitchen.data.ItemDefaults;
import com.example.kitchen.data.User;
import com.example.kitchen.dto.ItemDefaultsResponse;
import com.example.kitchen.exception.DefaultsNotFoundException;
import com.example.kitchen.repository.ItemDefaultsRepository;
import com.example.kitchen.repository.UserRepository;
import com.example.kitchen.service.ItemDefaultsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemDefaultsServiceTest {
    @Mock
    ItemDefaultsRepository repo;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ItemDefaultsService service;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void upsert_newItem_createsDefault() {
        User user = new User();
        user.setUserid(USER_ID);
        when(repo.findByUserUseridAndName(USER_ID, "MILK")).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(USER_ID)).thenReturn(user);

        service.upsert(USER_ID, "MILK", "DAIRY", "LITRE", "FRIDGE", 7);

        ArgumentCaptor<ItemDefaults> argument = ArgumentCaptor.forClass(ItemDefaults.class);

        verify(repo).save(argument.capture());
        assertEquals(USER_ID, argument.getValue().getUser().getUserid());
        assertEquals("MILK", argument.getValue().getName());
        assertEquals("DAIRY", argument.getValue().getFoodType());
        assertEquals("LITRE", argument.getValue().getUnit());
        assertEquals("FRIDGE", argument.getValue().getLocation());
        assertEquals(7, argument.getValue().getExpirationDays());
    }

    @Test
    void upsert_existingItem_updatesDefault() {
        ItemDefaults existing = new ItemDefaults();
        existing.setName("MILK");
        existing.setFoodType("DAIRY");
        existing.setUnit("LITRE");
        existing.setLocation("FRIDGE");
        existing.setExpirationDays(7);

        when(repo.findByUserUseridAndName(USER_ID, "MILK")).thenReturn(Optional.of(existing));
        when(userRepository.getReferenceById(USER_ID)).thenReturn(new User());
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.upsert(USER_ID, "MILK", "DAIRY", "LITRE", "FREEZER", 14);

        ArgumentCaptor<ItemDefaults> argument = ArgumentCaptor.forClass(ItemDefaults.class);

        verify(repo).save(argument.capture());
        assertEquals("MILK", argument.getValue().getName());
        assertEquals("DAIRY", argument.getValue().getFoodType());
        assertEquals("LITRE", argument.getValue().getUnit());
        assertEquals("FREEZER", argument.getValue().getLocation());
        assertEquals(14, argument.getValue().getExpirationDays());

    }

    @Test
    void upsert_withNullExpiryDays_savesNull() {
        when(repo.findByUserUseridAndName(USER_ID, "MILK")).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(USER_ID)).thenReturn(new User());
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.upsert(USER_ID, "MILK", "DAIRY", "LITRE", "FRIDGE", null);

        ArgumentCaptor<ItemDefaults> argument = ArgumentCaptor.forClass(ItemDefaults.class);


        verify(repo).save(argument.capture());
        assertNull(argument.getValue().getExpirationDays());
    }

    @Test
    void getDefaults_existingItem_returnsResponse() {
        ItemDefaults defaults = new ItemDefaults();
        defaults.setName("MILK");
        defaults.setFoodType("DAIRY");
        defaults.setUnit("LITRE");
        defaults.setLocation("FRIDGE");
        defaults.setExpirationDays(7);

        when(repo.findByUserUseridAndName(USER_ID, "MILK")).thenReturn(Optional.of(defaults));

        ItemDefaultsResponse response = service.getDefaults(USER_ID, "MILK");

        assertEquals("MILK", response.name());
        assertEquals("DAIRY", response.foodType());
        assertEquals("LITRE", response.unit());
        assertEquals("FRIDGE", response.location());
        assertEquals(7, response.expirationDays());
    }

    @Test
    void getDefaults_nonExistentItem_throwsException() {
        when(repo.findByUserUseridAndName(USER_ID, "MILK")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDefaults(USER_ID, "MILK"))
                .isInstanceOf(DefaultsNotFoundException.class);
    }

    @Test
    void getAllDefaults_returnsListForUser() {
        ItemDefaults d1 = new ItemDefaults();
        d1.setName("MILK");
        ItemDefaults d2 = new ItemDefaults();
        d2.setName("EGGS");

        when(repo.findAllByUserUserid(USER_ID)).thenReturn(List.of(d1, d2));

        List<ItemDefaultsResponse> result = service.getAllDefaults(USER_ID);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ItemDefaultsResponse::name)
                .containsExactly("MILK", "EGGS");
    }

    @Test
    void getAllDefaults_noDefaults_returnsEmptyList() {
        when(repo.findAllByUserUserid(USER_ID)).thenReturn(List.of());

        List<ItemDefaultsResponse> result = service.getAllDefaults(USER_ID);

        assertThat(result).isEmpty();
    }
}
