package com.pm.billingservice.kafka.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;

public class InitialConsultationPaymentCompletedEvent {
    private UUID patientId;
    private Long paymentAttemptId;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime paymentTimestamp;

    // Constructors
    public InitialConsultationPaymentCompletedEvent() {
    }

    public InitialConsultationPaymentCompletedEvent(UUID patientId, Long paymentAttemptId, 
                                                  BigDecimal amount, String currency, 
                                                  LocalDateTime paymentTimestamp) {
        this.patientId = patientId;
        this.paymentAttemptId = paymentAttemptId;
        this.amount = amount;
        this.currency = currency;
        this.paymentTimestamp = paymentTimestamp;
    }

    // Getters and Setters
    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public Long getPaymentAttemptId() {
        return paymentAttemptId;
    }

    public void setPaymentAttemptId(Long paymentAttemptId) {
        this.paymentAttemptId = paymentAttemptId;
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

    public LocalDateTime getPaymentTimestamp() {
        return paymentTimestamp;
    }

    public void setPaymentTimestamp(LocalDateTime paymentTimestamp) {
        this.paymentTimestamp = paymentTimestamp;
    }

    @Override
    public String toString() {
        return "InitialConsultationPaymentCompletedEvent{" +
                "patientId=" + patientId +
                ", paymentAttemptId=" + paymentAttemptId +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", paymentTimestamp=" + paymentTimestamp +
                '}';
    }
}