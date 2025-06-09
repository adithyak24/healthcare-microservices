package com.pm.patientservice.services;

import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.dto.PatientLoginRequestDTO;
import com.pm.patientservice.dto.PatientLoginResponseDTO;
import com.pm.patientservice.dto.AppointmentRequestDTO;
import com.pm.patientservice.dto.PatientVisitDTO;
import com.pm.patientservice.dto.VisitAppointmentRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface PatientService {

    PatientResponseDTO savePatient(PatientRequestDTO patientRequestDTO);

    List<PatientResponseDTO> getAllPatients();

    Page<PatientResponseDTO> getAllPatients(Pageable pageable);

    List<PatientResponseDTO> getRecentPatients();

    PatientResponseDTO getPatientById(UUID id);

    PatientResponseDTO getPatientDetailsByEmail(String email);

    PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO);

    void deletePatient(UUID id);

    PatientLoginResponseDTO loginPatient(PatientLoginRequestDTO loginRequestDTO);

    PatientResponseDTO scheduleAppointment(UUID patientId, AppointmentRequestDTO appointmentRequestDTO);

    PatientVisitDTO addVisitToPatient(UUID patientId, PatientVisitDTO visitDto);

    void updateVisitPaymentStatus(UUID patientId, Long visitId, boolean isPaid);

    void updatePatientConsultationPaymentStatus(UUID patientId, boolean isPaid);

    PatientVisitDTO scheduleAppointmentForVisit(UUID patientId, Long visitId, VisitAppointmentRequestDTO appointmentRequestDTO);

    void clearRecentPatientsCache();
} 