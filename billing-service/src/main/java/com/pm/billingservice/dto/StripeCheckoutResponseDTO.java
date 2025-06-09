package com.pm.billingservice.dto;

public class StripeCheckoutResponseDTO {
    private String sessionId;
    private String checkoutUrl;

    public StripeCheckoutResponseDTO(String sessionId, String checkoutUrl) {
        this.sessionId = sessionId;
        this.checkoutUrl = checkoutUrl;
    }

    // Getters
    public String getSessionId() {
        return sessionId;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    // No setters to make it immutable after creation
} 