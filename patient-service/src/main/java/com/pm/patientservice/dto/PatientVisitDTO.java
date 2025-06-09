package com.pm.patientservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientVisitDTO {
    private Long id;
    private LocalDate visitDate;
    private String problemDescription;
    private String notes;

    // New fields corresponding to PatientVisit entity
    private String consultationFee; // String representation of BigDecimal
    private String visitPaymentStatus; // String representation of Enum
    private String appointmentDoctorName;
    private String appointmentDateTime; // String representation of LocalDateTime (ISO format)

    // We don't include the full PatientDTO here to avoid circular dependencies in responses
    // and keep the DTO focused. The patient ID will be known from the context (e.g., request path or parent DTO).
} 