package com.example.e_commerce_api.repository;

import com.example.e_commerce_api.model.CartItem;
import com.example.e_commerce_api.model.Cart;
import com.example.e_commerce_api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // Custom method to quickly find if a specific product is already in a specific cart
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}