package com.example.kitchen.controller;

import com.example.kitchen.dto.AuthRequest;
import com.example.kitchen.dto.AuthResponse;
import com.example.kitchen.dto.LoginResult;
import com.example.kitchen.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Duration;

@Tag(name = "Authentication", description = "Register, login, token refresh, and logout")
@Slf4j
@RestController
@RequestMapping("/v1/auth")
public class LoginController {

    private final LoginService loginService;

    @Value("${cookie.secure}")
    private boolean cookieSecure;
    @Value("${cookie.same-site}")
    private String cookieSameSite;

    public LoginController(LoginService service) {
        this.loginService = service;
    }

    @Operation(summary = "Login", description = "Authenticates the user and returns a JWT access token. Sets an HttpOnly refresh token cookie.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful — access token in body, refresh token in cookie"),
        @ApiResponse(responseCode = "401", description = "Invalid username or password"),
        @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        LoginResult result = loginService.login(request);
        log.info("Cookie secure: {}, CookieSameSite: {}", cookieSecure, cookieSameSite);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/v1/auth/refresh")
                .sameSite(cookieSameSite)
                .maxAge(Duration.ofHours(8))
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(result.authResponse());
    }

    @Operation(summary = "Register a new user")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed"),
        @ApiResponse(responseCode = "409", description = "Username already taken")
    })
    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@Valid @RequestBody AuthRequest request) {
        loginService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Refresh access token", description = "Uses the HttpOnly refresh token cookie to issue a new JWT access token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "New access token issued"),
        @ApiResponse(responseCode = "401", description = "Refresh token missing, expired, or revoked")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue("refreshToken") String refreshToken) {
        return ResponseEntity.ok(loginService.refreshAccess(refreshToken));
    }

    @Operation(summary = "Logout", description = "Revokes all refresh tokens for the user and clears the refresh token cookie")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Logged out successfully"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Principal principal) {
        loginService.logout(ControllerUtils.userId(principal));
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/v1/auth/refresh")
                .sameSite(cookieSameSite)
                .maxAge(0)
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }
}
