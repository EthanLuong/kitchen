package com.example.kitchen;

import com.example.kitchen.data.RefreshToken;
import com.example.kitchen.data.User;
import com.example.kitchen.repository.RefreshTokenRepository;
import com.example.kitchen.service.RefreshTokenService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    RefreshTokenRepository tokenRepo;

    @InjectMocks
    RefreshTokenService service;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserid(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        testUser.setUsername("testuser");
    }

    @Test
    void createRefreshToken_savesTokenWithCorrectFields() {
        when(tokenRepo.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, RefreshToken.class));
        RefreshToken token = service.createRefreshToken(testUser);
        assertEquals(testUser, token.getUser());
        assertTrue(token.getExpiryTime().isAfter(LocalDateTime.now().plusHours(7)));
    }

    @Test
    void validateToken_validToken_returnsToken() {
        when(tokenRepo.findByToken(any())).thenReturn(Optional.of(new RefreshToken()));
        assertDoesNotThrow(()-> service.validateToken(UUID.randomUUID()));
    }

    @Test
    void validateToken_invalidToken_throwsUnauthorized() {
        when(tokenRepo.findByToken(any())).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, ()->service.validateToken(UUID.randomUUID()));
    }

    @Test
    void logoutUser_revokesAllUserTokens() {
        RefreshToken token1 = new RefreshToken();
        token1.setRevoked(false);
        RefreshToken token2 = new RefreshToken();
        token2.setRevoked(false);

        when(tokenRepo.findByUserId(testUser.getUserid())).thenReturn(List.of(token1, token2));

        service.logoutUser(testUser.getUserid());

        ArgumentCaptor<List<RefreshToken>> captor = ArgumentCaptor.forClass(List.class);
        verify(tokenRepo).saveAll(captor.capture());

        assertThat(captor.getValue()).allMatch(RefreshToken::isRevoked);

    }

    @Test
    void logoutUser_noTokens_doesNothing() {
        when(tokenRepo.findByUserId(testUser.getUserid())).thenReturn(List.of());

        service.logoutUser(testUser.getUserid());

        verify(tokenRepo).saveAll(List.of());
    }

    @Test
    void deleteExpiredTokens_callsRepositoryWithCurrentTime() {
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);

        service.deleteExpiredTokens();

        verify(tokenRepo).deleteExpiredTokens(captor.capture());
        assertThat(captor.getValue()).isCloseTo(LocalDateTime.now(), Assertions.within(5, ChronoUnit.SECONDS));
    }

    @Test
    void rotateToken_revokesOldToken_createsNewToken() {
        RefreshToken existing = new RefreshToken();
        existing.setToken(UUID.randomUUID());
        existing.setUser(testUser);
        existing.setRevoked(false);

        when(tokenRepo.findByToken(existing.getToken())).thenReturn(Optional.of(existing));
        when(tokenRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.rotateToken(existing.getToken());

        assertThat(existing.isRevoked()).isTrue();
        verify(tokenRepo, times(2)).save(any());  // once to revoke, once to create new
    }

    @Test
    void rotateToken_invalidToken_throwsUnauthorized() {
        UUID invalidToken = UUID.randomUUID();
        when(tokenRepo.findByToken(invalidToken)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rotateToken(invalidToken))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }
}