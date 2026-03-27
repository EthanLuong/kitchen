package com.example.kitchen.controller;

import com.example.kitchen.dto.AuthRequest;
import com.example.kitchen.dto.AuthResponse;
import com.example.kitchen.dto.LoginResult;
import com.example.kitchen.dto.RefreshRequest;
import com.example.kitchen.service.LoginService;
import com.example.kitchen.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/v1/auth")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService service) {
        this.loginService = service;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        LoginResult result = loginService.login(request);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/v1/auth/refresh")
                .sameSite("None")
                .maxAge(Duration.ofHours(8))
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(result.authResponse());
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@Valid @RequestBody AuthRequest request) {
        loginService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue("refreshToken") String refreshToken){
        return ResponseEntity.ok(loginService.refreshAccess(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Principal principal){
        loginService.logout(userid(principal));
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/v1/auth/refresh")
                .sameSite("None")
                .maxAge(0)
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    private static UUID userid(Principal principal){
        return UUID.fromString(principal.getName());
    }

}
