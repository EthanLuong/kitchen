package com.example.kitchen.integration;


import com.example.kitchen.data.FoodItem;
import com.example.kitchen.data.User;
import com.example.kitchen.data.UserLocations;
import com.example.kitchen.data.UserTypes;
import com.example.kitchen.dto.FoodItemRequest;
import com.example.kitchen.repository.FoodItemRepository;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@Transactional
public class FoodIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");


    @Autowired
    private FoodItemRepository foodRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserLocationRepository locationRepo;

    @Autowired
    private UserTypeRepository typeRepo;

    private RestTestClient restClient;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private BCryptPasswordEncoder encoder;

    static String token;

    private Long foodId;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeAll
    static void initializeData(){
    }


    @BeforeEach
    void setUp(){
        restClient = RestTestClient.bindTo(mockMvc).build();
        foodRepo.deleteAll();
        userRepo.deleteAll();
        locationRepo.deleteAll();
        typeRepo.deleteAll();
        User newUser = new User(null, "ethan", encoder.encode("strongpassword"), true, null);
        User savedUser = userRepo.save(newUser);

        UserTypes type = new UserTypes();
        UserLocations location = new UserLocations();
        type.setName("PRODUCE");
        location.setName("COUNTER");
        type.setUser(savedUser);
        location.setUser(savedUser);

        typeRepo.save(type);
        locationRepo.save(location);




        FoodItem item = new FoodItem();
        item.setUser(savedUser);
        item.setName("banana");
        item.setFoodType("PRODUCE");
        item.setUnit(FoodItem.Unit.COUNT);
        item.setLocation("COUNTER");
        foodId = foodRepo.save(item).getId();
        token = jwtService.createJWT(savedUser.getUserid().toString());



    }

    @Test
    void getItems_authenticatedUser_returnsPage() {
        restClient.get().uri("/v1/items").header("Authorization", "Bearer " + token).exchange().expectBody().jsonPath("$.content[0].name").isEqualTo("banana");
    }

    @Test
    void getItems_unauthenticated_returns401() {
        restClient.get().uri("/v1/items").exchange().expectStatus().isUnauthorized();

    }

    @Test
    void createItem_validRequest_returns201() {
        FoodItemRequest request = new FoodItemRequest("Orange", "PRODUCE", null, null, "COUNTER", null, null, null, null);
        restClient.post().uri("/v1/items").header("Authorization", "Bearer " + token).body(request).exchange().expectStatus().isCreated();

    }

    @Test
    void createItem_unauthenticated_returns401() {
        FoodItemRequest request = new FoodItemRequest("Orange", "PRODUCE", null, null, "COUNTER", null, null, null, null);
        restClient.post().uri("/v1/items").body(request).exchange().expectStatus().isUnauthorized();

    }

    @Test
    void createItem_invalidRequest_returns400() {
        FoodItemRequest request = new FoodItemRequest("", "PRODUCE", null, null, "COUNTER", null, null, null, null);
        restClient.post().uri("/v1/items").header("Authorization", "Bearer " + token).body(request).exchange().expectStatus().isBadRequest();

    }

    @Test
    void updateItem_validRequest_returns200() {
        FoodItemRequest request = new FoodItemRequest("Orange", "PRODUCE", null, null, "COUNTER", null, null, null, null);
        restClient.put().uri("/v1/items/" + foodId).header("Authorization", "Bearer " + token).body(request).exchange().expectStatus().isOk();

    }

    @Test
    void updateItem_wrongUser_returns404() {
        User newUser = new User(null, "george", encoder.encode("strongpassword"), true, null);
        User savedUser = userRepo.save(newUser);
        String newToken = jwtService.createJWT(savedUser.getUserid().toString());
        FoodItemRequest request = new FoodItemRequest("Orange", "PRODUCE", null, null, "COUNTER", null, null, null, null);
        restClient.put().uri("/v1/items/1").header("Authorization", "Bearer " + newToken).body(request).exchange().expectStatus().isNotFound();

    }

    @Test
    void updateItem_unauthenticated_returns401() {
        FoodItemRequest request = new FoodItemRequest("Orange", "PRODUCE", null, null, "COUNTER", null, null, null, null);
        restClient.put().uri("/v1/items/1").body(request).exchange().expectStatus().isUnauthorized();

    }

    @Test
    void deleteItem_validRequest_returns204() {
        restClient.delete().uri("/v1/items/" + foodId).header("Authorization", "Bearer " + token).exchange().expectStatus().isNoContent();
    }

    @Test
    void deleteItem_wrongUser_returns404() {
        User newUser = new User(null, "george", encoder.encode("strongpassword"), true, null);
        User savedUser = userRepo.save(newUser);
        String newToken = jwtService.createJWT(savedUser.getUserid().toString());
        restClient.delete().uri("/v1/items/1").header("Authorization", "Bearer " + newToken).exchange().expectStatus().isNotFound();

    }

    @Test
    void deleteItem_unauthenticated_returns401() {
        restClient.delete().uri("/v1/items/1").exchange().expectStatus().isUnauthorized();

    }

    //TODO: Add test for new food type/location implementation



}
