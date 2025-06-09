package com.pm.patientservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class PatientResponseDTO {
  private UUID id;
  private String name;
  private String email;
  private String address;
  private String dateOfBirth;
  private String registeredDate;

  // New fields
  private String problem;
  private String location;
  private String consultationFee; // Will be formatted as String from BigDecimal

  // Added for receptionist portal features
  private String consultationPaymentStatus;
  private String appointmentDoctorName;
  private String appointmentDateTime; // Consider formatting as ISO string

  private List<PatientVisitDTO> visits; // Added list of visits

  // Lombok will handle getters and setters, so manual ones are removed for brevity.
}
