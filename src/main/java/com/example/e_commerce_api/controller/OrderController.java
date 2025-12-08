package com.example.e_commerce_api.controller;

import com.example.e_commerce_api.dto.CheckoutResponse;
import com.stripe.model.PaymentIntent;
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
    public ResponseEntity<CheckoutResponse> placeOrder(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = getUserId(userDetails);
        
        try {
            PaymentIntent intent = orderService.placeOrder(userId);
            CheckoutResponse response = new CheckoutResponse(
                Long.parseLong(intent.getMetadata().get("order_id")), 
                intent.getClientSecret()
            );

            return ResponseEntity.ok(response);
        }catch (Exception e) {
            // Handle exceptions (e.g., cart empty, stock shortage, Stripe API errors)
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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