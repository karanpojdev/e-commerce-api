package com.example.e_commerce_api.service;

import com.example.e_commerce_api.model.Cart;
import com.example.e_commerce_api.model.CartItem;
import com.example.e_commerce_api.model.Product;
import com.example.e_commerce_api.model.User;
import com.example.e_commerce_api.repository.CartItemRepository;
import com.example.e_commerce_api.repository.CartRepository;
import com.example.e_commerce_api.repository.ProductRepository;
import com.example.e_commerce_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Finds the user's cart or creates a new one if it doesn't exist.
     */
    @Transactional
    public Cart getOrCreateCart(Long userId) {
        // 1. Try to find the existing cart using the User ID
        Optional<Cart> existingCart = cartRepository.findByUserId(userId);
        
        if (existingCart.isPresent()) {
            return existingCart.get();
        }

        // 2. If no cart exists, create a new one
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        Cart newCart = new Cart();
        newCart.setUser(user);
        
        // Ensure bidirectional link is set on the User (important for @MapsId setup)
        user.setCart(newCart); 
        
        return cartRepository.save(newCart);
    }

    /**
     * Adds a specific quantity of a product to the user's cart.
     * If the product is already in the cart, it updates the quantity.
     */
    @Transactional
    public Cart addProductToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
        
        Cart cart = getOrCreateCart(userId);
        
        // Find existing CartItem
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingItem.isPresent()) {
            // Update quantity if item exists
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            // Create a new CartItem if it doesn't exist
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
            
            // Add the new item to the Cart's collection for Hibernate to manage
            cart.getItems().add(newItem); 
        }

        return cart;
    }

    /**
     * Updates the quantity of a product in the cart. If quantity is <= 0, the item is removed.
     */
    @Transactional
    public Cart updateProductQuantity(Long userId, Long productId, int quantity) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new RuntimeException("Product not in cart."));

        if (quantity <= 0) {
            // Remove the item from both the cart's collection and the database
            cart.getItems().remove(item);
            cartItemRepository.delete(item); 
        } else {
            // Update the quantity
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return cart;
    }

    /**
     * Views the current state of the user's cart.
     */
    public Cart getCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user ID: " + userId));
    }

    /**
     * Clears all items from the user's cart.
     */
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user ID: " + userId));
        
        // Delete all associated CartItems and clear the cart's collection
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        
        cartRepository.save(cart); // Persist the change to the cart object (clearing the collection)
    }
}