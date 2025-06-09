package com.pm.patientservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class VisitAppointmentRequestDTO {

    @NotBlank(message = "Doctor name is required")
    private String doctorName;

    @NotNull(message = "Appointment date and time are required")
    @Future(message = "Appointment date and time must be in the future")
    private LocalDateTime appointmentDateTime;

    // Getters and Setters
    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }
} 