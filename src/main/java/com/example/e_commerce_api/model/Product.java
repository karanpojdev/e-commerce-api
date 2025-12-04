package com.example.e_commerce_api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Lob // Used for large strings like descriptions
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Use BigDecimal for currency

    @Column(nullable = false)
    private Integer stockQuantity;
    
    // Field to track if a product is active/visible
    private boolean available = true;
}