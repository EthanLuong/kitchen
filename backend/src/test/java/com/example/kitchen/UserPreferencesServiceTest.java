package com.example.kitchen;

import com.example.kitchen.data.User;
import com.example.kitchen.data.UserLocations;
import com.example.kitchen.data.UserTypes;
import com.example.kitchen.dto.UserLocationResponse;
import com.example.kitchen.dto.UserTypeResponse;
import com.example.kitchen.event.UserCreatedEvent;
import com.example.kitchen.exception.ResourceOwnershipException;
import com.example.kitchen.repository.UserLocationRepository;
import com.example.kitchen.repository.UserRepository;
import com.example.kitchen.repository.UserTypeRepository;
import com.example.kitchen.service.UserPreferencesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPreferencesServiceTest {

    @Mock
    UserTypeRepository typeRepo;

    @Mock
    UserLocationRepository locationRepo;

    @Mock
    UserRepository userRepo;

    @InjectMocks
    UserPreferencesService service;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OTHER_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserid(USER_ID);
    }

    @Test
    void getUserTypes_returnsListForUser() {
        UserTypes t1 = new UserTypes(testUser, "DAIRY");
        UserTypes t2 = new UserTypes(testUser, "MEAT");
        when(typeRepo.findByUserUserid(USER_ID)).thenReturn(List.of(t1, t2));

        List<UserTypeResponse> result = service.getUserTypes(USER_ID);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserTypeResponse::name)
                .containsExactly("DAIRY", "MEAT");
    }

    @Test
    void getUserTypes_noTypes_returnsEmptyList() {
        when(typeRepo.findByUserUserid(USER_ID)).thenReturn(List.of());

        List<UserTypeResponse> result = service.getUserTypes(USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void getUserLocations_returnsListForUser() {
        UserLocations l1 = new UserLocations(testUser, "FRIDGE");
        UserLocations l2 = new UserLocations(testUser, "FREEZER");
        when(locationRepo.findByUserUserid(USER_ID)).thenReturn(List.of(l1, l2));

        List<UserLocationResponse> result = service.getUserLocations(USER_ID);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserLocationResponse::name)
                .containsExactly("FRIDGE", "FREEZER");
    }

    @Test
    void getUserLocations_noLocations_returnsEmptyList() {
        when(locationRepo.findByUserUserid(USER_ID)).thenReturn(List.of());

        List<UserLocationResponse> result = service.getUserLocations(USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void addType_success_returnsResponse() {
        UserTypes saved = new UserTypes(testUser, "DAIRY");
        saved.setId(1L);
        when(typeRepo.save(any())).thenReturn(saved);

        UserTypeResponse result = service.addType(USER_ID, "DAIRY");

        assertThat(result.name()).isEqualTo("DAIRY");
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void addType_duplicate_throwsDataIntegrityViolationException() {
        when(typeRepo.save(any())).thenThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> service.addType(USER_ID, "DAIRY"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void addLocation_success_returnsResponse() {
        UserLocations saved = new UserLocations(testUser, "FRIDGE");
        saved.setId(1L);
        when(locationRepo.save(any())).thenReturn(saved);

        UserLocationResponse result = service.addLocation(USER_ID, "FRIDGE");

        assertThat(result.name()).isEqualTo("FRIDGE");
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void addLocation_duplicate_throwsDataIntegrityViolationException() {
        when(locationRepo.save(any())).thenThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> service.addLocation(USER_ID, "FRIDGE"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deleteType_ownedByUser_deletesSuccessfully() {
        UserTypes type = new UserTypes(testUser, "DAIRY");
        type.setId(1L);
        when(typeRepo.getReferenceById(1L)).thenReturn(type);

        service.deleteType(USER_ID, 1L);

        verify(typeRepo).delete(type);

    }

    @Test
    void deleteType_notOwnedByUser_throwsResourceOwnershipException() {
        User otherUser = new User();
        otherUser.setUserid(OTHER_USER_ID);
        UserTypes type = new UserTypes(otherUser, "DAIRY");
        type.setId(1L);
        when(typeRepo.getReferenceById(1L)).thenReturn(type);

        assertThatThrownBy(() -> service.deleteType(USER_ID, 1L))
                .isInstanceOf(ResourceOwnershipException.class);

        verify(typeRepo, never()).delete(any());
    }

    @Test
    void deleteLocation_ownedByUser_deletesSuccessfully() {
        UserLocations location = new UserLocations(testUser, "FRIDGE");
        location.setId(1L);
        when(locationRepo.getReferenceById(1L)).thenReturn(location);

        service.deleteLocation(USER_ID, 1L);

        verify(locationRepo).delete(location);
    }

    @Test
    void deleteLocation_notOwnedByUser_throwsResourceOwnershipException() {
        User otherUser = new User();
        otherUser.setUserid(OTHER_USER_ID);
        UserLocations location = new UserLocations(otherUser, "FRIDGE");
        location.setId(1L);
        when(locationRepo.getReferenceById(1L)).thenReturn(location);

        assertThatThrownBy(() -> service.deleteLocation(USER_ID, 1L))
                .isInstanceOf(ResourceOwnershipException.class);

        verify(locationRepo, never()).delete(any());
    }

    @Test
    void seedDefaults_savesAllDefaultTypesAndLocations() {
        service.seedDefaults(USER_ID);

        verify(typeRepo).saveAll(argThat(list ->
                ((List<?>) list).size() == 8));
        verify(locationRepo).saveAll(argThat(list ->
                ((List<?>) list).size() == 4));
    }

    @Test
    void handleNewUser_callsSeedDefaults() {
        UserCreatedEvent event = new UserCreatedEvent(this, USER_ID);

        service.handleNewUser(event);

        verify(typeRepo).saveAll(any());
        verify(locationRepo).saveAll(any());
    }
}