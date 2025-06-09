package com.pm.billingservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentAttemptResponseDTO {
    private Long id;
    private UUID patientId;
    private String stripeSessionId;
    private BigDecimal amount;
    private String currency;
    private String productName;
    private String status;
    private String paymentType;
    private Long visitId;
    private LocalDateTime createdTimestamp;  // Frontend expects this name
    private LocalDateTime updatedAt;

    public PaymentAttemptResponseDTO(Long id, UUID patientId, String stripeSessionId, BigDecimal amount, 
                                   String currency, String productName, String status, String paymentType, 
                                   Long visitId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.patientId = patientId;
        this.stripeSessionId = stripeSessionId;
        this.amount = amount;
        this.currency = currency;
        this.productName = productName;
        this.status = status;
        this.paymentType = paymentType;
        this.visitId = visitId;
        this.createdTimestamp = createdAt;  // Map createdAt to createdTimestamp for frontend
        this.updatedAt = updatedAt;
    }

    // Getters
    public Long getId() { return id; }
    public UUID getPatientId() { return patientId; }
    public String getStripeSessionId() { return stripeSessionId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getProductName() { return productName; }
    public String getStatus() { return status; }
    public String getPaymentType() { return paymentType; }
    public Long getVisitId() { return visitId; }
    public LocalDateTime getCreatedTimestamp() { return createdTimestamp; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
} 