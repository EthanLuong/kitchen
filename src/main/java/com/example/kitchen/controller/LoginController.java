package com.example.kitchen.controller;

import com.example.kitchen.data.User;
import com.example.kitchen.service.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class LoginController {

    private final LoginService service;

    public LoginController(LoginService service) {
        this.service = service;
    }

    // Note: changed from PUT to POST — more conventional for login/signup
    @PostMapping("/login")
    public ResponseEntity<LoginService.AuthResponse> login(@RequestBody User user) {
        return service.login(user);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody User user) {
        return service.signup(user);
    }
}
