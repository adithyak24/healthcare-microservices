package com.pm.notificationservice.kafka.dto;

import java.util.UUID;

// This DTO should ideally be in a shared library if multiple services use it.
// For this example, we are defining it within notification-service.
public class PatientRegisteredWithCredentialsEvent {

    private UUID patientId;
    private String patientName;
    private String username; // email or phone
    private String password; // generated plain text password
    private String email;    // Email address to send notification to

    // Constructors
    public PatientRegisteredWithCredentialsEvent() {
    }

    public PatientRegisteredWithCredentialsEvent(UUID patientId, String patientName, String username, String password, String email) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // Getters and Setters
    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        // Be cautious about logging sensitive information like passwords.
        // The password field here is intended for the notification content only.
        return "PatientRegisteredWithCredentialsEvent{" +
                "patientId=" + patientId +
                ", patientName='" + patientName + "'" +
                ", username='" + username + "'" +
                ", password='" + "[SENSITIVE]" + "'" + // Mask password in logs
                ", email='" + email + "'" +
                '}';
    }
} 