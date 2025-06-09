package com.pm.patientservice.kafka;

import com.pm.patientservice.kafka.dto.external.PaymentStatusUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentStatusConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentStatusConsumer.class);

    @KafkaListener(topics = "${kafka.topic.payment-status-updates}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumePaymentStatusUpdate(PaymentStatusUpdateEvent event) {
        try {
            logger.info("Received PaymentStatusUpdateEvent: Patient ID: {}, Stripe Session ID: {}, Status: {}, Amount: {} {}, Timestamp: {}",
                    event.getPatientId(),
                    event.getStripeSessionId(),
                    event.getPaymentStatus(),
                    event.getAmount(),
                    event.getCurrency(),
                    event.getEventTimestamp());

            // TODO: Implement logic based on payment status.
            // For example:
            // if ("COMPLETED".equalsIgnoreCase(event.getPaymentStatus())) {
            //     // Update patient record, unlock features, etc.
            //     logger.info("Payment COMPLETED for patient {}", event.getPatientId());
            // } else if ("FAILED".equalsIgnoreCase(event.getPaymentStatus())) {
            //     // Handle failed payment, notify patient or admin, etc.
            //     logger.warn("Payment FAILED for patient {}", event.getPatientId());
            // } else if ("EXPIRED".equalsIgnoreCase(event.getPaymentStatus())) {
            //     // Handle expired payment session
            //     logger.warn("Payment session EXPIRED for patient {}", event.getPatientId());
            // }

        } catch (Exception e) {
            logger.error("Error processing PaymentStatusUpdateEvent for patient ID {}: {}", event.getPatientId(), e.getMessage(), e);
            // Consider dead-letter queue or other error handling
        }
    }
} 