package com.example.kitchen.repository;

import com.example.kitchen.data.ItemDefaults;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemDefaultsRepository extends JpaRepository<ItemDefaults, Long> {
    Optional<ItemDefaults> findByUserUseridAndName(UUID userid, String name);
    List<ItemDefaults> findAllByUserUserid(UUID userid);

}
