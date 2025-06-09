package com.pm.billingservice.kafka;

import com.pm.billingservice.kafka.dto.PaymentCompletedEvent;
import com.pm.billingservice.kafka.dto.VisitPaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

// Import for new event
import com.pm.billingservice.kafka.dto.PaymentInitiatedEvent;
import com.pm.billingservice.kafka.dto.InitialConsultationPaymentCompletedEvent;
import com.pm.billingservice.dto.PaymentStatusUpdateEvent;
import org.springframework.beans.factory.annotation.Value;

@Service
public class BillingKafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(BillingKafkaProducer.class);
    private static final String TOPIC_PAYMENT_COMPLETED = "payment-completed-events";
    private static final String TOPIC_PAYMENT_INITIATED = "payment-initiated-events";
    private static final String TOPIC_VISIT_PAYMENT_COMPLETED = "visit-payment-completed";
    private static final String TOPIC_INITIAL_CONSULTATION_PAYMENT_COMPLETED = "initial-consultation-payment-completed";

    private final KafkaTemplate<String, Object> kafkaTemplate; // Changed to Object to handle multiple event types

    @Value("${kafka.topic.payment-status-updates}")
    private String paymentStatusUpdateTopic;

    @Autowired
    public BillingKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentCompletedEvent(PaymentCompletedEvent event) {
        try {
            logger.info("Sending PaymentCompletedEvent to Kafka topic {}: {}", TOPIC_PAYMENT_COMPLETED, event);
            kafkaTemplate.send(TOPIC_PAYMENT_COMPLETED, event.getStripeSessionId(), event);
            logger.info("Successfully sent PaymentCompletedEvent for Stripe Session ID: {}", event.getStripeSessionId());
        } catch (Exception e) {
            logger.error("Error sending PaymentCompletedEvent to Kafka for Stripe Session ID: {}", event.getStripeSessionId(), e);
            // Consider retry mechanisms or dead-letter queue for production
        }
    }

    public void sendPaymentInitiatedEvent(PaymentInitiatedEvent event) {
        try {
            logger.info("Sending PaymentInitiatedEvent to Kafka topic {}: {}", TOPIC_PAYMENT_INITIATED, event);
            kafkaTemplate.send(TOPIC_PAYMENT_INITIATED, event.getStripeSessionId(), event);
            logger.info("Successfully sent PaymentInitiatedEvent for Stripe Session ID: {}", event.getStripeSessionId());
        } catch (Exception e) {
            logger.error("Error sending PaymentInitiatedEvent to Kafka for Stripe Session ID: {}", event.getStripeSessionId(), e);
        }
    }

    public void sendPaymentStatusUpdateEvent(PaymentStatusUpdateEvent event) {
        try {
            logger.info("Sending PaymentStatusUpdateEvent to topic {}: Patient ID: {}, Stripe Session ID: {}, Status: {}",
                    paymentStatusUpdateTopic, event.getPatientId(), event.getStripeSessionId(), event.getPaymentStatus());
            kafkaTemplate.send(paymentStatusUpdateTopic, event.getStripeSessionId(), event);
        } catch (Exception e) {
            logger.error("Error sending PaymentStatusUpdateEvent for patient ID {}: {}", event.getPatientId(), e.getMessage(), e);
        }
    }

    public void sendVisitPaymentCompletedEvent(VisitPaymentCompletedEvent event) {
        try {
            logger.info("Sending VisitPaymentCompletedEvent to Kafka topic {}: Patient ID: {}, Visit ID: {}, PaymentAttempt ID: {}",
                    TOPIC_VISIT_PAYMENT_COMPLETED, event.getPatientId(), event.getVisitId(), event.getPaymentAttemptId());
            kafkaTemplate.send(TOPIC_VISIT_PAYMENT_COMPLETED, event.getPatientId().toString(), event);
            logger.info("Successfully sent VisitPaymentCompletedEvent for Visit ID: {}", event.getVisitId());
        } catch (Exception e) {
            logger.error("Error sending VisitPaymentCompletedEvent for Visit ID {}: {}", event.getVisitId(), e.getMessage(), e);
        }
    }

    public void sendInitialConsultationPaymentCompletedEvent(InitialConsultationPaymentCompletedEvent event) {
        try {
            logger.info("Sending InitialConsultationPaymentCompletedEvent to Kafka topic {}: Patient ID: {}, PaymentAttempt ID: {}",
                    TOPIC_INITIAL_CONSULTATION_PAYMENT_COMPLETED, event.getPatientId(), event.getPaymentAttemptId());
            kafkaTemplate.send(TOPIC_INITIAL_CONSULTATION_PAYMENT_COMPLETED, event.getPatientId().toString(), event);
            logger.info("Successfully sent InitialConsultationPaymentCompletedEvent for Patient ID: {}", event.getPatientId());
        } catch (Exception e) {
            logger.error("Error sending InitialConsultationPaymentCompletedEvent for Patient ID {}: {}", event.getPatientId(), e.getMessage(), e);
        }
    }
} 