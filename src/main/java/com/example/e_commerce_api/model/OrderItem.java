package com.example.e_commerce_api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore; // Import this!
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to the Order it belongs to
    @JsonIgnore // Break circular reference (Order -> OrderItem -> Order)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Link to the Product (The item sold)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // The quantity ordered
    @Column(nullable = false)
    private int quantity;

    // CRITICAL: The unit price AT THE TIME OF ORDER
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;
}