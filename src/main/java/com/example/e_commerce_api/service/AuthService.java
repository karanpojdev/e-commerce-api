package com.example.e_commerce_api.service;

import com.example.e_commerce_api.model.User;
import com.example.e_commerce_api.repository.UserRepository;
import com.example.e_commerce_api.model.Role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. Sign Up Logic
    public User registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already taken!");
        }

        // ⚠️ Store the HASHED password, never the plain text
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Ensure the default role is USER (unless an admin endpoint is used)
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }

        return userRepository.save(user);
    }
    
    // Note: The actual login (authentication) logic is handled by Spring Security, 
    // we only need the controller to trigger it and get the JWT from the JwtService.
}