package com.pm.patientservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseIndexConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseIndexConfig.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void createDatabaseIndexes() {
        logger.info("Creating database indexes for performance optimization...");
        
        try {
            // Create email index for fast lookups and unique constraint enforcement
            createIndexIfNotExists("idx_patients_email", "patients", "email");
            
            // Create registration date index for sorting and filtering
            createIndexIfNotExists("idx_patients_registered_date", "patients", "registered_date");
            
            // Create payment status index for filtering paid/unpaid patients
            createIndexIfNotExists("idx_patients_payment_status", "patients", "consultation_payment_status");
            
            // Create appointment date index for scheduling queries
            createIndexIfNotExists("idx_patients_appointment_date", "patients", "appointment_date_time");
            
            // Patient visits indexes
            createIndexIfNotExists("idx_visits_patient_id", "patient_visits", "patient_id");
            createIndexIfNotExists("idx_visits_date", "patient_visits", "visit_date");
            createIndexIfNotExists("idx_visits_payment_status", "patient_visits", "visit_payment_status");
            createIndexIfNotExists("idx_visits_appointment_date", "patient_visits", "appointment_date_time");
            
            logger.info("Database indexes created successfully!");
            
        } catch (Exception e) {
            logger.error("Error creating database indexes: {}", e.getMessage(), e);
        }
    }

    private void createIndexIfNotExists(String indexName, String tableName, String columnName) {
        try {
            // Check if index already exists
            String checkIndexQuery = "SELECT 1 FROM pg_indexes WHERE tablename = ? AND indexname = ?";
            Integer exists = jdbcTemplate.queryForObject(checkIndexQuery, Integer.class, tableName, indexName);
            
            if (exists == null) {
                // Create index if it doesn't exist
                String createIndexQuery = String.format("CREATE INDEX CONCURRENTLY IF NOT EXISTS %s ON %s(%s)", 
                                                      indexName, tableName, columnName);
                jdbcTemplate.execute(createIndexQuery);
                logger.info("Created index: {} on {}.{}", indexName, tableName, columnName);
            } else {
                logger.debug("Index {} already exists on {}.{}", indexName, tableName, columnName);
            }
            
        } catch (Exception e) {
            // Ignore errors for concurrent index creation or if table doesn't exist yet
            logger.debug("Could not create index {} on {}.{}: {}", indexName, tableName, columnName, e.getMessage());
        }
    }
} 