package com.pm.billingservice.kafka.dto;

import java.time.LocalDateTime;

public class PaymentCompletedEvent {
    private String paymentAttemptId; // Internal DB ID of the payment attempt
    private String stripeSessionId;
    private String patientId;
    private Double amountPaid;
    private String currency;
    private String productName;
    private LocalDateTime paymentTimestamp;

    // Constructors, Getters, and Setters

    public PaymentCompletedEvent() {
    }

    public PaymentCompletedEvent(String paymentAttemptId, String stripeSessionId, String patientId, Double amountPaid, String currency, String productName, LocalDateTime paymentTimestamp) {
        this.paymentAttemptId = paymentAttemptId;
        this.stripeSessionId = stripeSessionId;
        this.patientId = patientId;
        this.amountPaid = amountPaid;
        this.currency = currency;
        this.productName = productName;
        this.paymentTimestamp = paymentTimestamp;
    }

    public String getPaymentAttemptId() {
        return paymentAttemptId;
    }

    public void setPaymentAttemptId(String paymentAttemptId) {
        this.paymentAttemptId = paymentAttemptId;
    }

    public String getStripeSessionId() {
        return stripeSessionId;
    }

    public void setStripeSessionId(String stripeSessionId) {
        this.stripeSessionId = stripeSessionId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
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

    public LocalDateTime getPaymentTimestamp() {
        return paymentTimestamp;
    }

    public void setPaymentTimestamp(LocalDateTime paymentTimestamp) {
        this.paymentTimestamp = paymentTimestamp;
    }

    @Override
    public String toString() {
        return "PaymentCompletedEvent{" +
                "paymentAttemptId='" + paymentAttemptId + '\'' +
                ", stripeSessionId='" + stripeSessionId + '\'' +
                ", patientId='" + patientId + '\'' +
                ", amountPaid=" + amountPaid +
                ", currency='" + currency + '\'' +
                ", productName='" + productName + '\'' +
                ", paymentTimestamp=" + paymentTimestamp +
                '}';
    }
} 