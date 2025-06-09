package com.pm.patientservice.web;

import com.pm.patientservice.services.PaymentNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
public class PaymentNotificationController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentNotificationController.class);
    private final PaymentNotificationService paymentNotificationService;

    @Autowired
    public PaymentNotificationController(PaymentNotificationService paymentNotificationService) {
        this.paymentNotificationService = paymentNotificationService;
    }

    @GetMapping("/me/payment-notifications")
    public ResponseEntity<?> getMyPaymentNotifications(Authentication authentication) {
        try {
            UUID patientId = UUID.fromString(authentication.getName());
            PaymentNotificationService.PaymentNotification notification = 
                paymentNotificationService.consumeNotificationForPatient(patientId);
            
            if (notification != null) {
                logger.info("Retrieved payment notification for patient ID: {}", patientId);
                return ResponseEntity.ok(notification);
            } else {
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving payment notifications: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error retrieving notifications");
        }
    }
} 