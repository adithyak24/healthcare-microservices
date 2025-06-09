package com.pm.patientservice.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class PatientEventDTO {

    private EventType eventType;
    private UUID patientId;
    private String name;
    private String email;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDate registeredDate;
    private String problem;
    private String location;
    private BigDecimal consultationFee;
    private LocalDateTime eventTimestamp;

    public enum EventType {
        PATIENT_CREATED,
        PATIENT_UPDATED
        // PATIENT_DELETED // If you want to send delete events too
    }

    // Constructors
    public PatientEventDTO() {
        this.eventTimestamp = LocalDateTime.now();
    }

    public PatientEventDTO(EventType eventType, UUID patientId, String name, String email, String address, 
                           LocalDate dateOfBirth, LocalDate registeredDate, String problem, String location, 
                           BigDecimal consultationFee) {
        this.eventType = eventType;
        this.patientId = patientId;
        this.name = name;
        this.email = email;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.registeredDate = registeredDate;
        this.problem = problem;
        this.location = location;
        this.consultationFee = consultationFee;
        this.eventTimestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
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
} 