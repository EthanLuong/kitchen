package com.example.kitchen.repository;

import com.example.kitchen.data.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Query("SELECT x FROM RefreshToken x WHERE x.token = :token AND x.revoked = false AND x.expiryTime > CURRENT_TIMESTAMP")
    Optional<RefreshToken> findByToken(UUID token);

    @Query("SELECT x FROM RefreshToken x WHERE x.user.userid = :userId AND x.revoked = false")
    List<RefreshToken> findByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM RefreshToken x WHERE x.expiryTime < :now")
    void deleteExpiredTokens(LocalDateTime now);

}
