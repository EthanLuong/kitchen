package com.example.kitchen;

import com.example.kitchen.data.FoodItem;
import com.example.kitchen.dto.FoodItemRequest;
import com.example.kitchen.dto.FoodItemResponse;
import com.example.kitchen.service.FoodItemService;
import com.example.kitchen.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest
public class FoodItemControllerTest {
    @Autowired
    private WebApplicationContext context;

    private RestTestClient client;

    @MockitoBean
    private FoodItemService service;

    @MockitoBean
    private JwtService jwtService;

    private static final UUID USER_ID = UUID.randomUUID();

    private static final FoodItemResponse SAMPLE_RESPONSE = new FoodItemResponse(
            1L, "Milk", FoodItem.FoodType.DAIRY, 1.0, FoodItem.Unit.L,
            FoodItem.Location.FRIDGE, LocalDate.now().plusDays(7), null, null, null,
            false, null, null
    );

    @BeforeEach
    public void setup(WebApplicationContext context) {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        client = RestTestClient.bindTo(mockMvc).build();
        when(jwtService.getSubject(any())).thenReturn(USER_ID.toString());
    }


    @Test
    public void getAll_happy_returnsOk() {
        when(service.getAllItems(any())).thenReturn(List.of(SAMPLE_RESPONSE));
        client.get().uri("/items")
                .header("Authorization", "Bearer fake")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1);
    }


    @Test
    public void getOne_happy_returnsOk() {
        when(service.getItem(any(), eq(1L))).thenReturn(SAMPLE_RESPONSE);
        client.get().uri("/items/1")
                .header("Authorization", "Bearer fake")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Milk");
    }

    @Test
    public void getOne_notFound_returns404() {
        when(service.getItem(any(), eq(99L)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        client.get().uri("/items/99")
                .header("Authorization", "Bearer fake")
                .exchange()
                .expectStatus().isNotFound();
    }


    @Test
    public void getExpiring_happy_returnsOk() {
        when(service.getExpiringSoon(any(), eq(7))).thenReturn(List.of(SAMPLE_RESPONSE));
        client.get().uri("/items/expiring?days=7")
                .header("Authorization", "Bearer fake")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1);
    }


    @Test
    public void getByLocation_happy_returnsOk() {
        when(service.getByLocation(any(), eq(FoodItem.Location.FRIDGE))).thenReturn(List.of(SAMPLE_RESPONSE));
        client.get().uri("/items/location/FRIDGE")
                .header("Authorization", "Bearer fake")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].location").isEqualTo("FRIDGE");
    }

    @Test
    public void getByLocation_invalidEnum_returns400() {
        client.get().uri("/items/location/GARAGE")
                .header("Authorization", "Bearer fake")
                .exchange()
                .expectStatus().isBadRequest();
    }

    // ── GET /items/type/{type} ──────────────────────────────

    @Test
    public void getByType_happy_returnsOk() {
        when(service.getByFoodType(any(), eq(FoodItem.FoodType.DAIRY))).thenReturn(List.of(SAMPLE_RESPONSE));
        client.get().uri("/items/type/DAIRY")
                .header("Authorization", "Bearer fake")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].foodType").isEqualTo("DAIRY");
    }

    @Test
    public void getByType_invalidEnum_returns400() {
        client.get().uri("/items/type/CANDY")
                .header("Authorization", "Bearer fake")
                .exchange()
                .expectStatus().isBadRequest();
    }


    @Test
    public void create_happy_returns201() {
        when(service.addItem(any(), any())).thenReturn(SAMPLE_RESPONSE);
        client.post().uri("/items")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new FoodItemRequest("Milk", FoodItem.FoodType.DAIRY, 1.0,
                        FoodItem.Unit.L, FoodItem.Location.FRIDGE, null, null, null, null))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Milk");
    }

    @Test
    public void create_missingName_returns400() {
        client.post().uri("/items")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new FoodItemRequest("", FoodItem.FoodType.DAIRY, 1.0,
                        FoodItem.Unit.L, FoodItem.Location.FRIDGE, null, null, null, null))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void create_missingFoodType_returns400() {
        client.post().uri("/items")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new FoodItemRequest("Milk", null, 1.0,
                        FoodItem.Unit.L, FoodItem.Location.FRIDGE, null, null, null, null))
                .exchange()
                .expectStatus().isBadRequest();
    }


    @Test
    public void update_happy_returnsOk() {
        FoodItemResponse updated = new FoodItemResponse(
                1L, "Oat Milk", FoodItem.FoodType.DAIRY, 1.0, FoodItem.Unit.L,
                FoodItem.Location.FRIDGE, null, null, null, null, false, null, null);
        when(service.updateItem(any(), eq(1L), any())).thenReturn(updated);
        client.put().uri("/items/1")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new FoodItemRequest("Oat Milk", FoodItem.FoodType.DAIRY, 1.0,
                        FoodItem.Unit.L, FoodItem.Location.FRIDGE, null, null, null, null))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Oat Milk");
    }

    @Test
    public void update_notFound_returns404() {
        when(service.updateItem(any(), eq(99L), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        client.put().uri("/items/99")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new FoodItemRequest("Milk", FoodItem.FoodType.DAIRY, 1.0,
                        FoodItem.Unit.L, FoodItem.Location.FRIDGE, null, null, null, null))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void update_invalidBody_returns400() {
        client.put().uri("/items/1")
                .header("Authorization", "Bearer fake")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new FoodItemRequest("", null, null, null, null, null, null, null, null))
                .exchange()
                .expectStatus().isBadRequest();
    }


    @Test
    public void consume_happy_returnsOk() {
        FoodItemResponse consumed = new FoodItemResponse(
                1L, "Milk", FoodItem.FoodType.DAIRY, 1.0, FoodItem.Unit.L,
                FoodItem.Location.FRIDGE, null, null, null, null, true, null, null);
        when(service.markConsumed(any(), eq(1L))).thenReturn(consumed);
        client.patch().uri("/items/1/consume")
                .header("Authorization", "Bearer fake")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.consumed").isEqualTo(true);
    }

    @Test
    public void consume_notFound_returns404() {
        when(service.markConsumed(any(), eq(99L)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        client.patch().uri("/items/99/consume")
                .header("Authorization", "Bearer fake")
                .exchange()
                .expectStatus().isNotFound();
    }


    @Test
    public void delete_happy_returns204() {
        client.delete().uri("/items/1")
                .header("Authorization", "Bearer fake")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void delete_notFound_returns404() {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"))
                .when(service).deleteItem(any(), eq(99L));
        client.delete().uri("/items/99")
                .header("Authorization", "Bearer fake")
                .exchange()
                .expectStatus().isNotFound();
    }


    @Test
    public void noAuth_returns401() {
        when(jwtService.getSubject(any())).thenReturn(null);
        client.get().uri("/items")
                .exchange()
                .expectStatus().isForbidden();
    }
}
