package com.example.kitchen.service;

import com.example.kitchen.data.RefreshToken;
import com.example.kitchen.data.User;
import com.example.kitchen.repository.RefreshTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository tokenRepo;

    public RefreshTokenService(RefreshTokenRepository tokenRepo){
        this.tokenRepo=tokenRepo;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user){
        log.info("Creating new refresh token for user with userId {}", user.getUserid());
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID());
        token.setExpiryTime(LocalDateTime.now().plusHours(8));
        return tokenRepo.save(token);
    }

    public RefreshToken validateToken(UUID token){
        return tokenRepo.findByToken(token).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token"));
    }

    @Transactional
    public void logoutUser(UUID userId){
        log.info("Getting refresh tokens for user with userid {}", userId);
        List<RefreshToken> tokens = tokenRepo.findByUserId(userId);
        tokens.forEach((token) -> token.setRevoked(true));
        tokenRepo.saveAll(tokens);
    }

    @Transactional
    public void deleteExpiredTokens(){
        tokenRepo.deleteExpiredTokens(LocalDateTime.now());
    }

    @Transactional
    public RefreshToken rotateToken(UUID oldToken) {
        RefreshToken existing = validateToken(oldToken);
        existing.setRevoked(true);
        tokenRepo.save(existing);
        return createRefreshToken(existing.getUser());
    }
}
