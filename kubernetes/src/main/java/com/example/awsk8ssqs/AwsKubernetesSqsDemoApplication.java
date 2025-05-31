package com.example.awsk8ssqs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main Spring Boot application for AWS Kubernetes SQS Demo.
 * 
 * This application demonstrates:
 * - Listening to SQS messages for cluster provisioning requests
 * - Using Kubernetes Java client to manage cluster resources
 * - Provisioning Elasticsearch clusters locally
 */
@SpringBootApplication
@EnableAsync
public class AwsKubernetesSqsDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AwsKubernetesSqsDemoApplication.class, args);
    }
} 