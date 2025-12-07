package com.example.e_commerce_api.repository;

import com.example.e_commerce_api.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // Custom method to fetch all orders for a specific user, sorted newest first
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);
}