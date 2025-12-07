package com.example.e_commerce_api.controller;

import com.example.e_commerce_api.model.Order;
import com.example.e_commerce_api.security.CustomUserDetails;
import com.example.e_commerce_api.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    private Long getUserId(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
             throw new RuntimeException("Authentication context missing user details.");
        }
        return userDetails.getId();
    }

    /**
     * POST /api/orders/place: Converts the cart into a permanent order.
     */
    @PostMapping("/place")
    public ResponseEntity<Order> placeOrder(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = getUserId(userDetails);
        
        try {
            Order newOrder = orderService.placeOrder(userId);
            return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Catches errors like "Cart is empty" or "Insufficient stock"
            // You can enhance this to return specific error messages via a DTO
            System.err.println("Order placement failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(null); 
        }
    }

    /**
     * GET /api/orders: View the order history for the current user.
     */
    @GetMapping
    public ResponseEntity<List<Order>> getUserOrders(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = getUserId(userDetails);
        List<Order> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }
}