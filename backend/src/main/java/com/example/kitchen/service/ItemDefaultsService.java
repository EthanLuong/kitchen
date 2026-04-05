package com.example.kitchen.service;

import com.example.kitchen.data.ItemDefaults;
import com.example.kitchen.data.User;
import com.example.kitchen.dto.ItemDefaultsResponse;
import com.example.kitchen.exception.DefaultsNotFoundException;
import com.example.kitchen.repository.ItemDefaultsRepository;
import com.example.kitchen.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ItemDefaultsService {

    private final ItemDefaultsRepository defaultsRepository;
    private final UserRepository userRepository;

    public ItemDefaultsService(ItemDefaultsRepository defaultsRepository, UserRepository userRepository){
        this.defaultsRepository = defaultsRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void upsert(UUID userId, String name, String foodType, String unit, String location, Integer expiryDays){
        ItemDefaults defaults = defaultsRepository.findByUserUseridAndName(userId, name).orElse(new ItemDefaults());
        User user = userRepository.getReferenceById(userId);
        defaults.setUser(user);
        defaults.setName(name);
        defaults.setFoodType(foodType);
        defaults.setUnit(unit);
        defaults.setLocation(location);
        defaults.setExpirationDays(expiryDays);
        defaultsRepository.save(defaults);
    }
    public ItemDefaultsResponse getDefaults(UUID userid, String name){
        ItemDefaults defaults = defaultsRepository.findByUserUseridAndName(userid, name).orElseThrow(()-> new DefaultsNotFoundException("Item defaults with name " + name + " does not exist"));
        return new ItemDefaultsResponse(defaults.getName(), defaults.getFoodType(), defaults.getUnit(), defaults.getLocation(), defaults.getExpirationDays());
    }

    public List<ItemDefaultsResponse> getAllDefaults( UUID userid){
        List<ItemDefaults> defaults = defaultsRepository.findAllByUserUserid(userid);
        return defaults.stream().map(item -> new ItemDefaultsResponse(item.getName(), item.getFoodType(), item.getUnit(), item.getLocation(), item.getExpirationDays())).toList();
    }
}
