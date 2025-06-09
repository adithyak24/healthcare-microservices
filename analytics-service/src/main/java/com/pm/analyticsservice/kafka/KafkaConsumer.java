package com.pm.analyticsservice.kafka;

import com.pm.analyticsservice.document.PatientEventDocument;
import com.pm.analyticsservice.kafka.dto.PatientEventDTO;
import com.pm.analyticsservice.model.PatientEventRecord;
import com.pm.analyticsservice.repository.PatientEventDocumentRepository;
import com.pm.analyticsservice.repository.PatientEventRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    @Autowired
    private PatientEventRecordRepository patientEventRecordRepository;

    @Autowired
    private PatientEventDocumentRepository patientEventDocumentRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @KafkaListener(topics = "patient-events", groupId = "analytics-group")
    public void consume(PatientEventDTO event) {
        logger.info("Received patient event: {} for patient ID: {}", event.getEventType(), event.getPatientId());

        try {
            // Save to PostgreSQL
            PatientEventRecord record = createPatientEventRecord(event);
            PatientEventRecord savedRecord = patientEventRecordRepository.save(record);
            logger.debug("Saved patient event record to PostgreSQL with ID: {}", savedRecord.getRecordId());

            // Index in Elasticsearch
            PatientEventDocument document = createPatientEventDocument(event);
            PatientEventDocument savedDocument = patientEventDocumentRepository.save(document);
            logger.debug("Indexed patient event document in Elasticsearch with ID: {}", savedDocument.getId());
            
            // Force refresh to make the document immediately available for search
            try {
                elasticsearchOperations.indexOps(PatientEventDocument.class).refresh();
                logger.debug("Elasticsearch index refreshed for immediate visibility");
            } catch (Exception refreshEx) {
                logger.warn("Failed to refresh Elasticsearch index: {}", refreshEx.getMessage());
            }

            logger.info("Successfully processed and stored patient event for patient ID: {} in both PostgreSQL and Elasticsearch", event.getPatientId());

        } catch (Exception e) {
            logger.error("Error processing patient event for patient ID: {}. Event: {}. Error: {}", 
                         event.getPatientId(), event, e.getMessage(), e);
            // In production, you might want to send this to a dead letter queue
        }
    }

    private PatientEventRecord createPatientEventRecord(PatientEventDTO event) {
        PatientEventRecord record = new PatientEventRecord();
        record.setPatientId(event.getPatientId());
        record.setEventType(event.getEventType().name());
        record.setName(event.getName());
        record.setEmail(event.getEmail());
        record.setAddress(event.getAddress());
        record.setDateOfBirth(event.getDateOfBirth());
        record.setRegisteredDate(event.getRegisteredDate());
        record.setProblem(event.getProblem());
        record.setLocation(event.getLocation());
        record.setConsultationFee(event.getConsultationFee());
        record.setEventTimestamp(event.getEventTimestamp());
        return record;
    }

    private PatientEventDocument createPatientEventDocument(PatientEventDTO event) {
        PatientEventDocument document = new PatientEventDocument();
        document.setPatientId(event.getPatientId());
        document.setEventType(event.getEventType().name());
        document.setName(event.getName());
        document.setEmail(event.getEmail());
        document.setAddress(event.getAddress());
        document.setDateOfBirth(event.getDateOfBirth());
        document.setRegisteredDate(event.getRegisteredDate());
        document.setProblem(event.getProblem());
        document.setLocation(event.getLocation());
        document.setConsultationFee(event.getConsultationFee());
        document.setEventTimestamp(event.getEventTimestamp());
        return document;
    }
}
