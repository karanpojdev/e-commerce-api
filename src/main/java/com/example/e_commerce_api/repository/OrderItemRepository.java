package com.example.e_commerce_api.repository;

import com.example.e_commerce_api.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // Basic CRUD is enough
}