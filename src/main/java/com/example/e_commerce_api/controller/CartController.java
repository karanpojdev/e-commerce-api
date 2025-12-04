package com.example.e_commerce_api.controller;

import com.example.e_commerce_api.model.Cart;
import com.example.e_commerce_api.security.CustomUserDetails; // Import the custom UserDetails
import com.example.e_commerce_api.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Core Annotation
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // Helper method to securely extract the ID from the authenticated user.
    // This is safe because only a valid token can populate CustomUserDetails.
    private Long getUserId(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
             // This case should be caught by Spring Security's filter, but good for robustness
             throw new RuntimeException("Authentication context missing user details.");
        }
        return userDetails.getId();
    }

    // 1. Get the current user's cart
    @GetMapping 
    public ResponseEntity<Cart> getCart(@AuthenticationPrincipal CustomUserDetails userDetails) { 
        Long userId = getUserId(userDetails);
        Cart cart = cartService.getCart(userId);
        return ResponseEntity.ok(cart);
    }

    // 2. Add or Update a product quantity
    @PostMapping("/add")
    public ResponseEntity<Cart> addItemToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, Object> request) {
        
        // ID is retrieved securely from the token
        Long userId = getUserId(userDetails); 

        // Safely extract and cast parameters
        Long productId = ((Number) request.get("productId")).longValue();
        int quantity = ((Number) request.getOrDefault("quantity", 1)).intValue(); 
        
        Cart updatedCart = cartService.addProductToCart(userId, productId, quantity);
        return ResponseEntity.ok(updatedCart);
    }

    // 3. Update or Remove a product 
    @PutMapping("/update")
    public ResponseEntity<Cart> updateItemQuantity(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, Object> request) {
        
        Long userId = getUserId(userDetails);

        Long productId = ((Number) request.get("productId")).longValue();
        int quantity = ((Number) request.getOrDefault("quantity", 0)).intValue(); 
        
        Cart updatedCart = cartService.updateProductQuantity(userId, productId, quantity);
        return ResponseEntity.ok(updatedCart);
    }

    // 4. Clear the entire cart
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = getUserId(userDetails);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}