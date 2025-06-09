package com.pm.patientservice.kafka.dto;

import java.util.UUID;

public class PatientRegisteredWithCredentialsEvent {

    private UUID patientId;
    private String patientName;
    private String username; // email or phone
    private String password; // generated plain text password
    private String email; // To send notification to

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
        return "PatientRegisteredWithCredentialsEvent{" +
                "patientId=" + patientId +
                ", patientName='" + patientName + "'" +
                ", username='" + username + "'" +
                ", password='" + "[SENSITIVE]" + "'" + // Avoid logging password
                ", email='" + email + "'" +
                '}';
    }
} 