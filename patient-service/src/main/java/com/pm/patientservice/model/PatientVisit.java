package com.pm.patientservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "patient_visits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate visitDate;

    @Column(nullable = false, length = 1000)
    private String problemDescription;

    @Column(length = 2000)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // New fields for visit-specific billing and appointment
    @Column(precision = 10, scale = 2)
    private BigDecimal consultationFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "visit_payment_status")
    private Patient.ConsultationPaymentStatus visitPaymentStatus = Patient.ConsultationPaymentStatus.NOT_PAID;

    @Column(name = "appointment_doctor_name")
    private String appointmentDoctorName;

    @Column(name = "appointment_date_time")
    private LocalDateTime appointmentDateTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatientVisit that = (PatientVisit) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PatientVisit{" +
                "id=" + id +
                ", visitDate=" + visitDate +
                ", problemDescription='" + problemDescription + '\'' +
                ", notes='" + notes + '\'' +
                // Avoid printing patient to prevent circular toString calls leading to StackOverflowError
                (patient != null ? ", patient_id=" + patient.getId() : ", patient_id=null") +
                '}';
    }
} 