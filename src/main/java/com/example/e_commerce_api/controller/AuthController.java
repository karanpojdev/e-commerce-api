package com.example.e_commerce_api.controller;

import com.example.e_commerce_api.dto.AuthResponse;
import com.example.e_commerce_api.dto.LoginRequest;
import com.example.e_commerce_api.model.User;
import com.example.e_commerce_api.service.AuthService;
import com.example.e_commerce_api.service.JwtService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtService jwtService;

    // 1. Sign Up Endpoint
    // POST http://localhost:8080/api/auth/signup
    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        try {
            authService.registerUser(user);
            return new ResponseEntity<>("User registered successfully!", HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 2. Log In Endpoint
    // POST http://localhost:8080/api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {
        
        // 1. Authenticate the user credentials using the AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        
        // 2. Set the Authentication object in the Security Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Generate the JWT
        String token = jwtService.generateToken(authentication);

        // 4. Return the token to the client
        return new ResponseEntity<>(new AuthResponse(token, "Bearer"), HttpStatus.OK);
    }
}