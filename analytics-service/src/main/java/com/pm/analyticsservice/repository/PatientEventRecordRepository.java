package com.pm.analyticsservice.repository;

import com.pm.analyticsservice.model.PatientEventRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public interface PatientEventRecordRepository extends JpaRepository<PatientEventRecord, UUID> {

    // Example custom query for analytics: Count patients by problem
    // Note: This is a simplified example. For complex analytics, consider dedicated views or query builders.
    @Query("SELECT r.problem, COUNT(DISTINCT r.patientId) as patientCount FROM PatientEventRecord r GROUP BY r.problem ORDER BY patientCount DESC")
    List<Map<String, Object>> countPatientsByProblem();

    // Example: Count patients by location
    @Query("SELECT r.location, COUNT(DISTINCT r.patientId) as patientCount FROM PatientEventRecord r GROUP BY r.location ORDER BY patientCount DESC")
    List<Map<String, Object>> countPatientsByLocation();
    
    // You might want to add queries that consider the latest event for each patient, or events within a time window.
    // For example, to get the latest record for each patient (if you only want to count current state):
    // This is more complex and often handled by processing events to maintain a "current state" table.
} 