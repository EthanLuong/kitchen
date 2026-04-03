package com.example.kitchen.repository;

import com.example.kitchen.data.UserTypes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserTypeRepository extends JpaRepository<UserTypes, Long> {
    List<UserTypes> findByUserUserid(UUID userid);

}
