package com.pm.patientservice.kafka.dto;

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
    private Long paymentAttemptId; // ID of the PaymentAttempt in billing-service (for reference/logging)
    private LocalDateTime paymentTimestamp;
} 