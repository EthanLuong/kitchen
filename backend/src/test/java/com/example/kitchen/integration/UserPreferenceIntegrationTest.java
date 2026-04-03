package com.example.kitchen.integration;

import com.example.kitchen.dto.*;
import com.example.kitchen.repository.UserLocationRepository;
import com.example.kitchen.repository.UserRepository;
import com.example.kitchen.repository.UserTypeRepository;
import com.example.kitchen.service.JwtService;
import com.example.kitchen.service.UserPreferencesService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@Transactional
@Sql(scripts = "/test-data/preferences-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserPreferenceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserLocationRepository userLocationRepo;

    @Autowired
    private UserTypeRepository userTypeRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserPreferencesService userPreferencesService;

    @Autowired
    private JwtService jwtService;

    private RestTestClient restTestClient;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    String userToken;
    String otherToken;



    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }



    @BeforeEach
    void setUp(){
        userToken = jwtService.createJWT("00000000-0000-0000-0000-000000000001");
        otherToken = jwtService.createJWT("00000000-0000-0000-0000-000000000002");
        restTestClient = RestTestClient.bindTo(mockMvc).build();
    }

    @Test
    void  getTypesLocations_returnsDefaultTypesLocations_afterSignup() {
        restTestClient.post().uri("/v1/auth/signup").body(new AuthRequest("newuser", "strongpassword")).exchange().expectStatus().isCreated();
        String token = restTestClient.post().uri("/v1/auth/login").body(new AuthRequest("newuser", "strongpassword")).exchange().expectBody(AuthResponse.class).returnResult().getResponseBody().accessToken();
        restTestClient.get().uri("/v1/user/types").header("Authorization", "Bearer " + token).exchange()
                .expectStatus().isOk().expectBody()
                .jsonPath("$[0].name").isEqualTo("DAIRY")
                .jsonPath("$[1].name").isEqualTo("MEAT")
                .jsonPath("$[2].name").isEqualTo("PRODUCE")
                .jsonPath("$[3].name").isEqualTo("GRAIN")
                .jsonPath("$[4].name").isEqualTo("FROZEN")
                .jsonPath("$[5].name").isEqualTo("BEVERAGE")
                .jsonPath("$[6].name").isEqualTo("SNACK")
                .jsonPath("$[7].name").isEqualTo("OTHER");

        restTestClient.get().uri("/v1/user/locations").header("Authorization", "Bearer " + token).exchange()
                .expectStatus().isOk().expectBody()
                .jsonPath("$[0].name").isEqualTo("FRIDGE")
                .jsonPath("$[1].name").isEqualTo("FREEZER")
                .jsonPath("$[2].name").isEqualTo("PANTRY")
                .jsonPath("$[3].name").isEqualTo("COUNTER");



    }
    @Test
    void getTypes_unauthenticated_returnsUnauthorized() {
        restTestClient.get().uri("/v1/user/types").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void getLocations_unauthenticated_returnsUnauthorized() {
        restTestClient.get().uri("/v1/user/locations").exchange().expectStatus().isUnauthorized();
    }


    @Test
    void addType_returnsCreatedType() {
        restTestClient.post().uri("/v1/user/types").body(new PreferenceRequest("VEGETABLE")).header("Authorization", "Bearer " + userToken).exchange()
                .expectStatus().isCreated().expectBody(UserTypeResponse.class)
                .consumeWith(result -> {assertEquals( "VEGETABLE",result.getResponseBody().name());});
    }


    @Test
    void addLocation_returnsCreatedLocation() {
        restTestClient.post().uri("/v1/user/locations").body(new PreferenceRequest("BASEMENT")).header("Authorization", "Bearer " + userToken).exchange()
                .expectStatus().isCreated().expectBody(UserLocationResponse.class)
                .consumeWith(result -> {assertEquals( "BASEMENT",result.getResponseBody().name());});
    }

    @Test
    void deleteType_returnsNoContent() {
        restTestClient.delete().uri("/v1/user/types/10").header("Authorization", "Bearer " + userToken).exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteLocation_returnsNoContent() {
        restTestClient.delete().uri("/v1/user/locations/10").header("Authorization", "Bearer " + userToken).exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteType_belongingToOtherUser_returnsForbidden() {
        restTestClient.delete().uri("/v1/user/types/11").header("Authorization", "Bearer " + userToken).exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void deleteLocation_belongingToOtherUser_returnsForbidden() {
        restTestClient.delete().uri("/v1/user/locations/11").header("Authorization", "Bearer " + userToken).exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void addType_duplicate_returnsError() {
        restTestClient.post().uri("/v1/user/types").body(new PreferenceRequest("DAIRY")).header("Authorization", "Bearer " + userToken).exchange()
                .expectStatus().isBadRequest();
    }
    @Test
    void addLocation_duplicate_returnsError() {
        restTestClient.post().uri("/v1/user/locations").body(new PreferenceRequest("FRIDGE")).header("Authorization", "Bearer " + userToken).exchange()
                .expectStatus().isBadRequest();
    }


}
