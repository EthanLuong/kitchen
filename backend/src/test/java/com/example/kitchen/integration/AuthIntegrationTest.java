package com.example.kitchen.integration;

import com.example.kitchen.data.RefreshToken;
import com.example.kitchen.data.User;
import com.example.kitchen.dto.AuthRequest;
import com.example.kitchen.dto.AuthResponse;
import com.example.kitchen.dto.RefreshRequest;
import com.example.kitchen.repository.FoodItemRepository;
import com.example.kitchen.repository.RefreshTokenRepository;
import com.example.kitchen.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class AuthIntegrationTest {


    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private FoodItemRepository foodRepo;

    @Autowired
    private MockMvc mockMvc;

    private RestTestClient restClient;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private RefreshTokenRepository tokenRepo;


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    UUID tokenId = UUID.randomUUID();
    UUID expiredId = UUID.randomUUID();

    @BeforeEach
    void setUp(){
        restClient = RestTestClient.bindTo(mockMvc).build();
        foodRepo.deleteAll();
        userRepo.deleteAll();
        tokenRepo.deleteAll();

        User newUser = new User(null, "ethan", encoder.encode("strongpassword"), true, null);
        User tokenUser = userRepo.save(newUser);

        RefreshToken token = new RefreshToken();
        token.setToken(tokenId);
        token.setUser(tokenUser);
        token.setExpiryTime(LocalDateTime.now().plusDays(1));
        tokenRepo.save(token);

        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken(expiredId);
        expiredToken.setUser(tokenUser);
        expiredToken.setExpiryTime(LocalDateTime.now().minusDays(1));
        tokenRepo.save(expiredToken);




    }

    @Test
    void login_validCredentials_returnsToken(){
        AuthRequest request = new AuthRequest("ethan", "strongpassword");
        restClient.post().uri("/v1/auth/login").body(request).exchange().expectBody(AuthResponse.class).value(body -> {
            assert body != null;
            assertNotNull(body.accessToken());
        });
    }


    @Test
    void login_invalidPassword_returns401() {
        AuthRequest request = new AuthRequest("ethan", "wrongpassword");
        restClient.post().uri("/v1/auth/login").body(request).exchange().expectStatus().isUnauthorized();
    }

    @Test
    void login_nonExistentUser_returns401() {
        AuthRequest request = new AuthRequest("bob", "strongpassword");
        restClient.post().uri("/v1/auth/login").body(request).exchange().expectStatus().isUnauthorized();
    }

    @Test
    void signup_validRequest_returns201() {
        AuthRequest request = new AuthRequest("newuser", "areallygoodpassword");
        restClient.post().uri("/v1/auth/signup").body(request).exchange().expectStatus().isCreated();
    }

    @Test
    void signup_duplicateUsername_returns409() {
        AuthRequest request = new AuthRequest("ethan", "wrongpassword");
        restClient.post().uri("/v1/auth/signup").body(request).exchange().expectStatus().isEqualTo(409);
    }

    @Test
    void signup_blankUsername_returns400() {
        AuthRequest request = new AuthRequest("", "wrongpassword");
        restClient.post().uri("/v1/auth/signup").body(request).exchange().expectStatus().isBadRequest();
    }

    @Test
    void signup_shortPassword_returns400() {
        AuthRequest request = new AuthRequest("ethan", "ex");
        restClient.post().uri("/v1/auth/signup").body(request).exchange().expectStatus().isBadRequest();

    }
    @Test
    void refresh_validToken_returnsNewAccessToken() {
        RefreshRequest request = new RefreshRequest(tokenId);
        restClient.post().uri("/v1/auth/refresh").body(request).exchange().expectBody(AuthResponse.class);
    }

    @Test
    void refresh_invalidToken_returns401() {
        RefreshRequest request = new RefreshRequest(UUID.randomUUID());
        restClient.post().uri("/v1/auth/refresh").body(request).exchange().expectStatus().isUnauthorized();
    }

    @Test
    void refresh_expiredToken_returns401() {
        RefreshRequest request = new RefreshRequest(expiredId);
        restClient.post().uri("/v1/auth/refresh").body(request).exchange().expectStatus().isUnauthorized();
    }

    @Test
    void logout_authenticated_revokesTokens() {
        String accessToken = restClient.post().uri("/v1/auth/login").body(new AuthRequest("ethan", "strongpassword")).exchange().expectBody(AuthResponse.class).returnResult().getResponseBody().accessToken();
        restClient.post().uri("/v1/auth/logout").header("Authorization", "Bearer " + accessToken).exchange().expectStatus().isOk();
        RefreshToken token = tokenRepo.findAll().get(0);
        assertTrue(token.isRevoked());
    }

    @Test
    void logout_unauthenticated_returns401() {
        restClient.post().uri("/v1/auth/logout").exchange().expectStatus().isUnauthorized();

    }
}
