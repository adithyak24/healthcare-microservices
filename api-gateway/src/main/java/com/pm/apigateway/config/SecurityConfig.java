package com.pm.apigateway.config;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String auth0IssuerUri;

    @Value("${appcustomprops.patient-service.jwt.secret}")
    private String patientServiceJwtSecret;

    @Value("${appcustomprops.patient-service.jwt.issuer}")
    private String patientServiceJwtIssuer;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allowing specific origins for frontend development
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:3001", "http://127.0.0.1:3000", "http://127.0.0.1:3001"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); 

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public ReactiveAuthenticationManagerResolver<ServerWebExchange> tokenAuthenticationManagerResolver() {
        // Default manager for Auth0 tokens (RS256)
        ReactiveJwtDecoder jwtDecoderForAuth0 = ReactiveJwtDecoders.fromOidcIssuerLocation(auth0IssuerUri);
        ReactiveAuthenticationManager auth0AuthManager = new JwtReactiveAuthenticationManager(jwtDecoderForAuth0);

        // Manager for Patient Service tokens (HS256)
        SecretKeySpec patientServiceSecretKeySpec = new SecretKeySpec(patientServiceJwtSecret.getBytes(StandardCharsets.UTF_8), "HS256");
        ReactiveJwtDecoder jwtDecoderForPatientService = NimbusReactiveJwtDecoder.withSecretKey(patientServiceSecretKeySpec).build();
        ReactiveAuthenticationManager patientServiceAuthManager = new JwtReactiveAuthenticationManager(jwtDecoderForPatientService);

        return exchange -> {
            String path = exchange.getRequest().getPath().value();
            
            // Skip authentication for webhook endpoints completely
            if (path.startsWith("/api/billing/stripe-webhooks")) {
                logger.debug("Skipping authentication for webhook path: {}", path);
                return Mono.empty(); // No authentication manager needed for webhooks
            }
            
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
                String tokenValue = authHeader.substring(7);
                try {
                    SignedJWT signedJWT = SignedJWT.parse(tokenValue);
                    JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
                    String issuer = claimsSet.getIssuer();
                    logger.debug("Token issuer: {}", issuer);

                    if (patientServiceJwtIssuer.equals(issuer)) {
                        logger.debug("Attempting authentication with Patient Service (HS256) Authentication Manager for issuer: {}", issuer);
                        return Mono.just(patientServiceAuthManager);
                    } else {
                        logger.debug("Attempting authentication with Default/Auth0 (RS256) Authentication Manager for issuer: {}", issuer);
                        return Mono.just(auth0AuthManager);
                    }
                } catch (ParseException e) {
                    logger.error("Failed to parse JWT to determine issuer: {}. Token: {}", e.getMessage(), tokenValue);
                    return Mono.error(new InvalidBearerTokenException("Invalid JWT format", e));
                }
            }
            // If no Authorization header or not a Bearer token,
            // let one of the managers (e.g., auth0AuthManager by default) handle it.
            // It will fail if authentication is required for the path and no valid token is resolved by a manager.
            logger.debug("No token or not Bearer, will proceed and let path security decide. Defaulting to Auth0 manager context if needed.");
            return Mono.just(auth0AuthManager); // Default if no token, to provide a non-null manager
        };
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(authorize -> authorize
                .anyExchange().permitAll() // Permit all requests
            );
            
        return http.build();
    }
} 