package com.pm.patientservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.jwt.secret}")
    private String jwtSecretString;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] secretKeyBytes = jwtSecretString.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    // Chain for Receptionist Portal & API Gateway endpoints (no JWT required)
    @Bean
    @Order(1)
    public SecurityFilterChain receptionistEndpointsFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(
                "/patients/recent",
                "/patients/all",
                "/patients",
                "/patients/*",
                "/patients/*/visits",
                "/patients/*/visits/*/payment-status",
                "/patients/*/visits/*/schedule-appointment",
                "/patients/*/appointment",
                "/actuator/**",
                "/swagger-ui/**",
                "/v3/api-docs/**"
            )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());

        return http.build();
    }

    // Chain for Patient Portal endpoints (JWT required)
    @Bean
    @Order(2)
    public SecurityFilterChain patientPortalFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/patients/auth/login", "/patients/me/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(HttpMethod.POST, "/patients/auth/login").permitAll()
                .requestMatchers("/patients/me/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())));

        return http.build();
    }

    // Default chain for any remaining endpoints
    @Bean
    @Order(3)
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());

        return http.build();
    }
} 