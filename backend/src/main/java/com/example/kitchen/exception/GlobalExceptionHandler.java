package com.example.kitchen.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;


import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public  ProblemDetail handleUserExists(UserAlreadyExistsException ex){
        ProblemDetail response = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "User already exists");
        response.setTitle("Invalid registration");
        return response;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex){
        ProblemDetail response = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Credentials provided were incorrect");
        response.setTitle("Bad Credentials");
        return response;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleInvalidRequest(MethodArgumentNotValidException ex){
        String details = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ProblemDetail response = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, details);

        response.setTitle("Invalid arguments");

        return response;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatus(ResponseStatusException ex){
        ProblemDetail response = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), "Something went wrong");
        response.setTitle("Internal Server Error");

        return response;

    }

}
