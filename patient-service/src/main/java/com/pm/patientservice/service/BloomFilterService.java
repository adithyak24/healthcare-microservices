package com.pm.patientservice.service;

import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.BitSet;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.pm.patientservice.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class BloomFilterService {
    
    private static final Logger logger = LoggerFactory.getLogger(BloomFilterService.class);
    
    // Bloom filter parameters
    private static final int BIT_SET_SIZE = 1000000; // 1M bits for good false positive rate
    private static final int NUM_HASH_FUNCTIONS = 3; // Number of hash functions
    
    private BitSet bloomFilter;
    private MessageDigest md5;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @PostConstruct
    public void initializeBloomFilter() {
        try {
            bloomFilter = new BitSet(BIT_SET_SIZE);
            md5 = MessageDigest.getInstance("MD5");
            
            // Load existing emails into bloom filter
            loadExistingEmails();
            
            logger.info("✅ Bloom filter successfully initialized:");
            logger.info("   - Bit set size: {} bits", BIT_SET_SIZE);
            logger.info("   - Hash functions: {}", NUM_HASH_FUNCTIONS);
            logger.info("   - False positive rate: ~{}%", calculateFalsePositiveRate());
            
        } catch (NoSuchAlgorithmException e) {
            logger.error("❌ Failed to initialize MD5 for bloom filter", e);
            throw new RuntimeException("Critical error: Cannot initialize bloom filter", e);
        }
    }
    
    private void loadExistingEmails() {
        try {
            List<String> existingEmails = patientRepository.findAllEmails();
            int loadedCount = 0;
            for (String email : existingEmails) {
                if (email != null && !email.trim().isEmpty()) {
                    addToBloomFilter(email.toLowerCase().trim());
                    loadedCount++;
                }
            }
            logger.info("📧 Loaded {} existing emails into bloom filter (total found: {})", 
                       loadedCount, existingEmails.size());
        } catch (Exception e) {
            logger.warn("⚠️ Failed to load existing emails into bloom filter: {}", e.getMessage());
            // Continue with empty bloom filter - it will still work for new emails
        }
    }
    
    /**
     * Add an email to the bloom filter
     */
    public void addEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            addToBloomFilter(email.toLowerCase().trim());
            logger.debug("➕ Added email to bloom filter: {}", email.toLowerCase().trim());
        }
    }
    
    /**
     * Check if an email might already exist (bloom filter check)
     * Returns true if email MIGHT exist (could be false positive)
     * Returns false if email DEFINITELY does not exist
     */
    public boolean mightContainEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        return checkBloomFilter(email.toLowerCase().trim());
    }
    
    private void addToBloomFilter(String email) {
        int[] hashes = getHashValues(email);
        for (int hash : hashes) {
            bloomFilter.set(Math.abs(hash) % BIT_SET_SIZE);
        }
    }
    
    private boolean checkBloomFilter(String email) {
        int[] hashes = getHashValues(email);
        for (int hash : hashes) {
            if (!bloomFilter.get(Math.abs(hash) % BIT_SET_SIZE)) {
                return false; // Definitely not in the set
            }
        }
        return true; // Might be in the set
    }
    
    private int[] getHashValues(String email) {
        int[] hashes = new int[NUM_HASH_FUNCTIONS];
        
        try {
            byte[] emailBytes = email.getBytes();
            
            // First hash - simple hashCode
            hashes[0] = email.hashCode();
            
            // Second hash - MD5 based
            md5.reset();
            byte[] md5Hash = md5.digest(emailBytes);
            hashes[1] = bytesToInt(md5Hash, 0);
            
            // Third hash - combination with salt
            hashes[2] = (email + "salt").hashCode();
            
        } catch (Exception e) {
            logger.warn("Error generating hash for email: {}", e.getMessage());
            // Fallback to simple hashes
            for (int i = 0; i < NUM_HASH_FUNCTIONS; i++) {
                hashes[i] = (email + i).hashCode();
            }
        }
        
        return hashes;
    }
    
    private int bytesToInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24) |
               ((bytes[offset + 1] & 0xFF) << 16) |
               ((bytes[offset + 2] & 0xFF) << 8) |
               (bytes[offset + 3] & 0xFF);
    }
    
    /**
     * Calculate the theoretical false positive rate
     * Formula: (1 - e^(-k*n/m))^k
     * where k = number of hash functions, n = number of inserted elements, m = size of bit array
     */
    private double calculateFalsePositiveRate() {
        try {
            List<String> existingEmails = patientRepository.findAllEmails();
            int n = existingEmails.size(); // number of elements
            int m = BIT_SET_SIZE; // size of bit array
            int k = NUM_HASH_FUNCTIONS; // number of hash functions
            
            double rate = Math.pow((1 - Math.exp(-k * (double) n / m)), k) * 100;
            return Math.round(rate * 100.0) / 100.0; // Round to 2 decimal places
        } catch (Exception e) {
            return 1.0; // Default conservative estimate
        }
    }
} 