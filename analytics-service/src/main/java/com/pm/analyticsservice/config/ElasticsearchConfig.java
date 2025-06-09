package com.pm.analyticsservice.config;

import com.pm.analyticsservice.document.PatientEventDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

import jakarta.annotation.PostConstruct;

@Configuration
public class ElasticsearchConfig {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchConfig.class);

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @PostConstruct
    public void initializeElasticsearch() {
        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(PatientEventDocument.class);
            
            // Create index if it doesn't exist
            if (!indexOps.exists()) {
                indexOps.create();
                logger.info("Created Elasticsearch index for PatientEventDocument");
            } else {
                logger.info("Elasticsearch index for PatientEventDocument already exists");
            }
            
            // Put mapping
            indexOps.putMapping();
            logger.info("Updated Elasticsearch mapping for PatientEventDocument");
            
            // Set refresh policy for immediate visibility (development only)
            // In production, you might want to use "wait_for" or default refresh interval
            logger.info("Elasticsearch index initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize Elasticsearch index: {}", e.getMessage(), e);
        }
    }
} 