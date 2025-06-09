package com.pm.analyticsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
@EnableElasticsearchRepositories(basePackages = "com.pm.analyticsservice.repository")
// Also ensure JPA repositories are scanned if they are in a different or non-standard package
// @EnableJpaRepositories(basePackages = "com.pm.analyticsservice.repository") // Already covered by @SpringBootApplication if in same/sub-package
public class AnalyticsServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(AnalyticsServiceApplication.class, args);
  }

}
