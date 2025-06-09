package com.pm.patientservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "patients", indexes = {
    @Index(name = "idx_patients_email", columnList = "email")
})
@Getter
@Setter
public class Patient {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @NotNull
  private String name;

  @NotNull
  @Email
  @Column(unique = true)
  private String email;

  @NotNull
  private String address;

  @NotNull
  private LocalDate dateOfBirth;

  @NotNull
  private LocalDate registeredDate;

  private String problem;
  private String location;
  
  @Column(precision = 10, scale = 2)
  private BigDecimal consultationFee;

  private String portalPassword;

  // Enum for payment status
  public enum ConsultationPaymentStatus {
    NOT_PAID,       // Default status, or if a previous payment failed and needs retry
    PAYMENT_PENDING,// Payment process initiated by user, waiting for Stripe confirmation
    PAID,           // Payment successfully completed
    PAYMENT_FAILED  // Payment explicitly failed on Stripe's end
  }

  @Enumerated(EnumType.STRING)
  @Column(name = "consultation_payment_status")
  private ConsultationPaymentStatus consultationPaymentStatus = ConsultationPaymentStatus.NOT_PAID; // Default value

  @Column(name = "last_payment_update_timestamp")
  private LocalDateTime lastPaymentUpdateTimestamp;

  // Fields for Appointment Scheduling by Receptionist
  @Column(name = "appointment_doctor_name")
  private String appointmentDoctorName;

  @Column(name = "appointment_date_time")
  private LocalDateTime appointmentDateTime;

  @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<PatientVisit> visits = new ArrayList<>();

  @PrePersist
  public void onPrePersist() {
    if (this.id == null) { // Generate UUID if not set (though GenerationType.AUTO should handle it for UUIDs if provider supports it)
        this.id = UUID.randomUUID();
    }
    if (this.registeredDate == null) {
      this.registeredDate = LocalDate.now();
    }
    this.lastPaymentUpdateTimestamp = LocalDateTime.now();
  }

  @PreUpdate
  public void onPreUpdate() {
    this.lastPaymentUpdateTimestamp = LocalDateTime.now();
  }
}