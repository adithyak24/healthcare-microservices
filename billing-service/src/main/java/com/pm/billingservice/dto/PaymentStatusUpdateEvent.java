package com.pm.billingservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentStatusUpdateEvent {
    private String patientId;
    private String stripeSessionId;
    private String paymentStatus; // e.g., "COMPLETED", "FAILED", "EXPIRED"
    private BigDecimal amount;
    private String currency;
    private LocalDateTime eventTimestamp;

    // Constructors
    public PaymentStatusUpdateEvent() {
    }

    public PaymentStatusUpdateEvent(String patientId, String stripeSessionId, String paymentStatus, BigDecimal amount, String currency, LocalDateTime eventTimestamp) {
        this.patientId = patientId;
        this.stripeSessionId = stripeSessionId;
        this.paymentStatus = paymentStatus;
        this.amount = amount;
        this.currency = currency;
        this.eventTimestamp = eventTimestamp;
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

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(LocalDateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    @Override
    public String toString() {
        return "PaymentStatusUpdateEvent{" +
                "patientId='" + patientId + '\'' +
                ", stripeSessionId='" + stripeSessionId + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", eventTimestamp=" + eventTimestamp +
                '}';
    }
} 