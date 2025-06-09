package com.pm.patientservice.dto;

import java.util.UUID;
import java.time.LocalDateTime; // Import for type hint if needed, though field is String

public class PatientLoginResponseDTO {
    private String token;
    private UUID patientId;
    private String name;
    private String email;
    private String message;
    private boolean success;
    private String appointmentDoctorName; // Added
    private String appointmentDateTime;   // Added

    public PatientLoginResponseDTO(boolean success, String message, String token, UUID patientId, String name, String email, String appointmentDoctorName, String appointmentDateTime) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.patientId = patientId;
        this.name = name;
        this.email = email;
        this.appointmentDoctorName = appointmentDoctorName; // Added
        this.appointmentDateTime = appointmentDateTime;     // Added
    }

    public PatientLoginResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
        // Initialize other fields to null or default if this constructor is still used extensively elsewhere
        // For simplicity, assuming the main constructor will be used when login is successful
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    // Getters and setters for new fields
    public String getAppointmentDoctorName() {
        return appointmentDoctorName;
    }

    public void setAppointmentDoctorName(String appointmentDoctorName) {
        this.appointmentDoctorName = appointmentDoctorName;
    }

    public String getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(String appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }
} 