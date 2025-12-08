package com.example.e_commerce_api.service;

import com.example.e_commerce_api.model.*;
import com.example.e_commerce_api.repository.OrderRepository;
import com.example.e_commerce_api.repository.ProductRepository;
import com.stripe.model.PaymentIntent;
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
    @Autowired
    private PaymentService paymentService;

    /**
     * Processes the checkout, converting the user's cart into a final Order.
     */
    @Transactional
    public PaymentIntent placeOrder(Long userId) throws Exception {
        // 1. Fetch the user's cart and validate
        Cart cart = cartService.getCart(userId);
        List<CartItem> cartItems = cart.getItems();

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cannot place order: Cart is empty.");
        }

        // 2. Initialize Order and calculate total
        Order newOrder = new Order();
        newOrder.setUser(cart.getUser());
        newOrder.setStatus(OrderStatus.PENDING); // New intermediate status
        BigDecimal totalAmountDecimal = BigDecimal.ZERO;

        // 3. Process CartItems: Validate Stock and Create OrderItems
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            int requestedQuantity = cartItem.getQuantity();

            // Stock Validation (Still critical to check availability)
            if (product.getStockQuantity() < requestedQuantity) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // Create OrderItem (permanent record)
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(newOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(requestedQuantity);
            orderItem.setUnitPrice(product.getPrice());
            newOrder.getItems().add(orderItem);

            // Update total amount
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(requestedQuantity));
            totalAmountDecimal = totalAmountDecimal.add(itemTotal);

            // >>> IMPORTANT: Stock is NOT reduced here. It is reserved for the Webhook.
        }

        // 4. Finalize and Save Order (to get the Order ID)
        newOrder.setTotalAmount(totalAmountDecimal);
        Order savedOrder = orderRepository.save(newOrder);

        // 5. Convert Total to Long Cents (Stripe Requirement)
        Long totalAmountCents = totalAmountDecimal.multiply(new BigDecimal("100")).longValue();

        // 6. Create Stripe Payment Intent
        try {
            PaymentIntent intent = paymentService.createPaymentIntent(
                totalAmountCents,
                "usd", // Use your desired currency (e.g., "usd", "eur")
                savedOrder.getId()
            );

            // 7. Update Order with Payment Intent ID and save
            // This paymentIntentId must be a String field on your Order model.
            savedOrder.setPaymentIntentId(intent.getId());
            orderRepository.save(savedOrder);

            // 8. Return the Intent, allowing the controller to extract the client_secret
            return intent;

        } catch (Exception e) {
            // If Stripe fails, log the error and ensure the transaction rolls back
            System.err.println("Stripe Payment Intent creation failed: " + e.getMessage());
            // Optionally, delete the PENDING order here, or rely on @Transactional rollback
            throw new RuntimeException("Payment initiation failed, please try again.");
        }

        // >>> NOTE: cartService.clearCart(userId) is REMOVED.
        // Both cart clearing and stock reduction must be handled by a Stripe Webhook 
        // endpoint once payment is officially confirmed by Stripe.
    }

    /**
     * Retrieves a list of all orders for the current user.
     */
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
    }

    /**
     * Fulfills the order after Stripe confirms payment.
     * This method is called by the StripeWebhookController.
     */
    @Transactional
    public void finalizeOrderFulfillment(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // Prevent double processing (idempotency)
        if (OrderStatus.PAID.equals(order.getStatus())) {
            System.out.println("Order " + orderId + " already paid. Skipping fulfillment.");
            return;
        }

        // 1. Process Order Items (Stock Reduction)
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            int quantity = item.getQuantity();

            // Stock Validation check (should ideally pass since it was checked earlier, but safe to verify)
            if (product.getStockQuantity() < quantity) {
                // This is a critical failure. Log and throw. Manual intervention needed.
                throw new RuntimeException("Stock check failed during fulfillment for product: " + product.getName());
            }

            // Decrease product stock quantity
            product.setStockQuantity(product.getStockQuantity() - quantity);
            productRepository.save(product);
        }

        // 2. Clear the Cart (Only clear the cart associated with the user of this order)
        cartService.clearCart(order.getUser().getId());

        // 3. Update Order Status to PAID/COMPLETED
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        // Optional: Send confirmation email, generate invoice, etc.
    }
}