package com.pm.patientservice.kafka;

import com.pm.billingservice.kafka.dto.PaymentInitiatedEvent;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventConsumer.class);
    private static final String TOPIC_PAYMENT_INITIATED = "payment-initiated-events";

    private final PatientRepository patientRepository;

    @Autowired
    public PaymentEventConsumer(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @KafkaListener(topics = TOPIC_PAYMENT_INITIATED, groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumePaymentInitiatedEvent(PaymentInitiatedEvent event) {
        logger.info("Received PaymentInitiatedEvent: {}", event);
        try {
            UUID patientUuid = UUID.fromString(event.getPatientId());
            Patient patient = patientRepository.findById(patientUuid).orElse(null);

            if (patient != null) {
                // Update status only if it's not already paid or pending from a more recent event (idempotency check)
                // For this simplified flow, we directly set to PENDING.
                // A more robust check might involve timestamps if multiple initiations are possible.
                patient.setConsultationPaymentStatus(Patient.ConsultationPaymentStatus.PAYMENT_PENDING);
                patient.setLastPaymentUpdateTimestamp(LocalDateTime.now());
                patientRepository.save(patient);
                logger.info("Patient ID: {} consultation payment status updated to PENDING.", patient.getId());
            } else {
                logger.warn("Patient not found with ID: {}. Cannot update payment status from PaymentInitiatedEvent.", event.getPatientId());
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid Patient ID format in PaymentInitiatedEvent: {}. Error: {}", event.getPatientId(), e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing PaymentInitiatedEvent for patient ID {}: {}", event.getPatientId(), e.getMessage(), e);
            // Consider dead-letter queue or other error handling
        }
    }

    // TODO: Later, add another @KafkaListener method here for "payment-completed-events"
    // when webhook processing is re-enabled in billing-service. That listener would update
    // the status to PAID or FAILED based on the PaymentCompletedEvent.
} 