package com.pm.billingservice.web;

import com.pm.billingservice.dto.PaymentAttemptResponseDTO;
import com.pm.billingservice.dto.StripeCheckoutRequestDTO;
import com.pm.billingservice.dto.StripeCheckoutResponseDTO;
import com.pm.billingservice.service.StripeService;
import com.stripe.model.checkout.Session;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/billing/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private final StripeService stripeService;
    public static final String PATIENT_ID_CLAIM = "patientId"; // Same claim name as in patient-service

    @Autowired
    public PaymentController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/initial-consultation/create-checkout-session")
    public ResponseEntity<StripeCheckoutResponseDTO> createInitialCheckoutSession(@Valid @RequestBody StripeCheckoutRequestDTO requestDTO) {
        try {
            logger.info("Received request to create Stripe checkout session for initial consultation: Patient ID {}", requestDTO.getPatientId());
            Session session = stripeService.createCheckoutSessionForInitialConsultation(requestDTO);
            StripeCheckoutResponseDTO response = new StripeCheckoutResponseDTO(session.getId(), session.getUrl());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating Stripe checkout session for initial consultation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/visit/{visitId}/create-checkout-session")
    public ResponseEntity<StripeCheckoutResponseDTO> createVisitCheckoutSession(
            @PathVariable Long visitId,
            @RequestBody StripeCheckoutRequestDTO requestDTO) {
        try {
            UUID patientId = requestDTO.getPatientId();
            if (patientId == null) {
                logger.warn("Patient ID is missing in the request body for visit payment initiation.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            logger.info("Received request to create Stripe checkout session for visit ID: {}, Patient ID: {}", visitId, patientId);
            Session session = stripeService.createCheckoutSessionForVisit(patientId, visitId);
            StripeCheckoutResponseDTO response = new StripeCheckoutResponseDTO(session.getId(), session.getUrl());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error creating Stripe checkout session for visit {}: {}", visitId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            logger.error("Error creating Stripe checkout session for visit {}: {}", visitId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/my-attempts")
    public ResponseEntity<List<PaymentAttemptResponseDTO>> getMyPaymentAttempts(@RequestParam("patientId") UUID patientId) {
        if (patientId == null) {
            logger.warn("Attempt to fetch payment attempts without patientId.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
            logger.info("Fetching payment attempts for patient ID: {}", patientId);
            List<PaymentAttemptResponseDTO> paymentAttempts = stripeService.getPaymentAttemptsByPatientId(patientId);
            return ResponseEntity.ok(paymentAttempts);
        } catch (Exception e) {
            logger.error("Error fetching payment attempts for patient {}: {}", patientId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 