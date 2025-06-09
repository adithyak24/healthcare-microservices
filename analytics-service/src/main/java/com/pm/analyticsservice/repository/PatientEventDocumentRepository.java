package com.pm.analyticsservice.repository;

import com.pm.analyticsservice.document.PatientEventDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PatientEventDocumentRepository extends ElasticsearchRepository<PatientEventDocument, String> {
    // String is the type of the @Id field in PatientEventDocument
    // You can add custom query methods here if needed, for example:
    // List<PatientEventDocument> findByPatientId(UUID patientId);
    // List<PatientEventDocument> findByEventType(String eventType);
} 