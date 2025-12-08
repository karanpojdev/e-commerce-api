package com.example.e_commerce_api.controller;

import com.example.e_commerce_api.service.OrderService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks") // Secured public endpoint for Stripe
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Autowired
    private OrderService orderService;

    /**
     * Endpoint to receive asynchronous events from Stripe.
     * This is the "Confirmation" step for the order.
     */
    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeEvent(@RequestBody String payload,
                                                    @RequestHeader("Stripe-Signature") String sigHeader) {
        
        Event event;
        System.out.println("$$$ DEBUG: REQUEST HIT CONTROLLER $$$");
        // 1. Validate the Webhook signature
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            // Invalid signature
            System.err.println("❌ Webhook signature verification failed.");
            return new ResponseEntity<>("Invalid signature", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("❌ Error processing webhook payload: " + e.getMessage());
            return new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST);
        }

        // 2. Handle the Event Type
        // We only care about payments that succeeded
        if ("payment_intent.succeeded".equals(event.getType())) {
            
            StripeObject dataObject = event.getDataObjectDeserializer().getObject().orElse(null);

            if (dataObject instanceof PaymentIntent) {
                PaymentIntent paymentIntent = (PaymentIntent) dataObject;
                
                // 3. Extract the Order ID from the metadata
                String orderIdString = paymentIntent.getMetadata().get("order_id");
                
                if (orderIdString == null) {
                    System.err.println("❌ Webhook received without 'order_id' metadata.");
                    return new ResponseEntity<>("No Order ID in metadata", HttpStatus.OK); // OK to avoid retries
                }

                try {
                    Long orderId = Long.parseLong(orderIdString);
                    System.out.println("✅ Payment Succeeded for Order ID: " + orderId);
                    
                    // 4. Finalize the Order (Stock Reduction & Cart Clear)
                    orderService.finalizeOrderFulfillment(orderId); 
                    
                } catch (NumberFormatException e) {
                    System.err.println("❌ Invalid Order ID in metadata: " + orderIdString);
                    return new ResponseEntity<>("Invalid Order ID", HttpStatus.OK);
                } catch (Exception e) {
                    // This handles potential errors in orderService.finalizeOrderFulfillment
                    System.err.println("❌ Error finalizing order: " + e.getMessage());
                    // Stripe will retry this webhook if a 5xx error is returned. 
                    return new ResponseEntity<>("Internal Server Error during fulfillment", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }
        
        // Return 200 OK for events we don't care about, to avoid Stripe retrying
        return new ResponseEntity<>("Webhook Received", HttpStatus.OK);
    }
}