package com.pm.billingservice.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitPaymentCompletedEvent {
    private UUID patientId;
    private Long visitId;
    private Long paymentAttemptId; // The ID of the PaymentAttempt record in billing-service
    private LocalDateTime paymentTimestamp;
    
    public UUID getPatientId() {
        return patientId;
    }
    
    public Long getVisitId() {
        return visitId;
    }
    
    public Long getPaymentAttemptId() {
        return paymentAttemptId;
    }
    
    public LocalDateTime getPaymentTimestamp() {
        return paymentTimestamp;
    }
} 