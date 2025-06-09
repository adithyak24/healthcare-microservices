package com.pm.billingservice.service;

import com.pm.billingservice.dto.PaymentAttemptResponseDTO;
import com.pm.billingservice.dto.StripeCheckoutRequestDTO;
import com.pm.billingservice.kafka.BillingKafkaProducer;
import com.pm.billingservice.model.PaymentAttempt;
import com.pm.billingservice.repository.PaymentAttemptRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StripeService {

    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${frontend.base.url}")
    private String frontendBaseUrl;

    private final PaymentAttemptRepository paymentAttemptRepository;
    private final BillingKafkaProducer billingKafkaProducer;

    public StripeService(PaymentAttemptRepository paymentAttemptRepository, BillingKafkaProducer billingKafkaProducer) {
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.billingKafkaProducer = billingKafkaProducer;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
        logger.info("Stripe API Key initialized.");
        if (stripeApiKey == null || !stripeApiKey.startsWith("sk_test_")) {
            logger.warn("Stripe API Key is not configured correctly. Stripe integration will not work.");
        }
        if (frontendBaseUrl == null || frontendBaseUrl.trim().isEmpty()) {
            logger.warn("Frontend base URL is not configured. Stripe success/cancel URLs may not work correctly.");
        }
    }

    @CircuitBreaker(name = "stripe-api", fallbackMethod = "createCheckoutSessionFallback")
    @Retry(name = "stripe-api")
    @Transactional
    public Session createCheckoutSessionForInitialConsultation(StripeCheckoutRequestDTO checkoutRequestDTO) throws StripeException {
        if (stripeApiKey == null || !stripeApiKey.startsWith("sk_test_")) {
            logger.error("Stripe API Key is not configured. Cannot create Stripe session.");
            throw new IllegalStateException("Stripe API Key is not configured. Please set stripe.api.key in application properties.");
        }

        String successUrl = frontendBaseUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl = frontendBaseUrl + "/payment/cancel";

        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(checkoutRequestDTO.getProductName())
                        .build();

        BigDecimal amountInBigDecimal = checkoutRequestDTO.getAmount();
        if (amountInBigDecimal == null) {
            logger.error("Amount is null in checkoutRequestDTO");
            throw new IllegalArgumentException("Amount cannot be null.");
        }

        SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency(checkoutRequestDTO.getCurrency().toLowerCase())
                .setProductData(productData)
                .setUnitAmount(amountInBigDecimal.multiply(BigDecimal.valueOf(100)).longValue())
                .build();

        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setPriceData(priceData)
                .setQuantity(checkoutRequestDTO.getQuantity() != null ? checkoutRequestDTO.getQuantity() : 1L)
                .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(lineItem)
                .build();

        logger.info("Creating Stripe Checkout session for initial consultation: {}, amount: {} {}, quantity: {}",
                checkoutRequestDTO.getProductName(), checkoutRequestDTO.getAmount(),
                checkoutRequestDTO.getCurrency(), checkoutRequestDTO.getQuantity());

        Session session = Session.create(params);
        logger.info("Stripe Checkout session created with ID: {}", session.getId());
        
        return session;
    }

    public Session createCheckoutSessionFallback(StripeCheckoutRequestDTO checkoutRequestDTO, Throwable t) {
        logger.error("Fallback for createCheckoutSessionForInitialConsultation invoked for patient {}. Error: {}", checkoutRequestDTO.getPatientId(), t.getMessage());
        throw new RuntimeException("Unable to create payment session for initial consultation at this time. Please try again later.", t);
    }

    @CircuitBreaker(name = "stripe-api", fallbackMethod = "createVisitCheckoutSessionFallback")
    @Retry(name = "stripe-api")
    @Transactional
    public Session createCheckoutSessionForVisit(UUID patientId, Long visitId) throws StripeException {
        BigDecimal visitFee = getVisitFee(visitId); 

        String successUrl = frontendBaseUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}&visitId=" + visitId;
        String cancelUrl = frontendBaseUrl + "/payment/cancel?visitId=" + visitId;

        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName("Follow-up Visit Payment")
                        .build();

        SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency("usd")
                .setProductData(productData)
                .setUnitAmount(visitFee.multiply(BigDecimal.valueOf(100)).longValue())
                .build();

        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setPriceData(priceData)
                .setQuantity(1L)
                .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(lineItem)
                .putMetadata("patientId", patientId.toString())
                .putMetadata("visitId", visitId.toString())
                .putMetadata("paymentType", "VISIT_FEE")
                .build();

        Session session = Session.create(params);
        logger.info("Created Stripe session {} for visit {}", session.getId(), visitId);

        // Update existing AWAITING_PAYMENT attempt with Stripe session ID instead of creating new one
        List<PaymentAttempt> existingAttempts = paymentAttemptRepository.findByVisitIdOrderByCreatedAtDesc(visitId);
        PaymentAttempt attemptToUpdate = null;
        
        for (PaymentAttempt attempt : existingAttempts) {
            if (attempt.getStatus() == PaymentAttempt.PaymentStatus.AWAITING_PAYMENT && 
                attempt.getPatientId().equals(patientId)) {
                attemptToUpdate = attempt;
                break;
            }
        }
        
        if (attemptToUpdate != null) {
            // Update existing attempt with Stripe session
            attemptToUpdate.setStripeSessionId(session.getId());
            attemptToUpdate.setStatus(PaymentAttempt.PaymentStatus.PENDING);
            paymentAttemptRepository.save(attemptToUpdate);
            logger.info("Updated existing PaymentAttempt {} with Stripe session {}", 
                       attemptToUpdate.getId(), session.getId());
        } else {
            // Fallback: create new attempt if none found
            PaymentAttempt attempt = new PaymentAttempt();
            attempt.setPatientId(patientId);
            attempt.setStripeSessionId(session.getId());
            attempt.setVisitId(visitId);
            attempt.setAmount(visitFee);
            attempt.setCurrency("usd");
            attempt.setProductName("Follow-up Visit Payment");
            attempt.setStatus(PaymentAttempt.PaymentStatus.PENDING);
            attempt.setPaymentType(PaymentAttempt.PaymentType.VISIT_FEE);
            paymentAttemptRepository.save(attempt);
            logger.info("Created new PaymentAttempt {} with Stripe session {}", 
                       attempt.getId(), session.getId());
        }

        return session;
    }

    public Session createVisitCheckoutSessionFallback(UUID patientId, Long visitId, Throwable t) {
        logger.error("Fallback for createVisitCheckoutSessionForVisit invoked for patient {} and visit {}. Error: {}", patientId, visitId, t.getMessage());
        throw new RuntimeException("Unable to create payment session at this time. Please try again later.", t);
    }

    private BigDecimal getVisitFee(Long visitId) {
        logger.info("Fetching actual fee for visit ID {}", visitId);
        
        // First, try to find existing payment attempt for this visit
        List<PaymentAttempt> existingAttempts = paymentAttemptRepository.findByVisitIdOrderByCreatedAtDesc(visitId);
        if (!existingAttempts.isEmpty()) {
            BigDecimal existingAmount = existingAttempts.get(0).getAmount();
            logger.info("Found existing payment attempt for visit {}: amount = {}", visitId, existingAmount);
            return existingAmount;
        }
        
        // Fallback: return a default amount if no existing payment attempt found
        logger.warn("No existing payment attempt found for visit {}. Using default amount.", visitId);
        return new BigDecimal("50.00"); // Default consultation fee
    }

    @Transactional(readOnly = true)
    public List<PaymentAttemptResponseDTO> getPaymentAttemptsByPatientId(UUID patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
        List<PaymentAttempt> attempts = paymentAttemptRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
        return attempts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private PaymentAttemptResponseDTO convertToDTO(PaymentAttempt attempt) {
        return new PaymentAttemptResponseDTO(
                attempt.getId(),
                attempt.getPatientId(),
                attempt.getStripeSessionId(),
                attempt.getAmount(),
                attempt.getCurrency(),
                attempt.getProductName(),
                attempt.getStatus().name(),
                attempt.getPaymentType().name(),
                attempt.getVisitId(),
                attempt.getCreatedAt(),
                attempt.getUpdatedAt()
        );
    }
} 