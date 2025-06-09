package com.pm.patientservice.repository;

import com.pm.patientservice.model.PatientVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PatientVisitRepository extends JpaRepository<PatientVisit, Long> {
    // Custom query methods can be added here if needed, for example:
    List<PatientVisit> findByPatientId(UUID patientId);
    List<PatientVisit> findByPatientIdOrderByVisitDateDesc(UUID patientId);
} 