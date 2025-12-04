package com.example.e_commerce_api.repository;

import com.example.e_commerce_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Custom method to find a User by email (needed for login/security)
    Optional<User> findByEmail(String email);
}