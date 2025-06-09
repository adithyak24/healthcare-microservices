package com.pm.billingservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class StripeCheckoutRequestDTO {

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.50", message = "Amount must be at least 0.50") // Stripe has minimum amount requirements
    private BigDecimal amount; // Amount in major currency unit (e.g., dollars, euros)

    @NotBlank(message = "Currency is required")
    private String currency; // e.g., "usd", "eur"

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Long quantity;

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    // Getters and Setters
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }
} 