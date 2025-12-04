package com.example.e_commerce_api.controller;

import com.example.e_commerce_api.model.Product;
import com.example.e_commerce_api.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // 1. ADMIN - Create Product
    // POST http://localhost:8080/api/products
    @PostMapping
    // We'll secure this later so only ADMINs can access it
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    // 2. USER/ALL - Get All Products
    // GET http://localhost:8080/api/products
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllAvailableProducts();
    }

    // 3. USER/ALL - Get Product by ID
    // GET http://localhost:8080/api/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 4. ADMIN - Update Product
    // PUT http://localhost:8080/api/products/{id}
    @PutMapping("/{id}")
    // We'll secure this later so only ADMINs can access it
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        try {
            Product updatedProduct = productService.updateProduct(id, productDetails);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 5. ADMIN - Delete Product
    // DELETE http://localhost:8080/api/products/{id}
    @DeleteMapping("/{id}")
    // We'll secure this later so only ADMINs can access it
    public ResponseEntity<HttpStatus> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}