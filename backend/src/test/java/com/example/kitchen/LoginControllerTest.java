package com.example.kitchen;

import com.example.kitchen.configuration.JwtFilter;
import com.example.kitchen.configuration.RateLimiterFilter;
import com.example.kitchen.configuration.SecurityConfig;
import com.example.kitchen.controller.LoginController;
import com.example.kitchen.dto.AuthRequest;
import com.example.kitchen.dto.AuthResponse;
import com.example.kitchen.exception.UserAlreadyExistsException;
import com.example.kitchen.service.JwtService;
import com.example.kitchen.service.LoginService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@WebMvcTest(LoginController.class)
@Import({SecurityConfig.class, JwtFilter.class, RateLimiterFilter.class})
@AutoConfigureRestTestClient
@TestPropertySource(properties = "allowed.origin=http://localhost")
public class LoginControllerTest {

    @Autowired
    private RestTestClient client;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    public void signup_happy_returnsCreated() {
        doNothing().when(loginService).signup(any());
        client.post()
                .uri("/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AuthRequest("ethan", "password1234"))
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    public void signup_badInput_returnsBadRequest() {
        client.post()
                .uri("/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AuthRequest("", "password"))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void signup_duplicateUser_returns409() {
        Mockito.doThrow(UserAlreadyExistsException.class).when(loginService).signup(any());
        client.post()
                .uri("/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AuthRequest("test1", "password1234"))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    public void login_happy_returnsToken() {
        when(loginService.login(any())).thenReturn(new AuthResponse("token", "Bearer", 60, "refresh"));

        client.post()
                .uri("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AuthRequest("test1", "strongpassword"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("token");
    }

    @Test
    public void login_badCredentials_returnsUnauthorized() {
        when(loginService.login(any())).thenThrow(BadCredentialsException.class);

        client.post()
                .uri("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AuthRequest("zdffgg", "strongpassword"))
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
