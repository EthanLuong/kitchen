package com.example.kitchen.service;

import com.example.kitchen.data.User;
import com.example.kitchen.data.UserLocations;
import com.example.kitchen.data.UserTypes;
import com.example.kitchen.dto.FoodItemRequest;
import com.example.kitchen.dto.UserLocationResponse;
import com.example.kitchen.dto.UserTypeResponse;
import com.example.kitchen.event.UserCreatedEvent;
import com.example.kitchen.exception.ResourceOwnershipException;
import com.example.kitchen.repository.UserLocationRepository;
import com.example.kitchen.repository.UserRepository;
import com.example.kitchen.repository.UserTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class UserPreferencesService {

    private final UserLocationRepository locationRepo;
    private final UserTypeRepository typeRepo;
    private final UserRepository userRepo;

    public UserPreferencesService(UserTypeRepository typeRepo, UserLocationRepository locationRepo, UserRepository userRepo){
        this.locationRepo = locationRepo;
        this.typeRepo = typeRepo;
        this.userRepo = userRepo;
    }

    public List<UserTypeResponse> getUserTypes(UUID user){
        log.info("Getting types for user with userid {}", user);

        List<UserTypes> userTypes = typeRepo.findByUserUserid(user);
        return userTypes.stream().map(userType -> new UserTypeResponse(userType.getId(), userType.getName())).toList();
    }

    public List<UserLocationResponse> getUserLocations(UUID user){
        log.info("Getting locations for user with userid {}", user);

        List<UserLocations> userLocations =  locationRepo.findByUserUserid(user);
        return userLocations.stream().map(userLocation -> new UserLocationResponse(userLocation.getId(), userLocation.getName())).toList();
    }

    @Transactional
    public UserTypeResponse addType(UUID userid, String type){
        log.info("Adding type {} for user with userid {}", type, userid);
        User user = userRepo.getReferenceById(userid);

        UserTypes userType = new UserTypes();
        userType.setUser(user);
        userType.setName(type);
        try {
            UserTypes result = typeRepo.save(userType);
            return  new UserTypeResponse(result.getId(), result.getName());
        } catch (DataIntegrityViolationException ex) {
            log.warn("Adding type for user with userid {} failed", userid, ex);
            throw ex;
        }

    }
    @Transactional
    public UserLocationResponse addLocation(UUID userid, String location){
        log.info("Adding location {} for user with userid {}", location, userid);
        User user = userRepo.getReferenceById(userid);

        UserLocations userLocation = new UserLocations();
        userLocation.setUser(user);
        userLocation.setName(location);

        UserLocations result = locationRepo.save(userLocation);

        return new UserLocationResponse(result.getId(), result.getName());
    }
    @Transactional
    public void deleteLocation(UUID userid, Long id){
        log.info("Deleting location with id {} for user with userid {}", id, userid);

        UserLocations location = locationRepo.getReferenceById(id);

        if(!location.getUser().getUserid().equals(userid)){
            throw new ResourceOwnershipException("Wrong user tried to access location");
        }

        locationRepo.delete(location);
    }
    @Transactional
    public void deleteType(UUID userid, Long id){
        log.info("Deleting type with id {} for user with userid {}", id, userid);

        UserTypes type = typeRepo.getReferenceById(id);

        if(!type.getUser().getUserid().equals(userid)) throw new ResourceOwnershipException("Wrong user tried to access type");

        typeRepo.delete(type);
    }
    @Transactional
    public void seedDefaults(UUID userId){
        log.info("Seeding default types and locations for user with userid {}", userId);
        User user = userRepo.getReferenceById(userId);

        typeRepo.saveAll(DEFAULT_TYPES.stream().map(name -> new UserTypes(user, name)).toList());
        locationRepo.saveAll(DEFAULT_LOCATIONS.stream().map(name -> new UserLocations(user, name)).toList());

    }



    @EventListener
    public void handleNewUser(UserCreatedEvent userCreatedEvent){
        seedDefaults(userCreatedEvent.getUserId());
    }

    private static final List<String> DEFAULT_TYPES = List.of(
            "DAIRY", "MEAT", "PRODUCE", "GRAIN", "FROZEN", "BEVERAGE", "SNACK", "OTHER"
    );

    private static final List<String> DEFAULT_LOCATIONS = List.of(
            "FRIDGE", "FREEZER", "PANTRY", "COUNTER"
    );




}
