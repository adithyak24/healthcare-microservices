package com.pm.patientservice.controller;

import com.pm.patientservice.grpc.client.BillingGrpcClient;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.service.BloomFilterService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/test")
public class CircuitBreakerTestController {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerTestController.class);

    @Autowired
    private BillingGrpcClient billingGrpcClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private BloomFilterService bloomFilterService;

    @GetMapping("/circuit-breaker-status")
    public ResponseEntity<Map<String, Object>> getCircuitBreakerStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            CircuitBreaker billingCircuitBreaker = circuitBreakerRegistry.circuitBreaker("billing-service");
            
            status.put("circuit_breaker_name", "billing-service");
            status.put("state", billingCircuitBreaker.getState().toString());
            status.put("failure_rate", billingCircuitBreaker.getMetrics().getFailureRate());
            status.put("buffered_calls", billingCircuitBreaker.getMetrics().getNumberOfBufferedCalls());
            status.put("successful_calls", billingCircuitBreaker.getMetrics().getNumberOfSuccessfulCalls());
            status.put("failed_calls", billingCircuitBreaker.getMetrics().getNumberOfFailedCalls());
            status.put("not_permitted_calls", billingCircuitBreaker.getMetrics().getNumberOfNotPermittedCalls());
            
            logger.info("Circuit breaker status retrieved: {}", status);
            
        } catch (Exception e) {
            status.put("error", e.getMessage());
            logger.error("Failed to get circuit breaker status", e);
        }
        
        return ResponseEntity.ok(status);
    }

    @PostMapping("/test-billing-circuit-breaker")
    public ResponseEntity<Map<String, Object>> testBillingCircuitBreaker() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Create a test patient
            Patient testPatient = new Patient();
            testPatient.setId(UUID.randomUUID());
            testPatient.setName("Test Patient");
            testPatient.setEmail("test@example.com");
            testPatient.setAddress("Test Address");
            testPatient.setDateOfBirth(LocalDate.of(1990, 1, 1));
            testPatient.setRegisteredDate(LocalDate.now());
            testPatient.setProblem("Test Problem");
            testPatient.setLocation("Test Location");
            testPatient.setConsultationFee(new BigDecimal("50.00"));

            // Test the circuit breaker
            boolean success = billingGrpcClient.sendPatientData(testPatient);
            
            result.put("status", "SUCCESS");
            result.put("billing_call_result", success);
            result.put("message", "Circuit breaker test completed");
            
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", e.getMessage());
            logger.error("Circuit breaker test failed", e);
        }
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/test-bloom-filter")
    public ResponseEntity<Map<String, Object>> testBloomFilter(@RequestParam String email) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Test if email might already exist
            boolean mightExist = bloomFilterService.mightContainEmail(email);
            
            // Add email to bloom filter
            bloomFilterService.addEmail(email);
            
            // Test again after adding
            boolean mightExistAfterAdd = bloomFilterService.mightContainEmail(email);
            
            result.put("status", "SUCCESS");
            result.put("email", email);
            result.put("might_exist_before_add", mightExist);
            result.put("might_exist_after_add", mightExistAfterAdd);
            result.put("message", "Bloom filter test completed");
            
            logger.info("Bloom filter test for email {}: before={}, after={}", email, mightExist, mightExistAfterAdd);
            
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", e.getMessage());
            logger.error("Bloom filter test failed for email: {}", email, e);
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("service", "patient-service");
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        
        // Add circuit breaker health
        try {
            CircuitBreaker billingCircuitBreaker = circuitBreakerRegistry.circuitBreaker("billing-service");
            health.put("circuit_breaker_state", billingCircuitBreaker.getState().toString());
        } catch (Exception e) {
            health.put("circuit_breaker_error", e.getMessage());
        }
        
        return ResponseEntity.ok(health);
    }
} 