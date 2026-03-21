package com.example.kitchen;

import com.example.kitchen.dto.AuthRequest;
import com.example.kitchen.dto.AuthResponse;
import com.example.kitchen.exception.UserAlreadyExistsException;
import com.example.kitchen.service.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class LoginControllerTest {

    @Autowired
    private WebApplicationContext context;

    private RestTestClient client;

    @MockitoBean
    private LoginService loginService;

//    @MockitoBean
//    private JwtFilter jwtFilter;
//
//    @MockitoBean
//    CustomUserDetailsService userDetailsService;

    @BeforeEach
    public void setup(WebApplicationContext context){
        client = RestTestClient.bindToApplicationContext(context).build();
    }

    @Test
    public void signup_happy_returnsCreated(){
        client.post()
                .uri("/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AuthRequest("ethan", "password"))
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    public void signup_badInput_returnsBadRequest(){
        client.post()
                .uri("/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AuthRequest("", "password"))
                .exchange()
                .expectStatus().isBadRequest();

    }

    @Test
    public void signup_duplicateUser_returns409(){
        Mockito.doThrow(UserAlreadyExistsException.class).when(loginService).signup(any());
        client.post()
                .uri("/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AuthRequest("test", "password"))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    public void login_happy_returnsToken(){
        when(loginService.login(any())).thenReturn(new AuthResponse("token", "Bearer", 60));

        client.post()
                .uri("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AuthRequest("test", "password"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("token");
    }

    @Test
    public void login_badCredentials_returnsUnauthorized(){
        when(loginService.login(any())).thenThrow(BadCredentialsException.class);

        client.post()
                .uri("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AuthRequest("zdffg", "strongpassword"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

}
