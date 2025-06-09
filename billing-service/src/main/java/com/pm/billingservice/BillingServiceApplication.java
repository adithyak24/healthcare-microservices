package com.pm.billingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableJpaRepositories // To scan for JPA repositories
@EnableKafka // To enable Kafka listener and producer features
public class BillingServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(BillingServiceApplication.class, args);
  }

}
