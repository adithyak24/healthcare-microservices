package com.pm.billingservice.kafka;

import com.pm.billingservice.kafka.dto.VisitFeeChargeRequestedEvent;
import com.pm.billingservice.model.PaymentAttempt;
import com.pm.billingservice.repository.PaymentAttemptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.Optional;

@Service
public class VisitFeeConsumer {

    private static final Logger logger = LoggerFactory.getLogger(VisitFeeConsumer.class);
    private static final String VISIT_FEE_TOPIC = "visit-fee-charge-requested";

    private final PaymentAttemptRepository paymentAttemptRepository;

    @Autowired
    public VisitFeeConsumer(PaymentAttemptRepository paymentAttemptRepository) {
        this.paymentAttemptRepository = paymentAttemptRepository;
    }

    @KafkaListener(topics = VISIT_FEE_TOPIC, groupId = "billing_group_visit_fee", containerFactory = "visitFeeKafkaListenerContainerFactory")
    public void consumeVisitFeeChargeRequested(@Payload VisitFeeChargeRequestedEvent event) {
        logger.info("Received VisitFeeChargeRequestedEvent for patient ID: {}, visit ID: {}, amount: {}",
                event.getPatientId(), event.getVisitId(), event.getFeeAmount());

        try {
            // Check if payment attempt already exists for this visit
            Optional<PaymentAttempt> existingAttempt = paymentAttemptRepository.findByPatientIdAndVisitIdAndPaymentTypeAndStatus(
                    event.getPatientId(), 
                    event.getVisitId(), 
                    PaymentAttempt.PaymentType.VISIT_FEE, 
                    PaymentAttempt.PaymentStatus.AWAITING_PAYMENT
            );
            
            if (existingAttempt.isPresent()) {
                logger.info("Payment attempt already exists for patient {} and visit {}. Skipping creation.", 
                           event.getPatientId(), event.getVisitId());
                return;
            }

            PaymentAttempt paymentAttempt = new PaymentAttempt();
            paymentAttempt.setPatientId(event.getPatientId());
            paymentAttempt.setVisitId(event.getVisitId());
            paymentAttempt.setAmount(event.getFeeAmount());
            paymentAttempt.setCurrency("USD"); // Assuming USD, or make this configurable/part of event
            paymentAttempt.setProductName("Consultation Fee for Visit #" + event.getVisitId());
            paymentAttempt.setPaymentType(PaymentAttempt.PaymentType.VISIT_FEE);
            paymentAttempt.setStatus(PaymentAttempt.PaymentStatus.AWAITING_PAYMENT); // Initial status

            paymentAttemptRepository.save(paymentAttempt);
            logger.info("Successfully created PaymentAttempt for visit ID: {}. PaymentAttempt ID: {}",
                    event.getVisitId(), paymentAttempt.getId());

        } catch (Exception e) {
            logger.error("Error processing VisitFeeChargeRequestedEvent for visit ID: {}: {}",
                    event.getVisitId(), e.getMessage(), e);
            // Consider dead-letter queue or other error handling mechanisms here
        }
    }
} 