package com.example.kitchen.repository;

import com.example.kitchen.data.UserLocations;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserLocationRepository extends JpaRepository<UserLocations, Long> {
    List<UserLocations> findByUserUserid(UUID userid);
}
