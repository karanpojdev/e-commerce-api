package com.example.e_commerce_api.dto;

public class CheckoutResponse {
    private Long orderId;
    private String clientSecret;

    public CheckoutResponse(Long orderId, String clientSecret) {
        this.orderId = orderId;
        this.clientSecret = clientSecret;
    }

    // Add standard getters and setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
}