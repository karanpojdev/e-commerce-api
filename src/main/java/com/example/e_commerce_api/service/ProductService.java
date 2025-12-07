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

    // --- FIX 1: saveProduct (Used for both CREATE and initial UPDATE) ---
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    // --- FIX 2: findAllProducts (Used for READ All) ---
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    // --- FIX 3: findProductById (Used for READ Single) ---
    public Optional<Product> findProductById(Long id) {
        return productRepository.findById(id);
    }
    
    // The update and delete methods from Step 7 should also be here:
    
    public Product updateProduct(Long id, Product productDetails) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        existingProduct.setName(productDetails.getName());
        existingProduct.setDescription(productDetails.getDescription());
        existingProduct.setPrice(productDetails.getPrice());
        existingProduct.setStockQuantity(productDetails.getStockQuantity());
        existingProduct.setAvailable(productDetails.isAvailable());
        
        return productRepository.save(existingProduct);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}