package com.pm.patientservice.repository;

import com.pm.patientservice.model.Patient;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
  boolean existsByEmail(String email);
  boolean existsByEmailAndIdNot(String email, UUID id);

  List<Patient> findFirst5ByOrderByRegisteredDateDesc();

  java.util.Optional<Patient> findByEmail(String email);
  
  @Query("SELECT p.email FROM Patient p")
  List<String> findAllEmails();
}
