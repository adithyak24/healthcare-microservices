package com.pm.patientservice.services;

import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.dto.PatientLoginRequestDTO;
import com.pm.patientservice.dto.PatientLoginResponseDTO;
import com.pm.patientservice.dto.AppointmentRequestDTO;
import com.pm.patientservice.dto.PatientVisitDTO;
import com.pm.patientservice.dto.VisitAppointmentRequestDTO;
import com.pm.patientservice.exception.PatientNotFoundException;
import com.pm.patientservice.exception.VisitNotFoundException;
import com.pm.patientservice.grpc.client.BillingGrpcClient;
import com.pm.patientservice.kafka.KafkaProducer;
import com.pm.patientservice.kafka.dto.PatientEventDTO;
import com.pm.patientservice.kafka.dto.PatientRegisteredWithCredentialsEvent;
import com.pm.patientservice.kafka.dto.VisitFeeChargeRequestedEvent;

import com.pm.patientservice.mapper.PatientMapper;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.model.PatientVisit;
import com.pm.patientservice.repository.PatientRepository;
import com.pm.patientservice.repository.PatientVisitRepository;
import com.pm.patientservice.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.time.format.DateTimeParseException;
import com.pm.patientservice.exception.EmailAlreadyExistsException;
import com.pm.patientservice.service.BloomFilterService;

@Service
@CacheConfig(cacheNames = "patients")
public class PatientServiceImpl implements PatientService {

  private static final Logger logger = LoggerFactory.getLogger(PatientServiceImpl.class);
  private final PatientRepository patientRepository;
  private final PatientVisitRepository patientVisitRepository;
  private final BillingGrpcClient billingGrpcClient;
  private final KafkaProducer kafkaProducer;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final AuthenticationManager authenticationManager;
  private final BloomFilterService bloomFilterService;
  private final PaymentNotificationService paymentNotificationService;
  private static final SecureRandom secureRandom = new SecureRandom();
  private static final int PASSWORD_LENGTH = 8;

  @Autowired @Lazy
  private PatientService self;

  @Autowired
  public PatientServiceImpl(PatientRepository patientRepository, 
                          PatientVisitRepository patientVisitRepository,
                          BillingGrpcClient billingGrpcClient,
                          KafkaProducer kafkaProducer, 
                          PasswordEncoder passwordEncoder, 
                          JwtTokenProvider jwtTokenProvider,
                          AuthenticationManager authenticationManager,
                          BloomFilterService bloomFilterService,
                          PaymentNotificationService paymentNotificationService) {
    this.patientRepository = patientRepository;
    this.patientVisitRepository = patientVisitRepository;
    this.billingGrpcClient = billingGrpcClient;
    this.kafkaProducer = kafkaProducer;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenProvider = jwtTokenProvider;
    this.authenticationManager = authenticationManager;
    this.bloomFilterService = bloomFilterService;
    this.paymentNotificationService = paymentNotificationService;
  }

  private String generateRandomPassword() {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
    for (int i = 0; i < PASSWORD_LENGTH; i++) {
        int randomIndex = secureRandom.nextInt(chars.length());
        sb.append(chars.charAt(randomIndex));
    }
    return sb.toString();
  }

  @Override
  @Transactional
  @Caching(evict = {
      @CacheEvict(value = "patients", allEntries = true),
      @CacheEvict(value = "recentPatients", allEntries = true),
      @CacheEvict(value = "patientDetailsByEmail", allEntries = true)
  })
  public PatientResponseDTO savePatient(PatientRequestDTO patientRequestDTO) {
    logger.info("Saving new patient: {}", patientRequestDTO.getEmail());
    
    // First check with Bloom filter for quick duplicate detection
    if (bloomFilterService.mightContainEmail(patientRequestDTO.getEmail())) {
      logger.debug("Bloom filter indicates email might already exist, checking database: {}", patientRequestDTO.getEmail());
      
      // If bloom filter says might exist, verify with database
      if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
        logger.warn("Attempted to create patient with duplicate email: {}", patientRequestDTO.getEmail());
        throw new EmailAlreadyExistsException("A patient with email '" + patientRequestDTO.getEmail() + "' already exists");
      }
    }
    
    Patient patient = PatientMapper.toModel(patientRequestDTO);

    String plainPassword = generateRandomPassword();
    patient.setPortalPassword(passwordEncoder.encode(plainPassword));
    patient.setRegisteredDate(LocalDate.now());
    patient.setConsultationPaymentStatus(Patient.ConsultationPaymentStatus.NOT_PAID);
    logger.info("Generated and hashed portal password for patient: {}", patientRequestDTO.getEmail());
    
    if (patientRequestDTO.getProblem() != null && !patientRequestDTO.getProblem().isEmpty()) {
        PatientVisit initialVisit = new PatientVisit();
        initialVisit.setVisitDate(patient.getRegisteredDate());
        initialVisit.setProblemDescription(patientRequestDTO.getProblem());
        initialVisit.setPatient(patient);
        initialVisit.setNotes("Initial consultation.");
        if (patientRequestDTO.getConsultationFee() != null && !patientRequestDTO.getConsultationFee().trim().isEmpty()) {
            try {
                initialVisit.setConsultationFee(new BigDecimal(patientRequestDTO.getConsultationFee()));
            } catch (NumberFormatException e) {
                logger.warn("Invalid consultation fee format for initial visit: {}. Setting to ZERO.", patientRequestDTO.getConsultationFee());
                initialVisit.setConsultationFee(BigDecimal.ZERO);
            }
        } else {
            initialVisit.setConsultationFee(BigDecimal.ZERO);
        }
        initialVisit.setVisitPaymentStatus(Patient.ConsultationPaymentStatus.NOT_PAID);
        patient.getVisits().add(initialVisit);
    }

    try {
        Patient savedPatient = patientRepository.save(patient);
        logger.info("Patient saved with ID: {}", savedPatient.getId());
        
        // Add email to bloom filter after successful save
        bloomFilterService.addEmail(savedPatient.getEmail());
        logger.debug("Added email to bloom filter: {}", savedPatient.getEmail());

        // Send patient event to analytics service via Kafka
        kafkaProducer.sendPatientEvent(savedPatient, PatientEventDTO.EventType.PATIENT_CREATED);
        logger.info("Patient analytics event sent to Kafka for patient ID: {}", savedPatient.getId());

        kafkaProducer.sendPatientRegisteredWithCredentialsEvent(
            new PatientRegisteredWithCredentialsEvent(
                savedPatient.getId(),
                savedPatient.getName(),
                savedPatient.getEmail(),
                plainPassword,
                savedPatient.getEmail()
            )
        );
        logger.info("Patient credentials event published for ID: {}", savedPatient.getId());

        if (!savedPatient.getVisits().isEmpty()) {
            PatientVisit initialVisit = savedPatient.getVisits().iterator().next();
            if (initialVisit.getConsultationFee() != null && initialVisit.getConsultationFee().compareTo(BigDecimal.ZERO) > 0) {
                kafkaProducer.sendVisitFeeChargeRequestedEvent(
                    new VisitFeeChargeRequestedEvent(
                        savedPatient.getId(),
                        initialVisit.getId(),
                        initialVisit.getConsultationFee()
                    )
                );
                logger.info("Visit fee charge requested event published for patient ID: {}, visit ID: {}, fee: {}",
                        savedPatient.getId(), initialVisit.getId(), initialVisit.getConsultationFee());
            }
        }

        if (savedPatient.getVisits() != null) savedPatient.getVisits().size();
        return PatientMapper.toDTO(savedPatient);
        
    } catch (org.springframework.dao.DataIntegrityViolationException e) {
        // Handle database constraint violation (in case of race condition)
        if (e.getMessage() != null && e.getMessage().contains("duplicate key")) {
            logger.warn("Database constraint violation - duplicate email detected: {}", patientRequestDTO.getEmail());
            throw new EmailAlreadyExistsException("A patient with email '" + patientRequestDTO.getEmail() + "' already exists", e);
        }
        throw e; // Re-throw if it's a different constraint violation
    }
  }

  @Override
  @Cacheable("patients")
  @Transactional(readOnly = true)
  public List<PatientResponseDTO> getAllPatients() {
    logger.debug("Fetching all patients.");
    List<Patient> patients = patientRepository.findAll();
    patients.forEach(p -> { if (p.getVisits() != null) p.getVisits().size(); });
    return patients.stream().map(PatientMapper::toDTO).collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PatientResponseDTO> getAllPatients(Pageable pageable) {
    logger.debug("Fetching patients with pagination - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
    Page<Patient> patientPage = patientRepository.findAll(pageable);
    
    // Force load visits for each patient to avoid lazy loading issues
    patientPage.getContent().forEach(p -> { 
      if (p.getVisits() != null) p.getVisits().size(); 
    });
    
    return patientPage.map(PatientMapper::toDTO);
  }

  @Override
  @Cacheable("recentPatients")
  @Transactional(readOnly = true)
  public List<PatientResponseDTO> getRecentPatients() {
    logger.debug("Fetching recent patients.");
    List<Patient> patients = patientRepository.findFirst5ByOrderByRegisteredDateDesc();
    patients.forEach(p -> { if (p.getVisits() != null) p.getVisits().size(); });
    return patients.stream().map(PatientMapper::toDTO).collect(Collectors.toList());
  }

  @Override
  @CacheEvict(value = "recentPatients", allEntries = true)
  public void clearRecentPatientsCache() {
    logger.info("Recent patients cache cleared.");
  }

  @Override
  @Cacheable(value = "patient", key = "#id")
  @Transactional(readOnly = true)
  public PatientResponseDTO getPatientById(UUID id) {
    logger.debug("Fetching patient by ID: {}", id);
    Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + id));
    if (patient.getVisits() != null) patient.getVisits().size();
    return PatientMapper.toDTO(patient);
  }

  @Override
  @Cacheable(value = "patientDetailsByEmail", key = "#email")
  @Transactional(readOnly = true)
  public PatientResponseDTO getPatientDetailsByEmail(String email) {
    logger.info("Fetching full details for patient email: {}", email);
    Patient patient = patientRepository.findByEmail(email)
            .orElseThrow(() -> new PatientNotFoundException("Patient not found with email: " + email));
    if (patient.getVisits() != null) patient.getVisits().size();
    return PatientMapper.toDTO(patient);
  }

  @Override
  @Transactional
  @Caching(evict = {
      @CacheEvict(value = "patient", key = "#id"),
      @CacheEvict(value = "patients", allEntries = true),
      @CacheEvict(value = "recentPatients", allEntries = true),
      @CacheEvict(value = "patientDetailsByEmail", allEntries = true)
  })
  public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO) {
    logger.info("Updating patient ID: {}", id);
    Patient existingPatient = patientRepository.findById(id)
            .orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + id));

    // Check if email is being changed and if new email already exists
    if (!existingPatient.getEmail().equals(patientRequestDTO.getEmail())) {
        logger.debug("Email is being changed from {} to {}", existingPatient.getEmail(), patientRequestDTO.getEmail());
        
        // First check with Bloom filter
        if (bloomFilterService.mightContainEmail(patientRequestDTO.getEmail())) {
            logger.debug("Bloom filter indicates new email might already exist, checking database: {}", patientRequestDTO.getEmail());
            
            // Check if another patient already has this email
            if (patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(), id)) {
                logger.warn("Attempted to update patient {} to duplicate email: {}", id, patientRequestDTO.getEmail());
                throw new EmailAlreadyExistsException("A patient with email '" + patientRequestDTO.getEmail() + "' already exists");
            }
        }
    }

    existingPatient.setName(patientRequestDTO.getName());
    existingPatient.setEmail(patientRequestDTO.getEmail());
    existingPatient.setAddress(patientRequestDTO.getAddress());
    if (patientRequestDTO.getDateOfBirth() != null) {
        try {
            existingPatient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));
        } catch(DateTimeParseException e) {
            logger.warn("Invalid date of birth format: {} for patient ID: {}. Skipping update.", patientRequestDTO.getDateOfBirth(), id);
        }
    }
    
    try {
        Patient updatedPatient = patientRepository.save(existingPatient);
        
        // Add new email to bloom filter if it was changed
        if (!existingPatient.getEmail().equals(patientRequestDTO.getEmail())) {
            bloomFilterService.addEmail(updatedPatient.getEmail());
            logger.debug("Added updated email to bloom filter: {}", updatedPatient.getEmail());
        }
        
        // Send patient update event to analytics service
        kafkaProducer.sendPatientEvent(updatedPatient, PatientEventDTO.EventType.PATIENT_UPDATED);
        logger.info("Patient update analytics event sent to Kafka for patient ID: {}", updatedPatient.getId());
        
        if (updatedPatient.getVisits() != null) updatedPatient.getVisits().size();
        return PatientMapper.toDTO(updatedPatient);
        
    } catch (org.springframework.dao.DataIntegrityViolationException e) {
        // Handle database constraint violation (in case of race condition)
        if (e.getMessage() != null && e.getMessage().contains("duplicate key")) {
            logger.warn("Database constraint violation during update - duplicate email detected: {}", patientRequestDTO.getEmail());
            throw new EmailAlreadyExistsException("A patient with email '" + patientRequestDTO.getEmail() + "' already exists", e);
        }
        throw e; // Re-throw if it's a different constraint violation
    }
  }

  @Override
  @Transactional
  @Caching(evict = {
      @CacheEvict(value = "patient", key = "#id"),
      @CacheEvict(value = "patients", allEntries = true),
      @CacheEvict(value = "recentPatients", allEntries = true),
      @CacheEvict(value = "patientDetailsByEmail", allEntries = true)
  })
  public void deletePatient(UUID id) {
    logger.info("Deleting patient with ID: {}", id);
    if (!patientRepository.existsById(id)) {
        throw new PatientNotFoundException("Patient not found with id: " + id);
    }
    patientRepository.deleteById(id);
  }

  @Override
  @Transactional
  @Caching(evict = {
      @CacheEvict(value = "patient", key = "#patientId"),
      @CacheEvict(value = "patients", allEntries = true),
      @CacheEvict(value = "recentPatients", allEntries = true),
      @CacheEvict(value = "patientDetailsByEmail", allEntries = true)
  })
  public PatientVisitDTO addVisitToPatient(UUID patientId, PatientVisitDTO visitDto) {
    logger.debug("Attempting to add visit for patient ID: {}", patientId);
    Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + patientId));

    PatientVisit newVisit = new PatientVisit();
    newVisit.setPatient(patient);
    newVisit.setVisitDate(visitDto.getVisitDate() != null ? visitDto.getVisitDate() : LocalDate.now());
    newVisit.setProblemDescription(visitDto.getProblemDescription());
    newVisit.setNotes(visitDto.getNotes());

    if (visitDto.getConsultationFee() != null && !visitDto.getConsultationFee().trim().isEmpty()) {
        try {
            newVisit.setConsultationFee(new BigDecimal(visitDto.getConsultationFee()));
        } catch (NumberFormatException e) {
            logger.warn("Invalid consultation fee format for new visit: {}. Setting to ZERO.", visitDto.getConsultationFee());
            newVisit.setConsultationFee(BigDecimal.ZERO);
        }
    } else {
        newVisit.setConsultationFee(BigDecimal.ZERO);
    }
    newVisit.setVisitPaymentStatus(Patient.ConsultationPaymentStatus.NOT_PAID);

    newVisit.setAppointmentDoctorName(visitDto.getAppointmentDoctorName());
    if (visitDto.getAppointmentDateTime() != null && !visitDto.getAppointmentDateTime().trim().isEmpty()) {
        try {
            newVisit.setAppointmentDateTime(LocalDateTime.parse(visitDto.getAppointmentDateTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } catch (DateTimeParseException e) {
            logger.warn("Invalid appointment date/time format for new visit: {}. Setting to null.", visitDto.getAppointmentDateTime());
            newVisit.setAppointmentDateTime(null);
        }
    }

    PatientVisit savedVisit = patientVisitRepository.save(newVisit);
    logger.info("Added visit ID {} for patient ID: {}", savedVisit.getId(), patientId);
    
    // Send billing event if visit has consultation fee
    if (savedVisit.getConsultationFee() != null && savedVisit.getConsultationFee().compareTo(BigDecimal.ZERO) > 0) {
        kafkaProducer.sendVisitFeeChargeRequestedEvent(
            new VisitFeeChargeRequestedEvent(
                patientId,
                savedVisit.getId(),
                savedVisit.getConsultationFee()
            )
        );
        logger.info("Visit fee charge requested event published for patient ID: {}, visit ID: {}, fee: {}",
                patientId, savedVisit.getId(), savedVisit.getConsultationFee());
    }
    
    if (patient.getVisits() != null) patient.getVisits().size();
    return PatientMapper.toVisitDTO(savedVisit);
  }

  @Override
  @Transactional
  @Caching(evict = {
      @CacheEvict(value = "patient", key = "#patientId"),
      @CacheEvict(value = "patients", allEntries = true),
      @CacheEvict(value = "recentPatients", allEntries = true),
      @CacheEvict(value = "patientDetailsByEmail", allEntries = true)
  })
  public void updateVisitPaymentStatus(UUID patientId, Long visitId, boolean isPaid) {
    logger.info("Updating payment status for visit ID {} of patient ID {} to {}", visitId, patientId, isPaid ? "PAID" : "NOT_PAID");
    PatientVisit visit = patientVisitRepository.findById(visitId)
        .filter(v -> v.getPatient().getId().equals(patientId))
        .orElseThrow(() -> new VisitNotFoundException("Visit ID " + visitId + " not found for patient ID " + patientId));
    visit.setVisitPaymentStatus(isPaid ? Patient.ConsultationPaymentStatus.PAID : Patient.ConsultationPaymentStatus.NOT_PAID);
    patientVisitRepository.save(visit);

    // If visit is paid and has an appointment, update main appointment to be the FIRST (earliest) appointment
    if (isPaid && visit.getAppointmentDateTime() != null && visit.getAppointmentDoctorName() != null) {
        updateMainAppointmentToFirstScheduled(patientId);
    }

    // Check if all visits are paid and update patient-level consultation status
    if (isPaid) {
        updatePatientConsultationStatusBasedOnVisits(patientId);
    }

    // Notify clients of payment status change
    try {
      String message = isPaid
          ? "Your payment for visit on " + visit.getVisitDate() + " has been successfully processed."
          : "Payment status for visit on " + visit.getVisitDate() + " has been updated.";
      paymentNotificationService.notifyPaymentStatusUpdate(patientId, isPaid ? "PAID" : "NOT_PAID", "VISIT_FEE", message);
      logger.debug("Sent payment notification for visit payment status change: patient ID {}, visit ID {}", patientId, visitId);
    } catch (Exception e) {
      logger.error("Error sending payment notification for visit payment status: {}", e.getMessage());
    }
  }

  @Override
  @Transactional
  @Caching(evict = {
      @CacheEvict(value = "patient", key = "#patientId"),
      @CacheEvict(value = "patients", allEntries = true),
      @CacheEvict(value = "recentPatients", allEntries = true),
      @CacheEvict(value = "patientDetailsByEmail", allEntries = true)
  })
  public void updatePatientConsultationPaymentStatus(UUID patientId, boolean isPaid) {
    logger.info("Updating consultation payment status for patient ID {} to {}", patientId, isPaid ? "PAID" : "NOT_PAID");
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + patientId));
    
    patient.setConsultationPaymentStatus(isPaid ? Patient.ConsultationPaymentStatus.PAID : Patient.ConsultationPaymentStatus.NOT_PAID);
    patient.setLastPaymentUpdateTimestamp(java.time.LocalDateTime.now());
    patientRepository.save(patient);
    logger.info("Successfully updated consultation payment status for patient ID {} to {}", patientId, isPaid ? "PAID" : "NOT_PAID");
    
    // Notify clients of payment status change
    try {
      String message = isPaid 
          ? "Your initial consultation payment has been successfully processed. You can now schedule appointments."
          : "Initial consultation payment status updated.";
      paymentNotificationService.notifyPaymentStatusUpdate(patientId, isPaid ? "PAID" : "NOT_PAID", "INITIAL_CONSULTATION", message);
      logger.debug("Sent payment notification for consultation payment status change: patient ID {}", patientId);
    } catch (Exception e) {
      logger.error("Error sending payment notification for consultation payment status: {}", e.getMessage());
    }
  }

  @Override
  @Transactional
  @Caching(evict = {
      @CacheEvict(value = "patient", key = "#patientId"),
      @CacheEvict(value = "patients", allEntries = true),
      @CacheEvict(value = "recentPatients", allEntries = true),
      @CacheEvict(value = "patientDetailsByEmail", allEntries = true)
  })
  public PatientVisitDTO scheduleAppointmentForVisit(UUID patientId, Long visitId, VisitAppointmentRequestDTO appointmentRequestDTO) {
    logger.info("Scheduling appointment for visit ID: {} of patient ID: {}", visitId, patientId);
    PatientVisit visit = patientVisitRepository.findById(visitId)
        .filter(v -> v.getPatient().getId().equals(patientId))
        .orElseThrow(() -> new VisitNotFoundException("Visit ID " + visitId + " not found for patient ID " + patientId));

    visit.setAppointmentDoctorName(appointmentRequestDTO.getDoctorName());
    if (appointmentRequestDTO.getAppointmentDateTime() != null) {
        visit.setAppointmentDateTime(appointmentRequestDTO.getAppointmentDateTime());
    } else {
        visit.setAppointmentDateTime(null);
    }
    PatientVisit savedVisit = patientVisitRepository.save(visit);
    return PatientMapper.toVisitDTO(savedVisit);
  }

  @Override
  public PatientLoginResponseDTO loginPatient(PatientLoginRequestDTO loginRequestDTO) {
    logger.info("Attempting login for user: {}", loginRequestDTO.getEmail());
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword()));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    Patient patient = patientRepository.findByEmail(loginRequestDTO.getEmail())
            .orElseThrow(() -> {
                logger.warn("Authenticated user not found in database during token generation: {}", loginRequestDTO.getEmail());
                return new PatientNotFoundException("Patient not found after authentication: " + loginRequestDTO.getEmail());
            });

    String token = jwtTokenProvider.createToken(patient.getEmail(), patient.getId());
    logger.info("Login successful for user: {}, token generated.", loginRequestDTO.getEmail());

    String formattedDateTime = null;
    if (patient.getAppointmentDateTime() != null) {
        try {
            formattedDateTime = patient.getAppointmentDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            logger.warn("Could not format appointmentDateTime for patient {}: {}", patient.getEmail(), e.getMessage());
        }
    }

    return new PatientLoginResponseDTO(
        true,                                  // success
        "Login successful",                    // message
        token,                                 // token
        patient.getId(),                       // patientId (UUID type)
        patient.getName(),                     // name
        patient.getEmail(),                    // email
        patient.getAppointmentDoctorName(),    // appointmentDoctorName
        formattedDateTime                      // appointmentDateTime (String)
    );
  }

  private void updatePatientConsultationStatusBasedOnVisits(UUID patientId) {
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + patientId));
    
    // Check if patient has any visits
    List<PatientVisit> visits = patientVisitRepository.findByPatientIdOrderByVisitDateDesc(patientId);
    if (visits.isEmpty()) {
        return; // No visits, don't change patient-level status
    }
    
    // Only check if the LAST (most recent) visit with a fee is paid
    PatientVisit lastVisitWithFee = visits.stream()
        .filter(visit -> visit.getConsultationFee() != null && visit.getConsultationFee().compareTo(BigDecimal.ZERO) > 0)
        .findFirst() // First in desc order = most recent
        .orElse(null);
    
    if (lastVisitWithFee != null && lastVisitWithFee.getVisitPaymentStatus() == Patient.ConsultationPaymentStatus.PAID) {
        patient.setConsultationPaymentStatus(Patient.ConsultationPaymentStatus.PAID);
        patient.setLastPaymentUpdateTimestamp(java.time.LocalDateTime.now());
        patientRepository.save(patient);
        logger.info("Updated patient-level consultation status to PAID for patient ID {} (last visit is paid)", patientId);
    }
  }

  private void updateMainAppointmentToFirstScheduled(UUID patientId) {
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + patientId));
    
    // Get all visits with appointments, ordered by appointment date (earliest first)
    List<PatientVisit> visitsWithAppointments = patientVisitRepository.findByPatientIdOrderByVisitDateDesc(patientId)
        .stream()
        .filter(visit -> visit.getAppointmentDateTime() != null && visit.getAppointmentDoctorName() != null)
        .filter(visit -> visit.getVisitPaymentStatus() == Patient.ConsultationPaymentStatus.PAID) // Only paid visits
        .sorted((v1, v2) -> v1.getAppointmentDateTime().compareTo(v2.getAppointmentDateTime())) // Sort by appointment time
        .toList();
    
    if (!visitsWithAppointments.isEmpty()) {
        // Set main appointment to the FIRST (earliest) scheduled appointment from paid visits
        PatientVisit firstAppointment = visitsWithAppointments.get(0);
        patient.setAppointmentDateTime(firstAppointment.getAppointmentDateTime());
        patient.setAppointmentDoctorName(firstAppointment.getAppointmentDoctorName());
        patientRepository.save(patient);
        logger.info("Updated main appointment for patient ID {} to first scheduled appointment: {} with Dr. {}", 
                   patientId, firstAppointment.getAppointmentDateTime(), firstAppointment.getAppointmentDoctorName());
    } else {
        // No paid visits with appointments, clear main appointment
        if (patient.getAppointmentDateTime() != null) {
            patient.setAppointmentDateTime(null);
            patient.setAppointmentDoctorName(null);
            patientRepository.save(patient);
            logger.info("Cleared main appointment for patient ID {} (no paid visits with appointments)", patientId);
        }
    }
  }

  @Override
  @Transactional
  @Caching(evict = {
      @CacheEvict(value = "patient", key = "#patientId"),
      @CacheEvict(value = "patients", allEntries = true),
      @CacheEvict(value = "recentPatients", allEntries = true),
      @CacheEvict(value = "patientDetailsByEmail", allEntries = true)
  })
  public PatientResponseDTO scheduleAppointment(UUID patientId, AppointmentRequestDTO appointmentRequestDTO) {
    logger.info("Scheduling main appointment for patient ID: {}", patientId);
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + patientId));

    patient.setAppointmentDoctorName(appointmentRequestDTO.getDoctorName());
    if (appointmentRequestDTO.getAppointmentDateTime() != null) {
        patient.setAppointmentDateTime(appointmentRequestDTO.getAppointmentDateTime());
    } else {
        patient.setAppointmentDateTime(null);
    }
    Patient savedPatient = patientRepository.save(patient);
    if (savedPatient.getVisits() != null) savedPatient.getVisits().size();
    return PatientMapper.toDTO(savedPatient);
  }
} 