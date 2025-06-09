package com.pm.patientservice.kafka.dto;

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
    // Consider adding a timestamp if useful for consumers
    // private LocalDateTime eventTimestamp;
} 