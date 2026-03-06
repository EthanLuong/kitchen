package com.example.kitchen;

import com.example.kitchen.data.User;
import com.example.kitchen.dto.AuthRequest;
import com.example.kitchen.dto.AuthResponse;
import com.example.kitchen.exception.UserAlreadyExistsException;
import com.example.kitchen.repository.UserRepository;
import com.example.kitchen.service.JwtService;
import com.example.kitchen.service.LoginService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoginServiceTest {
    @Mock
    private  AuthenticationManager auth;
    @Mock
    private  UserRepository repo;
    @Mock
    private  PasswordEncoder encoder;
    @Mock
    private  JwtService jwtService;

    @InjectMocks
    private LoginService service;

    @Test
    public void login_validCredentials_returnsToken(){
        Authentication authentication = new UsernamePasswordAuthenticationToken("ethan", "password");
        when(auth.authenticate(authentication)).thenReturn(new UsernamePasswordAuthenticationToken("ethan", null, Collections.emptyList()));
        when(jwtService.createJWT(any())).thenReturn("token");

        AuthRequest request = new AuthRequest("ethan", "password");
        AuthResponse response = service.login(request);

        assertEquals( "token", response.accessToken());
    }

    @Test
    public void login_badCredentials_throwsException(){
        when(auth.authenticate(any())).thenThrow(BadCredentialsException.class);
        AuthRequest auth = new AuthRequest("ethan", "password");
        assertThrows(BadCredentialsException.class, ()-> service.login(auth));
    }


    @Test
    public void signup_success(){
        when(repo.findByUsername(any())).thenReturn(null);
        when(encoder.encode(any())).thenReturn("encoded");
        AuthRequest request = new AuthRequest("ethan", "password");
        service.signup(request);
        verify(repo).save(argThat(user->user.getUsername().equals("ethan") && user.getPassword().equals("encoded")));


    }

    @Test
    public void signup_userAlreadyExists_throwsException(){

        when(repo.findByUsername(any())).thenReturn(new User());
        AuthRequest auth = new AuthRequest("username", "password");
        assertThrows(UserAlreadyExistsException.class, ()->service.signup(auth));

    }

}
