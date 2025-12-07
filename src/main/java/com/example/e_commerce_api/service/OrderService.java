package com.example.e_commerce_api.service;

import com.example.e_commerce_api.model.*;
import com.example.e_commerce_api.repository.OrderItemRepository;
import com.example.e_commerce_api.repository.OrderRepository;
import com.example.e_commerce_api.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CartService cartService; 

    /**
     * Processes the checkout, converting the user's cart into a final Order.
     */
    @Transactional
    public Order placeOrder(Long userId) {
        // 1. Fetch the user's cart
        Cart cart = cartService.getCart(userId);
        List<CartItem> cartItems = cart.getItems();

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cannot place order: Cart is empty.");
        }

        // 2. Initialize Order details
        Order newOrder = new Order();
        newOrder.setUser(cart.getUser());
        newOrder.setStatus(OrderStatus.PENDING);
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 3. Process CartItems: Convert, Validate, Calculate, Update Inventory
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            int requestedQuantity = cartItem.getQuantity();

            // Stock Validation
            if (product.getStockQuantity() < requestedQuantity) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // Create OrderItem (permanent record)
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(newOrder); 
            orderItem.setProduct(product);
            orderItem.setQuantity(requestedQuantity);
            
            // CRITICAL: Snapshot the price at checkout time
            orderItem.setUnitPrice(product.getPrice()); 
            
            newOrder.getItems().add(orderItem);

            // Update total amount
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(requestedQuantity));
            totalAmount = totalAmount.add(itemTotal);
            
            // Decrease product stock quantity
            product.setStockQuantity(product.getStockQuantity() - requestedQuantity);
            productRepository.save(product);
        }

        // 4. Finalize and Save Order
        newOrder.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(newOrder);

        // 5. Clear the Cart after successful order placement
        cartService.clearCart(userId);

        return savedOrder;
    }

    /**
     * Retrieves a list of all orders for the current user.
     */
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
    }
}