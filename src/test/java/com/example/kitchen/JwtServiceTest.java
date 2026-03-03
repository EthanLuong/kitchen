package com.example.kitchen;

import com.example.kitchen.service.JwtService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    private JwtService jwtService;

    @Mock
    HttpServletRequest request;


    @BeforeEach
    public void setup(){
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "MMHLMQG7ERhS25dJE089lOAbujWeSvHPcjPw1yX9SBl");
    }

    @Test
    public void createAndValidateJwt(){
        String token = jwtService.createJWT("ethan");
        String s = Jwts.parser().verifyWith(jwtService.getKey()).build().parseSignedClaims(token).getPayload().getSubject();
        assertEquals("ethan", s );
    }

    @Test
    public void createTokenAndRetrieveFromRequest(){
        String token = jwtService.createJWT("ethan");
        assertNotNull(token);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        assertEquals("ethan", jwtService.getSubject(request));
    }

    @Test
    public void getSubjectNoHeader(){
        assertNull(jwtService.getSubject(request));
    }

    @Test
    public void getSubjectInvalidToken(){
        when(request.getHeader("Authorization")).thenReturn("Bearer garbage");
        assertNull(jwtService.getSubject(request));
    }

    @Test
    public void retrieveJWTValid(){
        when(request.getHeader("Authorization")).thenReturn("Bearer mytoken");
        assertEquals("mytoken", jwtService.retrieveJWT(request));
    }

    @Test
    public void retrieveJWTNoHeader(){
        when(request.getHeader("Authorization")).thenReturn(null);
        assertThrows(JwtException.class, () -> jwtService.retrieveJWT(request));
    }


}
