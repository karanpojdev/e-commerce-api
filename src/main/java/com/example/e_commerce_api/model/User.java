package com.example.e_commerce_api.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // Stored as a HASH

    // Role will be used for authorization (e.g., "USER", "ADMIN")
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER; 
}