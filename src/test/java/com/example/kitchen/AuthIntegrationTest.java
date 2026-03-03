package com.example.kitchen;

import com.example.kitchen.configuration.CustomUserDetailsService;
import com.example.kitchen.configuration.JwtFilter;
import com.example.kitchen.controller.LoginController;
import com.example.kitchen.dto.AuthRequest;
import com.example.kitchen.dto.AuthResponse;
import com.example.kitchen.exception.UserAlreadyExistsException;
import com.example.kitchen.service.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class AuthIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private RestTestClient client;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    CustomUserDetailsService userDetailsService;

    @BeforeEach
    public void setup(WebApplicationContext context){
        client = RestTestClient.bindToApplicationContext(context).build();
    }

    @Test
    public void validSignup(){
        client.post()
                .uri("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AuthRequest("ethan", "password"))
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    public void invalidInputSignup(){
        client.post()
                .uri("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AuthRequest("", "password"))
                .exchange()
                .expectStatus().isBadRequest();

    }

    @Test
    public void duplicateUser(){
        Mockito.doThrow(UserAlreadyExistsException.class).when(loginService).signup(any());
        client.post()
                .uri("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AuthRequest("test", "password"))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    public void validLogin(){
        when(loginService.login(any())).thenReturn(new AuthResponse("token", "Bearer", 60));

        client.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AuthRequest("test", "password"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("token");
    }

    @Test
    public void badCredentials(){
        client.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AuthRequest("zdffg", "zdfsdf"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

}
