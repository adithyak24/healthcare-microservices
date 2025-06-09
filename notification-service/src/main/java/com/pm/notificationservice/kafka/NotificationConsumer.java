package com.pm.notificationservice.kafka;

import com.pm.notificationservice.kafka.dto.PatientRegisteredWithCredentialsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);
    private static final String TOPIC_PATIENT_CREDENTIALS_NOTIFICATIONS = "patient-credentials-notifications";

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String mailFromAddress;

    @Autowired
    public NotificationConsumer(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @KafkaListener(topics = TOPIC_PATIENT_CREDENTIALS_NOTIFICATIONS, groupId = "${spring.kafka.consumer.group-id}")
    public void consumePatientCredentialsEvent(@Payload PatientRegisteredWithCredentialsEvent event) {
        logger.info("Received PatientRegisteredWithCredentialsEvent for patient: {} (ID: {}) to email: {}", 
            event.getPatientName(), event.getPatientId(), event.getEmail());

        String subject = "Your Patient Portal Credentials & Payment Information";
        String body = String.format(
                "Hello %s,\n\n" +
                "Welcome to our patient portal!\n" +
                "You can log in with the following credentials:\n" +
                "Username: %s\n" +
                "Password: %s\n\n" +
                "To view your details and pay any outstanding bills, please log in at: http://localhost:3000/login\n\n" +
                "Please keep your password secure.\n\n" +
                "Regards,\nThe Hospital Team",
                event.getPatientName(),
                event.getUsername(),
                event.getPassword() // Plain text password from the event
        );

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFromAddress);
            message.setTo(event.getEmail());
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            logger.info("Successfully sent portal credentials email to: {}", event.getEmail());

        } catch (MailException e) {
            logger.error("Failed to send portal credentials email to {}: {}", event.getEmail(), e.getMessage(), e);
            // Optionally, implement retry logic or send to a dead-letter queue for mail failures
        } catch (Exception e) {
            logger.error("An unexpected error occurred while trying to send email to {}: {}", event.getEmail(), e.getMessage(), e);
        }
    }
} 