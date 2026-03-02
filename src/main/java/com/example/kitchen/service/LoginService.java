package com.example.kitchen.service;

import com.example.kitchen.data.User;
import com.example.kitchen.exception.UserAlreadyExistsException;
import com.example.kitchen.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final AuthenticationManager auth;
    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public LoginService(AuthenticationManager auth, UserRepository repo,
                        PasswordEncoder encoder, JwtService jwtService) {
        this.auth = auth;
        this.repo = repo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public ResponseEntity<AuthResponse> login(User user) {
        Authentication authentication = auth.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

        AuthResponse response = new AuthResponse(
                jwtService.createJWT(authentication.getName()), "Bearer", 3600);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> signup(User user) {
        User existingUser = repo.findByUsername(user.getUsername());
        if (existingUser != null) {
            throw new UserAlreadyExistsException();
        }
        user.setPassword(encoder.encode(user.getPassword()));
        repo.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public record AuthResponse(String accessToken, String tokenType, long expiresIn) {}
}
