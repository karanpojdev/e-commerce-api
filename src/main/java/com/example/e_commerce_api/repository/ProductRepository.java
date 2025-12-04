package com.example.e_commerce_api.repository;

import com.example.e_commerce_api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // We might add custom search/filter methods later, but JpaRepository gives us findById, findAll, save, etc.
}