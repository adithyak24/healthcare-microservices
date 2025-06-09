package com.pm.billingservice.web;

import com.google.gson.JsonSyntaxException;
import com.pm.billingservice.kafka.BillingKafkaProducer;
import com.pm.billingservice.kafka.dto.VisitPaymentCompletedEvent;
import com.pm.billingservice.kafka.dto.InitialConsultationPaymentCompletedEvent;
import com.pm.billingservice.model.PaymentAttempt;
import com.pm.billingservice.repository.PaymentAttemptRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/billing/stripe-webhooks")
public class StripeWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookController.class);

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private final PaymentAttemptRepository paymentAttemptRepository;
    private final BillingKafkaProducer billingKafkaProducer;

    @Autowired
    public StripeWebhookController(PaymentAttemptRepository paymentAttemptRepository, 
                                 BillingKafkaProducer billingKafkaProducer) {
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.billingKafkaProducer = billingKafkaProducer;
    }

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader, HttpServletRequest request) {
        if (endpointSecret == null || endpointSecret.trim().isEmpty() || endpointSecret.equals("your-webhook-secret-here")) {
            logger.error("Stripe webhook secret is not configured. Rejecting webhook event.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook secret not configured.");
        }
        
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (JsonSyntaxException e) {
            logger.error("Webhook error: Invalid payload format.", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload");
        } catch (SignatureVerificationException e) {
            logger.error("Webhook error: Invalid signature.", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            logger.error("Webhook error: Could not construct event.", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not construct event");
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            logger.warn("Webhook event data object deserialization failed for event ID: {}", event.getId());
            // Depending on the event type, this might be acceptable or an error.
        }

        logger.info("Received Stripe event: id={}, type={}", event.getId(), event.getType());

        switch (event.getType()) {
            case "checkout.session.completed":
                Session session = (Session) stripeObject;
                if (session != null) {
                    logger.info("Checkout session completed for Stripe Session ID: {}. Payment status: {}", session.getId(), session.getPaymentStatus());
                    handleCheckoutSessionCompleted(session);
                } else {
                     logger.error("StripeObject could not be cast to Checkout Session for event ID: {}", event.getId());
                }
                break;
            case "charge.succeeded":
                Session asyncSession = (Session) stripeObject;
                if (asyncSession != null) {
                    logger.info("Checkout session async payment succeeded for Stripe Session ID: {}. Payment status: {}", asyncSession.getId(), asyncSession.getPaymentStatus());
                    handleCheckoutSessionCompleted(asyncSession); // Same logic as completed
                } else {
                     logger.error("StripeObject could not be cast to Checkout Session (async payment succeeded) for event ID: {}", event.getId());
                }
                break;
            case "checkout.session.expired":
                Session expiredSession = (Session) stripeObject;
                 if (expiredSession != null) {
                    logger.info("Checkout session expired for Stripe Session ID: {}", expiredSession.getId());
                    handleCheckoutSessionExpired(expiredSession);
                } else {
                     logger.error("StripeObject could not be cast to Checkout Session (expired) for event ID: {}", event.getId());
                }
                break;
            // Add other event types to handle as needed (e.g., payment_intent.succeeded, payment_intent.payment_failed, etc.)
            default:
                logger.warn("Unhandled Stripe event type: {}", event.getType());
        }

        return ResponseEntity.ok("Received");
    }

    private void handleCheckoutSessionCompleted(Session session) {
        String stripeSessionId = session.getId();
        String clientReferenceId = session.getClientReferenceId(); // This is our PaymentAttempt.id for visit fees
        String customerEmail = null;
        
        // Extract customer email from session
        if (session.getCustomerDetails() != null && session.getCustomerDetails().getEmail() != null) {
            customerEmail = session.getCustomerDetails().getEmail();
            logger.info("Customer email from session: {}", customerEmail);
        }
        
        Optional<PaymentAttempt> paymentAttemptOptional = Optional.empty();

        // For visit fees, we expect clientReferenceId to be set to our PaymentAttempt ID
        if (clientReferenceId != null) {
            try {
                Long paymentAttemptId = Long.parseLong(clientReferenceId);
                paymentAttemptOptional = paymentAttemptRepository.findById(paymentAttemptId);
                 if(paymentAttemptOptional.isPresent()){
                    logger.info("Found PaymentAttempt by clientReferenceId (PaymentAttempt ID): {}", paymentAttemptId);
                } else {
                    logger.warn("No PaymentAttempt found for clientReferenceId: {}. Will try lookup by Stripe Session ID.", clientReferenceId);
                }
            } catch (NumberFormatException e) {
                logger.warn("Could not parse clientReferenceId '{}' to Long. Will try lookup by Stripe Session ID.", clientReferenceId);
            }
        }

        // Fallback or primary method for initial consultations: find by Stripe Session ID
        if (paymentAttemptOptional.isEmpty()) {
            paymentAttemptOptional = paymentAttemptRepository.findByStripeSessionId(stripeSessionId);
            if(paymentAttemptOptional.isPresent()){
                 logger.info("Found PaymentAttempt by Stripe Session ID: {}", stripeSessionId);
            }
        }
        
        // Log customer email for tracking purposes
        if (customerEmail != null) {
            logger.info("Payment completed for customer email: {}", customerEmail);
        }

        if (paymentAttemptOptional.isPresent()) {
            PaymentAttempt paymentAttempt = paymentAttemptOptional.get();
            // Ensure status is not already COMPLETED to avoid reprocessing
            if (paymentAttempt.getStatus() == PaymentAttempt.PaymentStatus.COMPLETED) {
                logger.info("PaymentAttempt ID: {} is already COMPLETED. Skipping update.", paymentAttempt.getId());
                return;
            }

            paymentAttempt.setStatus(PaymentAttempt.PaymentStatus.COMPLETED);
            paymentAttemptRepository.save(paymentAttempt);
            logger.info("Updated PaymentAttempt ID: {} to COMPLETED.", paymentAttempt.getId());

            // Check payment type and publish appropriate event
            if (paymentAttempt.getPaymentType() == PaymentAttempt.PaymentType.VISIT_FEE) {
                VisitPaymentCompletedEvent visitPaymentEvent = new VisitPaymentCompletedEvent(
                        paymentAttempt.getPatientId(),
                        paymentAttempt.getVisitId(),
                        paymentAttempt.getId(),
                        LocalDateTime.now()
                );
                billingKafkaProducer.sendVisitPaymentCompletedEvent(visitPaymentEvent);
                logger.info("Published VisitPaymentCompletedEvent for PaymentAttempt ID: {}", paymentAttempt.getId());
            } else if (paymentAttempt.getPaymentType() == PaymentAttempt.PaymentType.INITIAL_CONSULTATION) {
                InitialConsultationPaymentCompletedEvent consultationPaymentEvent = new InitialConsultationPaymentCompletedEvent(
                        paymentAttempt.getPatientId(),
                        paymentAttempt.getId(),
                        paymentAttempt.getAmount(),
                        paymentAttempt.getCurrency(),
                        LocalDateTime.now()
                );
                billingKafkaProducer.sendInitialConsultationPaymentCompletedEvent(consultationPaymentEvent);
                logger.info("Published InitialConsultationPaymentCompletedEvent for PaymentAttempt ID: {}", paymentAttempt.getId());
            }
        } else {
            logger.warn("No PaymentAttempt found for Stripe Session ID: {} or ClientReferenceID: {}. Cannot update status.", stripeSessionId, clientReferenceId);
        }
    }

    private void handleCheckoutSessionExpired(Session session) {
        String stripeSessionId = session.getId();
        Optional<PaymentAttempt> paymentAttemptOptional = paymentAttemptRepository.findByStripeSessionId(stripeSessionId);

        if (paymentAttemptOptional.isPresent()) {
            PaymentAttempt paymentAttempt = paymentAttemptOptional.get();
            if (paymentAttempt.getStatus() == PaymentAttempt.PaymentStatus.PENDING) { // Only update if it was pending
                paymentAttempt.setStatus(PaymentAttempt.PaymentStatus.EXPIRED);
                paymentAttemptRepository.save(paymentAttempt);
                logger.info("Updated PaymentAttempt for Stripe Session ID: {} to EXPIRED.", stripeSessionId);
            } else {
                 logger.info("PaymentAttempt for Stripe Session ID: {} was not PENDING (status: {}). Not changing to EXPIRED.", stripeSessionId, paymentAttempt.getStatus());
            }
        } else {
            logger.warn("No PaymentAttempt found for expired Stripe Session ID: {}. Cannot update status.", stripeSessionId);
        }
    }
} 