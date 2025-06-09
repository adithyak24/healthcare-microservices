package com.pm.patientservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.data.web.config.EnableSpringDataWebSupport; // Keep if needed, but not directly related to this issue
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;

// Import for exclusion
import net.devh.boot.grpc.server.autoconfigure.GrpcServerSecurityAutoConfiguration;

@SpringBootApplication(
    exclude = {
        GrpcServerSecurityAutoConfiguration.class
    }
)
@EnableCaching
@EnableKafka
// @EnableSpringDataWebSupport // This can also be kept if needed for other features like Pageable resolving
public class PatientServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(PatientServiceApplication.class, args);
  }

}
