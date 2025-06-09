package com.pm.patientservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@Service
public class PaymentNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentNotificationService.class);
    private final Map<UUID, PaymentNotification> notifications = new ConcurrentHashMap<>();

    public static class PaymentNotification {
        private String status;
        private String paymentType;
        private String message;
        private long timestamp;

        public PaymentNotification(String status, String paymentType, String message) {
            this.status = status;
            this.paymentType = paymentType;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters
        public String getStatus() { return status; }
        public String getPaymentType() { return paymentType; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }

    public void notifyPaymentStatusUpdate(UUID patientId, String status, String paymentType, String message) {
        logger.info("Notifying payment status update for patient ID: {}, status: {}, type: {}", 
                   patientId, status, paymentType);
        
        PaymentNotification notification = new PaymentNotification(status, paymentType, message);
        notifications.put(patientId, notification);
        
        // Clean up old notifications (older than 30 seconds)
        cleanupOldNotifications();
    }

    public PaymentNotification getNotificationForPatient(UUID patientId) {
        return notifications.get(patientId);
    }

    public PaymentNotification consumeNotificationForPatient(UUID patientId) {
        return notifications.remove(patientId);
    }

    private void cleanupOldNotifications() {
        long thirtySecondsAgo = System.currentTimeMillis() - 30000;
        notifications.entrySet().removeIf(entry -> entry.getValue().getTimestamp() < thirtySecondsAgo);
    }
} 