package com.pm.billingservice.repository;

import com.pm.billingservice.model.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {
    Optional<PaymentAttempt> findByStripeSessionId(String stripeSessionId);
    List<PaymentAttempt> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    Optional<PaymentAttempt> findByPatientIdAndVisitIdAndPaymentTypeAndStatus(
            UUID patientId,
            Long visitId,
            PaymentAttempt.PaymentType paymentType,
            PaymentAttempt.PaymentStatus status
    );
    
    List<PaymentAttempt> findByVisitIdOrderByCreatedAtDesc(Long visitId);
} 