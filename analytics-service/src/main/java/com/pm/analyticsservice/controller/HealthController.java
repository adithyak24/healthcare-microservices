package com.pm.analyticsservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.pm.analyticsservice.repository.PatientEventDocumentRepository;
import com.pm.analyticsservice.document.PatientEventDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @Autowired
    private PatientEventDocumentRepository patientEventDocumentRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("service", "analytics-service");
        health.put("status", "UP");

        // Check Elasticsearch connectivity
        try {
            boolean isElasticsearchUp = elasticsearchOperations.indexOps(PatientEventDocument.class).exists();
            health.put("elasticsearch", Map.of(
                "status", isElasticsearchUp ? "UP" : "DOWN",
                "details", "Connection test successful"
            ));
        } catch (Exception e) {
            logger.error("Elasticsearch health check failed", e);
            health.put("elasticsearch", Map.of(
                "status", "DOWN",
                "details", e.getMessage()
            ));
            health.put("status", "DOWN");
        }

        // Check if we can count documents
        try {
            long count = patientEventDocumentRepository.count();
            health.put("documents_count", count);
        } catch (Exception e) {
            logger.error("Failed to count documents in Elasticsearch", e);
            health.put("documents_count", "ERROR: " + e.getMessage());
        }

        return ResponseEntity.ok(health);
    }

    @GetMapping("/test-elasticsearch")
    public ResponseEntity<Map<String, Object>> testElasticsearch() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Test if index exists
            boolean indexExists = elasticsearchOperations.indexOps(PatientEventDocument.class).exists();
            result.put("index_exists", indexExists);
            
            // Test document count
            long count = patientEventDocumentRepository.count();
            result.put("document_count", count);
            
            result.put("status", "SUCCESS");
            logger.info("Elasticsearch test successful. Index exists: {}, Document count: {}", indexExists, count);
            
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            logger.error("Elasticsearch test failed", e);
        }
        
        return ResponseEntity.ok(result);
    }
} 