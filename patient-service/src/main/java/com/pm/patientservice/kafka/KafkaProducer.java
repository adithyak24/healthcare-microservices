package com.pm.patientservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pm.patientservice.kafka.dto.PatientEventDTO;
import com.pm.patientservice.kafka.dto.PatientRegisteredWithCredentialsEvent;
import com.pm.patientservice.kafka.dto.VisitFeeChargeRequestedEvent;
import com.pm.patientservice.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);
    private static final String TOPIC_PATIENT_EVENTS = "patient-events";
    private static final String TOPIC_PATIENT_CREDENTIALS_NOTIFICATIONS = "patient-credentials-notifications";
    private static final String TOPIC_VISIT_FEE_CHARGE_REQUESTED = "visit-fee-charge-requested";

    private final KafkaTemplate<String, PatientEventDTO> patientEventKafkaTemplate;
    private final KafkaTemplate<String, PatientRegisteredWithCredentialsEvent> patientCredentialsKafkaTemplate;
    private final KafkaTemplate<String, VisitFeeChargeRequestedEvent> visitFeeKafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaProducer(KafkaTemplate<String, PatientEventDTO> patientEventKafkaTemplate,
                         KafkaTemplate<String, PatientRegisteredWithCredentialsEvent> patientCredentialsKafkaTemplate,
                         KafkaTemplate<String, VisitFeeChargeRequestedEvent> visitFeeKafkaTemplate) {
        this.patientEventKafkaTemplate = patientEventKafkaTemplate;
        this.patientCredentialsKafkaTemplate = patientCredentialsKafkaTemplate;
        this.visitFeeKafkaTemplate = visitFeeKafkaTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void sendPatientEvent(Patient patient, PatientEventDTO.EventType eventType) {
        if (patient == null) {
            logger.warn("Attempted to send event for a null patient.");
            return;
        }

        PatientEventDTO event = new PatientEventDTO(
                eventType,
                patient.getId(),
                patient.getName(),
                patient.getEmail(),
                patient.getAddress(),
                patient.getDateOfBirth(),
                patient.getRegisteredDate(),
                patient.getProblem(),
                patient.getLocation(),
                patient.getConsultationFee()
        );

        try {
            logger.info("Sending {} event to Kafka topic '{}': Patient ID {}, Event Timestamp: {}",
                event.getEventType(), TOPIC_PATIENT_EVENTS, event.getPatientId(), event.getEventTimestamp());
            patientEventKafkaTemplate.send(TOPIC_PATIENT_EVENTS, patient.getId().toString(), event);
            logger.debug("PatientEventDTO sent: {}", objectMapper.writeValueAsString(event));

        } catch (JsonProcessingException e) {
            logger.error("Error serializing PatientEventDTO for logging: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error sending patient event to Kafka: {}", e.getMessage(), e);
        }
    }

    public void sendPatientRegisteredWithCredentialsEvent(PatientRegisteredWithCredentialsEvent event) {
        if (event == null) {
            logger.warn("Attempted to send a null PatientRegisteredWithCredentialsEvent.");
            return;
        }

        try {
            logger.info("Sending PatientRegisteredWithCredentialsEvent to Kafka topic '{}': Patient ID {}, Username: {}",
                TOPIC_PATIENT_CREDENTIALS_NOTIFICATIONS, event.getPatientId(), event.getUsername());
            patientCredentialsKafkaTemplate.send(TOPIC_PATIENT_CREDENTIALS_NOTIFICATIONS, event.getPatientId().toString(), event);
            logger.debug("PatientRegisteredWithCredentialsEvent for patient ID {} sent to topic {}", event.getPatientId(), TOPIC_PATIENT_CREDENTIALS_NOTIFICATIONS);

        } catch (Exception e) {
            logger.error("Error sending PatientRegisteredWithCredentialsEvent to Kafka: {}", e.getMessage(), e);
        }
    }

    public void sendVisitFeeChargeRequestedEvent(VisitFeeChargeRequestedEvent event) {
        if (event == null) {
            logger.warn("Attempted to send a null VisitFeeChargeRequestedEvent.");
            return;
        }

        try {
            logger.info("Sending VisitFeeChargeRequestedEvent to Kafka topic '{}': Patient ID {}, Visit ID {}, Fee: {}",
                TOPIC_VISIT_FEE_CHARGE_REQUESTED, event.getPatientId(), event.getVisitId(), event.getFeeAmount());
            visitFeeKafkaTemplate.send(TOPIC_VISIT_FEE_CHARGE_REQUESTED, event.getPatientId().toString(), event);
            logger.debug("VisitFeeChargeRequestedEvent sent: {}", objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            logger.error("Error serializing VisitFeeChargeRequestedEvent for logging: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error sending VisitFeeChargeRequestedEvent to Kafka: {}", e.getMessage(), e);
        }
    }
}
