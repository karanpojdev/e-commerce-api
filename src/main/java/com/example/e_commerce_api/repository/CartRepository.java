package com.example.e_commerce_api.repository;

import com.example.e_commerce_api.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    // Custom method to easily fetch the Cart using the User's ID
    Optional<Cart> findByUserId(Long userId);
}