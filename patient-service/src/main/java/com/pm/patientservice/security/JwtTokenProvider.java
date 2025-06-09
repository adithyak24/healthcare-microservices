package com.pm.patientservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret}")
    private String jwtSecretString;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    private SecretKey jwtSecretKey;
    public static final String PATIENT_ID_CLAIM = "patientId";
    public static final String ISSUER_PATIENT_SERVICE = "patient-service-local";

    @PostConstruct
    protected void init() {
        if (jwtSecretString == null || jwtSecretString.getBytes(StandardCharsets.UTF_8).length < 32) {
            logger.warn("JWT secret (app.jwt.secret) from properties is null, too short (requires at least 256 bits / 32 UTF-8 chars), or not configured. Using a default, insecure key. PLEASE CONFIGURE a strong app.jwt.secret.");
            String tempSecret = "ThisIsADefaultDevelopmentSecretKeyPleaseChangeItForProductionSecurity";
            this.jwtSecretKey = Keys.hmacShaKeyFor(tempSecret.getBytes(StandardCharsets.UTF_8));
            logger.info("Using a default (development-only) JWT secret key due to configuration issue.");
        } else {
            byte[] keyBytes = jwtSecretString.getBytes(StandardCharsets.UTF_8);
            this.jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);
            logger.info("JWT Secret Key initialized from application.properties for HS256.");
        }

        if (this.jwtExpirationMs <= 0) {
            logger.warn("JWT expiration (app.jwt.expiration-ms) is not configured or invalid (<=0). Using default 1 hour (3600000 ms).");
            this.jwtExpirationMs = 3600000;
        }
        logger.info("JWT Expiration in JwtTokenProvider set to: {} ms", this.jwtExpirationMs);
    }

    public String createToken(String email, UUID patientId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        logger.info("Token Generation - Email: {}, Patient ID: {}", email, patientId);
        logger.info("Token Generation - Current Time (ms from epoch): {}", now.getTime());
        logger.info("Token Generation - Expiration Time (ms from epoch): {}", expiryDate.getTime());
        logger.info("Token Generation - Configured Expiration Duration (ms): {}", jwtExpirationMs);
        logger.info("Token Generation - IssuedAt Date Object: {}", now);
        logger.info("Token Generation - ExpiryDate Date Object: {}", expiryDate);
        logger.debug("Creating token for email: {}, algorithm: {}, key algorithm: {}", email, SignatureAlgorithm.HS256, jwtSecretKey.getAlgorithm());

        return Jwts.builder()
                .setIssuer(ISSUER_PATIENT_SERVICE)
                .setSubject(email)
                .claim(PATIENT_ID_CLAIM, patientId.toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // You might add methods here later to validate tokens or extract username from token
    // if patient-service needs to validate tokens itself (e.g. for inter-service comms)
    // For now, API Gateway will likely handle external token validation.
} 