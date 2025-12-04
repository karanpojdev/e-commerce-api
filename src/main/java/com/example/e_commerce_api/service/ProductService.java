package com.example.e_commerce_api.service;

import com.example.e_commerce_api.model.Product;
import com.example.e_commerce_api.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // ADMIN Functionality: Add a new product
    public Product createProduct(Product product) {
        // Future logic: Validate product price, name uniqueness, etc.
        return productRepository.save(product);
    }

    // USER Functionality: View all available products
    public List<Product> getAllAvailableProducts() {
        // For now, return all. Later, add filtering for available=true
        return productRepository.findAll();
    }

    // USER Functionality: View a single product by ID
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // ADMIN Functionality: Update an existing product
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found for this id :: " + id));

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStockQuantity(productDetails.getStockQuantity());
        product.setAvailable(productDetails.isAvailable());

        return productRepository.save(product);
    }

    // ADMIN Functionality: Delete a product
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}