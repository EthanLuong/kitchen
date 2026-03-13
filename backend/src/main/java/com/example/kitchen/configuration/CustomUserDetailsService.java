package com.example.kitchen.configuration;

import com.example.kitchen.data.User;
import com.example.kitchen.repository.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@NullMarked
@Component
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    public CustomUserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username);

        if(user == null) {
            throw new UsernameNotFoundException(username + " not found");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUserid().toString())
                .password(user.getPassword())
                .disabled(!user.isEnabled())
                .authorities(Collections.emptyList())
                .build();
    }
}
