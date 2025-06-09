package com.pm.billingservice.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitFeeChargeRequestedEvent {
    private UUID patientId;
    private Long visitId;
    private BigDecimal feeAmount;
    // Add a timestamp if it was added in the producer and is needed
    
    public UUID getPatientId() {
        return patientId;
    }
    
    public Long getVisitId() {
        return visitId;
    }
    
    public BigDecimal getFeeAmount() {
        return feeAmount;
    }
} 