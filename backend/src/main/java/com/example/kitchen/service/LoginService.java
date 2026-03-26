package com.example.kitchen.service;

import com.example.kitchen.data.RefreshToken;
import com.example.kitchen.data.User;
import com.example.kitchen.dto.AuthRequest;
import com.example.kitchen.dto.AuthResponse;
import com.example.kitchen.dto.RefreshRequest;
import com.example.kitchen.exception.UserAlreadyExistsException;
import com.example.kitchen.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class LoginService {

    private final AuthenticationManager auth;
    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshService;

    @Value("${jwt.expiration}")
    private int expirationTime;

    public LoginService(AuthenticationManager auth, UserRepository repo,
                        PasswordEncoder encoder, JwtService jwtService, RefreshTokenService refreshService) {
        this.auth = auth;
        this.repo = repo;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.refreshService = refreshService;
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        log.info("Attempting sign in for user with username {}", request.username());
        Authentication authentication = auth.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        RefreshToken token = refreshService.createRefreshToken(repo.findById(UUID.fromString(authentication.getName())).orElseThrow());

        return new AuthResponse(
                jwtService.createJWT(authentication.getName()), "Bearer", expirationTime, token.getToken().toString());
    }

    @Transactional
    public void signup(AuthRequest request) {
        log.info("Attempting to create new user with username {}", request.username());
        User existingUser = repo.findByUsername(request.username());
        if (existingUser != null) {
            throw new UserAlreadyExistsException();
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(encoder.encode(request.password()));
        repo.save(user);
    }

    public AuthResponse refreshAccess(RefreshRequest refreshToken){
        RefreshToken token = refreshService.validateToken(refreshToken.refreshToken());
        String jwtToken = jwtService.createJWT(token.getUser().getUserid().toString());
        return new AuthResponse(jwtToken, "Bearer", expirationTime, refreshToken.refreshToken().toString());
    }


    public void logout(UUID userid){
        refreshService.logoutUser(userid);
    }


}
