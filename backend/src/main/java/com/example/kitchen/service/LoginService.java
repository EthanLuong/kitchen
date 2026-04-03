package com.example.kitchen.service;

import com.example.kitchen.data.RefreshToken;
import com.example.kitchen.data.User;
import com.example.kitchen.dto.AuthRequest;
import com.example.kitchen.dto.AuthResponse;
import com.example.kitchen.dto.LoginResult;
import com.example.kitchen.event.UserCreatedEvent;
import com.example.kitchen.exception.UserAlreadyExistsException;
import com.example.kitchen.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    @Value("${jwt.expiration}")
    private int expirationTime;

    public LoginService(AuthenticationManager auth, UserRepository repo,
                        PasswordEncoder encoder, JwtService jwtService, RefreshTokenService refreshService, ApplicationEventPublisher eventPublisher) {
        this.auth = auth;
        this.repo = repo;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.refreshService = refreshService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public LoginResult login(AuthRequest request) {
        log.info("Attempting sign in for user with username {}", request.username());
        Authentication authentication = auth.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        RefreshToken token = refreshService.createRefreshToken(repo.findById(UUID.fromString(authentication.getName())).orElseThrow());

        AuthResponse response =  new AuthResponse(
                jwtService.createJWT(authentication.getName()), "Bearer", expirationTime);

        return new LoginResult(response, token.getToken().toString());
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

        User createdUser = repo.save(user);

        UserCreatedEvent newUserEvent = new UserCreatedEvent(this, createdUser.getUserid());

        eventPublisher.publishEvent(newUserEvent);
    }

    public AuthResponse refreshAccess(String refreshToken){
        RefreshToken token = refreshService.validateToken(UUID.fromString(refreshToken));
        String jwtToken = jwtService.createJWT(token.getUser().getUserid().toString());
        return new AuthResponse(jwtToken, "Bearer", expirationTime);
    }


    public void logout(UUID userid){
        refreshService.logoutUser(userid);
    }


}
