package com.pm.patientservice.kafka;

import com.pm.patientservice.kafka.dto.VisitPaymentCompletedEvent;
import com.pm.patientservice.kafka.dto.InitialConsultationPaymentCompletedEvent;
import com.pm.patientservice.services.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class BillingEventsConsumer {

    private static final Logger logger = LoggerFactory.getLogger(BillingEventsConsumer.class);
    private static final String VISIT_PAYMENT_COMPLETED_TOPIC = "visit-payment-completed";
    private static final String INITIAL_CONSULTATION_PAYMENT_COMPLETED_TOPIC = "initial-consultation-payment-completed";

    private final PatientService patientService;

    @Autowired
    public BillingEventsConsumer(PatientService patientService) {
        this.patientService = patientService;
    }

    @KafkaListener(topics = VISIT_PAYMENT_COMPLETED_TOPIC, groupId = "patient_service_visit_payment", containerFactory = "visitPaymentKafkaListenerContainerFactory")
    public void consumeVisitPaymentCompleted(@Payload VisitPaymentCompletedEvent event,
                                           @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                           @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                           @Header(KafkaHeaders.OFFSET) long offset) {
        logger.info("Received VisitPaymentCompletedEvent from topic: {}, partition: {}, offset: {} for patient ID: {}, visit ID: {}. Payment Timestamp: {}",
                topic, partition, offset, event.getPatientId(), event.getVisitId(), event.getPaymentTimestamp());

        try {
            // It's good practice for the service method to be idempotent
            patientService.updateVisitPaymentStatus(event.getPatientId(), event.getVisitId(), true /*isPaid*/);
            logger.info("Successfully processed VisitPaymentCompletedEvent for visit ID: {}", event.getVisitId());
        } catch (Exception e) {
            logger.error("Error processing VisitPaymentCompletedEvent for visit ID: {}: {}",
                    event.getVisitId(), e.getMessage(), e);
            // Consider dead-letter queue or other error handling mechanisms here
        }
    }

    @KafkaListener(topics = INITIAL_CONSULTATION_PAYMENT_COMPLETED_TOPIC, groupId = "patient_service_consultation_payment", containerFactory = "consultationPaymentKafkaListenerContainerFactory")
    public void consumeInitialConsultationPaymentCompleted(@Payload InitialConsultationPaymentCompletedEvent event,
                                                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                                          @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                                          @Header(KafkaHeaders.OFFSET) long offset) {
        logger.info("Received InitialConsultationPaymentCompletedEvent from topic: {}, partition: {}, offset: {} for patient ID: {}. Payment Timestamp: {}",
                topic, partition, offset, event.getPatientId(), event.getPaymentTimestamp());

        try {
            // Update patient consultation payment status to PAID
            patientService.updatePatientConsultationPaymentStatus(event.getPatientId(), true /*isPaid*/);
            logger.info("Successfully processed InitialConsultationPaymentCompletedEvent for patient ID: {}", event.getPatientId());
        } catch (Exception e) {
            logger.error("Error processing InitialConsultationPaymentCompletedEvent for patient ID: {}: {}",
                    event.getPatientId(), e.getMessage(), e);
            // Consider dead-letter queue or other error handling mechanisms here
        }
    }
} 