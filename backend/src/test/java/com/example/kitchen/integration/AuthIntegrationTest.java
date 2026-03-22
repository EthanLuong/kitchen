package com.example.kitchen.integration;

import com.example.kitchen.data.User;
import com.example.kitchen.dto.AuthRequest;
import com.example.kitchen.dto.AuthResponse;
import com.example.kitchen.repository.FoodItemRepository;
import com.example.kitchen.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@Testcontainers
@SpringBootTest
@AutoConfigureRestTestClient
public class AuthIntegrationTest {


    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private FoodItemRepository foodRepo;

    @Autowired
    private RestTestClient restClient;

    @Autowired
    private BCryptPasswordEncoder encoder;


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }




    @BeforeEach
    void setUp(){
        foodRepo.deleteAll();
        userRepo.deleteAll();
        User newUser = new User(null, "ethan", encoder.encode("strongpassword"), true, null);
        userRepo.save(newUser);

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

}
