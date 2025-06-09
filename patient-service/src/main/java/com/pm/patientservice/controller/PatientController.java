package com.pm.patientservice.controller;

// Core DTOs (using wildcard for brevity, specific ones if needed can be added if issues persist)
import com.pm.patientservice.dto.*;
import com.pm.patientservice.dto.validators.CreatePatientValidationGroup;
import com.pm.patientservice.services.PatientService;
import com.pm.patientservice.exception.ResourceNotFoundException;

// Swagger/OpenAPI - Correct Imports
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

// Validation
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;

// Java & Spring General
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

// Spring Security
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Import login DTOs
import com.pm.patientservice.dto.PatientLoginRequestDTO;
import com.pm.patientservice.dto.PatientLoginResponseDTO;
import com.pm.patientservice.dto.AppointmentRequestDTO;
import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.dto.PatientVisitDTO;
import com.pm.patientservice.dto.VisitAppointmentRequestDTO;

@RestController
@RequestMapping("/patients")
@Tag(name = "Patient", description = "API for managing Patients")
public class PatientController {

  private static final Logger logger = LoggerFactory.getLogger(PatientController.class);
  private final PatientService patientService;

  public PatientController(PatientService patientService) {
    this.patientService = patientService;
  }

  @GetMapping
  @Operation(summary = "Get all Patients with pagination (potentially for internal use or admin)")
  public ResponseEntity<Page<PatientResponseDTO>> getPatients(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "registeredDate") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {
    
    Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
    Page<PatientResponseDTO> patients = patientService.getAllPatients(pageable);
    return ResponseEntity.ok().body(patients);
  }

  @GetMapping("/recent")
  @Operation(summary = "Get 5 most recent Patients")
  public ResponseEntity<List<PatientResponseDTO>> getRecentPatients() {
    List<PatientResponseDTO> patients = patientService.getRecentPatients();
    return ResponseEntity.ok().body(patients);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get Patient by ID (includes visit history)")
  public ResponseEntity<PatientResponseDTO> getPatientById(@PathVariable UUID id) {
    PatientResponseDTO patient = patientService.getPatientById(id);
    return ResponseEntity.ok().body(patient);
  }

  @PostMapping
  @Operation(summary = "Create a new Patient (initial problem becomes first visit)")
  public ResponseEntity<PatientResponseDTO> createPatient(
      @Validated({Default.class, CreatePatientValidationGroup.class})
      @RequestBody PatientRequestDTO patientRequestDTO) {
    PatientResponseDTO patientResponseDTO = patientService.savePatient(patientRequestDTO);
    // Return 201 Created for new resources
    return ResponseEntity.status(HttpStatus.CREATED).body(patientResponseDTO);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a Patient's core details (does not add visits)")
  public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable UUID id,
      @Validated({Default.class}) @RequestBody PatientRequestDTO patientRequestDTO) {
    PatientResponseDTO patientResponseDTO = patientService.updatePatient(id, patientRequestDTO);
    return ResponseEntity.ok().body(patientResponseDTO);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a Patient")
  public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
    patientService.deletePatient(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Get detailed information for the authenticated patient",
               description = "Fetches comprehensive details for the logged-in patient, including main appointment and visit history with their appointments. Requires JWT authentication.")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Successfully retrieved patient details", 
                       content = @Content(mediaType = "application/json", schema = @Schema(implementation = PatientResponseDTO.class))),
          @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is missing or invalid"),
          @ApiResponse(responseCode = "404", description = "Patient not found for the authenticated user")
  })
  @GetMapping("/me/details")
  public ResponseEntity<PatientResponseDTO> getMyDetails(@AuthenticationPrincipal Jwt jwt) {
      if (jwt == null || jwt.getSubject() == null) {
          logger.warn("Attempt to fetch current patient details without JWT or JWT subject.");
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }
      try {
          String email = jwt.getSubject(); // Subject is the email
          logger.info("Fetching details for authenticated patient email: {}", email);
          // Call a new service method that fetches by email
          PatientResponseDTO patientDetails = patientService.getPatientDetailsByEmail(email); 
          return ResponseEntity.ok(patientDetails);
      } catch (ResourceNotFoundException e) {
          logger.warn("Patient details not found for authenticated user (email from JWT: {}): {}", jwt.getSubject(), e.getMessage());
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      } catch (Exception e) {
          logger.error("Error fetching details for patient (email from JWT: {}): {}", jwt.getSubject(), e.getMessage(), e);
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }
  }

  @PostMapping("/auth/login")
  @Operation(summary = "Login a patient", description = "Authenticates a patient and returns a JWT token along with patient details including main appointment.")
  public ResponseEntity<PatientLoginResponseDTO> loginPatient(@Valid @RequestBody PatientLoginRequestDTO loginRequestDTO) {
    logger.debug("Login attempt for email: {}", loginRequestDTO.getEmail());
    PatientLoginResponseDTO response = patientService.loginPatient(loginRequestDTO);
    return ResponseEntity.ok(response);
  }
  
  // Endpoint for Receptionist Portal to get all patients (might be redundant with /patients if no specific auth diff)
  @GetMapping("/all") 
  @PreAuthorize("hasRole(\'RECEPTIONIST\') or hasRole(\'ADMIN\')") 
  @Operation(summary = "Get All Patients for Receptionist Portal with pagination (includes visit history)")
  public ResponseEntity<Page<PatientResponseDTO>> getAllPatientsForReceptionist(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "registeredDate") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {
    
    Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
    Page<PatientResponseDTO> patients = patientService.getAllPatients(pageable);
    return ResponseEntity.ok(patients);
  }

  @PutMapping("/{id}/appointment")
  @Operation(summary = "Schedule or update an appointment for a Patient")
  // @PreAuthorize("hasRole(\'RECEPTIONIST\') or hasRole(\'ADMIN\')") // Add if needed
  public ResponseEntity<PatientResponseDTO> scheduleAppointment(
      @PathVariable UUID id,
      @Validated @RequestBody AppointmentRequestDTO appointmentRequestDTO) {
    PatientResponseDTO updatedPatient = patientService.scheduleAppointment(id, appointmentRequestDTO);
    return ResponseEntity.ok(updatedPatient);
  }

  // New endpoint to add a visit to a patient
  @PostMapping("/{patientId}/visits")
  @Operation(summary = "Add a new visit/problem for an existing Patient")
  // @PreAuthorize("hasRole(\'RECEPTIONIST\') or hasRole(\'ADMIN\')") // Secure as needed
  public ResponseEntity<PatientVisitDTO> addPatientVisit(
      @PathVariable UUID patientId,
      @Valid @RequestBody PatientVisitDTO visitDto) {
    PatientVisitDTO newVisit = patientService.addVisitToPatient(patientId, visitDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(newVisit);
  }

  // New endpoint to schedule an appointment for a specific visit
  @PostMapping("/{patientId}/visits/{visitId}/schedule-appointment")
  @Operation(summary = "Schedule an appointment for a specific paid visit")
  // @PreAuthorize("hasRole(\'RECEPTIONIST\') or hasRole(\'ADMIN\')") // Secure as needed
  public ResponseEntity<PatientVisitDTO> scheduleVisitAppointment(
      @PathVariable UUID patientId,
      @PathVariable Long visitId,
      @Valid @RequestBody VisitAppointmentRequestDTO appointmentRequestDTO) {
    PatientVisitDTO updatedVisit = patientService.scheduleAppointmentForVisit(patientId, visitId, appointmentRequestDTO);
    return ResponseEntity.ok(updatedVisit);
  }
}
