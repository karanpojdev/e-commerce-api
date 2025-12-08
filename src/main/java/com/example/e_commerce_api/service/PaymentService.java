package com.example.e_commerce_api.service;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class PaymentService {

    @Value("${stripe.secret.key}")
    private String secretKey;

    // Initialize Stripe client after construction
    @PostConstruct
    public void init() {
        System.out.println("DEBUG: Loaded Stripe Secret Key (first 8 chars): " 
                           + (secretKey != null ? secretKey.substring(0, 8) : "KEY IS NULL/MISSING"));          
        Stripe.apiKey = secretKey;
    }

    /**
     * Creates a Payment Intent on the Stripe server.
     * @param amount Cents/smallest currency unit (e.g., $10.50 is 1050)
     * @param currency The currency code (e.g., "usd")
     * @param orderId Your internal order reference
     * @return The created PaymentIntent object
     */
    public PaymentIntent createPaymentIntent(Long amount, String currency, Long orderId) throws Exception {
        
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount) // Amount in cents
                .setCurrency(currency)
                .putMetadata("order_id", String.valueOf(orderId)) // Link to your internal order
                .setDescription("E-commerce Order ID: " + orderId)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                .build()
                )
                .build();

        return PaymentIntent.create(params);
    }
}