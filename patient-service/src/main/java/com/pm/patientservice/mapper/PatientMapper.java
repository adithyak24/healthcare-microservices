package com.pm.patientservice.mapper;

import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.dto.PatientVisitDTO;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.model.PatientVisit;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.stream.Collectors;

public class PatientMapper {

  private static final DecimalFormat feeFormatter = new DecimalFormat("0.00");

  // Mapper for PatientVisit to PatientVisitDTO
  public static PatientVisitDTO toVisitDTO(PatientVisit visit) {
    if (visit == null) {
      return null;
    }
    PatientVisitDTO dto = new PatientVisitDTO();
    dto.setId(visit.getId());
    dto.setVisitDate(visit.getVisitDate());
    dto.setProblemDescription(visit.getProblemDescription());
    dto.setNotes(visit.getNotes());

    // Map new fields
    if (visit.getConsultationFee() != null) {
      dto.setConsultationFee(feeFormatter.format(visit.getConsultationFee()));
    } else {
      dto.setConsultationFee(null); // Or "0.00" if preferred for display
    }

    if (visit.getVisitPaymentStatus() != null) {
      dto.setVisitPaymentStatus(visit.getVisitPaymentStatus().name());
    } else {
      // Should default in entity, but good to handle if it can be null by any chance
      dto.setVisitPaymentStatus(Patient.ConsultationPaymentStatus.NOT_PAID.name()); 
    }

    dto.setAppointmentDoctorName(visit.getAppointmentDoctorName());
    if (visit.getAppointmentDateTime() != null) {
      dto.setAppointmentDateTime(visit.getAppointmentDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    } else {
      dto.setAppointmentDateTime(null);
    }

    return dto;
  }

  public static PatientResponseDTO toDTO(Patient patient) {
    if (patient == null) {
        return null;
    }
    PatientResponseDTO patientDTO = new PatientResponseDTO();
    patientDTO.setId(patient.getId());
    patientDTO.setName(patient.getName());
    patientDTO.setAddress(patient.getAddress());
    patientDTO.setEmail(patient.getEmail());
    patientDTO.setDateOfBirth(patient.getDateOfBirth().format(DateTimeFormatter.ISO_LOCAL_DATE));
    patientDTO.setRegisteredDate(patient.getRegisteredDate().format(DateTimeFormatter.ISO_LOCAL_DATE));

    patientDTO.setProblem(patient.getProblem()); // Initial problem
    patientDTO.setLocation(patient.getLocation());
    if (patient.getConsultationFee() != null) {
        DecimalFormat df = new DecimalFormat("0.00");
        patientDTO.setConsultationFee(df.format(patient.getConsultationFee()));
    } else {
        patientDTO.setConsultationFee(null);
    }

    if (patient.getConsultationPaymentStatus() != null) {
        patientDTO.setConsultationPaymentStatus(patient.getConsultationPaymentStatus().name());
    } else {
        patientDTO.setConsultationPaymentStatus(Patient.ConsultationPaymentStatus.NOT_PAID.name());
    }

    patientDTO.setAppointmentDoctorName(patient.getAppointmentDoctorName());
    if (patient.getAppointmentDateTime() != null) {
        patientDTO.setAppointmentDateTime(patient.getAppointmentDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    } else {
        patientDTO.setAppointmentDateTime(null);
    }

    // Map the list of visits
    if (patient.getVisits() != null && !patient.getVisits().isEmpty()) {
        patientDTO.setVisits(patient.getVisits().stream()
                                .map(PatientMapper::toVisitDTO)
                                .collect(Collectors.toList()));
    } else {
        patientDTO.setVisits(Collections.emptyList());
    }

    return patientDTO;
  }

  public static Patient toModel(PatientRequestDTO patientRequestDTO) {
    if (patientRequestDTO == null) {
        return null;
    }
    Patient patient = new Patient();
    patient.setName(patientRequestDTO.getName());
    patient.setAddress(patientRequestDTO.getAddress());
    patient.setEmail(patientRequestDTO.getEmail());
    // Assuming dates are always provided in correct ISO format from frontend
    patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth())); 
    patient.setRegisteredDate(LocalDate.parse(patientRequestDTO.getRegisteredDate()));
    
    patient.setProblem(patientRequestDTO.getProblem());
    patient.setLocation(patientRequestDTO.getLocation());
    if (patientRequestDTO.getConsultationFee() != null && !patientRequestDTO.getConsultationFee().isEmpty()) {
        try {
            patient.setConsultationFee(new BigDecimal(patientRequestDTO.getConsultationFee()));
        } catch (NumberFormatException e) {
            System.err.println("Error parsing consultationFee: " + patientRequestDTO.getConsultationFee());
            patient.setConsultationFee(null); 
        }
    }
    // Note: Patient visits are not typically mapped from a general PatientRequestDTO.
    // Adding visits would be a separate operation/DTO.
    return patient;
  }
}
