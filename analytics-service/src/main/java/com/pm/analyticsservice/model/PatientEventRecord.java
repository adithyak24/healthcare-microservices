package com.pm.analyticsservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "patient_event_records")
public class PatientEventRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Or GenerationType.IDENTITY if preferred for UUIDs with DB support
    private UUID recordId; // Primary key for this analytics record

    private String eventType; // e.g., PATIENT_CREATED, PATIENT_UPDATED

    @Column(nullable = false)
    private UUID patientId; // The ID of the patient from patient-service

    private String name;
    private String email;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDate registeredDate;
    private String problem;
    private String location;

    @Column(precision = 10, scale = 2)
    private BigDecimal consultationFee;

    @Column(nullable = false)
    private LocalDateTime eventTimestamp; // Timestamp from the event DTO

    private LocalDateTime ingestedTimestamp; // Timestamp when analytics-service processed it

    public PatientEventRecord() {
        this.ingestedTimestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getRecordId() {
        return recordId;
    }

    public void setRecordId(UUID recordId) {
        this.recordId = recordId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDate getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(LocalDate registeredDate) {
        this.registeredDate = registeredDate;
    }

    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(LocalDateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public LocalDateTime getIngestedTimestamp() {
        return ingestedTimestamp;
    }

    public void setIngestedTimestamp(LocalDateTime ingestedTimestamp) {
        this.ingestedTimestamp = ingestedTimestamp;
    }
} 