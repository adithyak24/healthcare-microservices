package com.pm.analyticsservice.controller;

import com.pm.analyticsservice.document.PatientEventDocument;
import com.pm.analyticsservice.repository.PatientEventDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/test")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private PatientEventDocumentRepository patientEventDocumentRepository;

    @PostMapping("/create-sample-document")
    public ResponseEntity<Map<String, Object>> createSampleDocument() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Create a test document
            PatientEventDocument testDocument = new PatientEventDocument();
            testDocument.setPatientId(UUID.randomUUID());
            testDocument.setEventType("PATIENT_REGISTERED");
            testDocument.setName("Test Patient " + System.currentTimeMillis());
            testDocument.setEmail("test" + System.currentTimeMillis() + "@example.com");
            testDocument.setAddress("123 Test Street");
            testDocument.setDateOfBirth(LocalDate.of(1990, 1, 1));
            testDocument.setRegisteredDate(LocalDate.now());
            testDocument.setProblem("Test Problem");
            testDocument.setLocation("Test Location");
            testDocument.setConsultationFee(new BigDecimal("50.00"));
            testDocument.setEventTimestamp(LocalDateTime.now());

            PatientEventDocument saved = patientEventDocumentRepository.save(testDocument);
            
            response.put("status", "SUCCESS");
            response.put("message", "Test document created successfully");
            response.put("document_id", saved.getId());
            response.put("patient_id", saved.getPatientId());
            
            logger.info("Created test document in Elasticsearch with ID: {}", saved.getId());
            
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            logger.error("Failed to create test document in Elasticsearch", e);
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count-documents")
    public ResponseEntity<Map<String, Object>> countDocuments() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long count = patientEventDocumentRepository.count();
            response.put("status", "SUCCESS");
            response.put("total_documents", count);
            logger.info("Total documents in Elasticsearch: {}", count);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            logger.error("Failed to count documents in Elasticsearch", e);
        }
        
        return ResponseEntity.ok(response);
    }
} 