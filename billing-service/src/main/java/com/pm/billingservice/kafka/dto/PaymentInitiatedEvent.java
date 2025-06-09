package com.pm.billingservice.kafka.dto;

import java.time.LocalDateTime;

public class PaymentInitiatedEvent {
    private String patientId;
    private String stripeSessionId; // To potentially correlate later if needed
    private Double amount;
    private String currency;
    private String productName;
    private LocalDateTime initiatedTimestamp;

    // Constructors
    public PaymentInitiatedEvent() {
    }

    public PaymentInitiatedEvent(String patientId, String stripeSessionId, Double amount, String currency, String productName, LocalDateTime initiatedTimestamp) {
        this.patientId = patientId;
        this.stripeSessionId = stripeSessionId;
        this.amount = amount;
        this.currency = currency;
        this.productName = productName;
        this.initiatedTimestamp = initiatedTimestamp;
    }

    // Getters and Setters
    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getStripeSessionId() {
        return stripeSessionId;
    }

    public void setStripeSessionId(String stripeSessionId) {
        this.stripeSessionId = stripeSessionId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public LocalDateTime getInitiatedTimestamp() {
        return initiatedTimestamp;
    }

    public void setInitiatedTimestamp(LocalDateTime initiatedTimestamp) {
        this.initiatedTimestamp = initiatedTimestamp;
    }

    @Override
    public String toString() {
        return "PaymentInitiatedEvent{" +
                "patientId='" + patientId + '\'' +
                ", stripeSessionId='" + stripeSessionId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", productName='" + productName + '\'' +
                ", initiatedTimestamp=" + initiatedTimestamp +
                '}';
    }
} 