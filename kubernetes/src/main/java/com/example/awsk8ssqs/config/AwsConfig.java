package com.example.awsk8ssqs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.opensearch.OpenSearchClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

/**
 * AWS configuration for both local development (LocalStack) and production environments.
 */
@Configuration
public class AwsConfig {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.localstack.endpoint:http://localhost:4566}")
    private String localstackEndpoint;

    @Value("${aws.localstack.access-key:test}")
    private String localstackAccessKey;

    @Value("${aws.localstack.secret-key:test}")
    private String localstackSecretKey;

    /**
     * SQS Client for local development with LocalStack.
     */
    @Bean
    @Profile("local")
    public SqsClient localSqsClient() {
        return SqsClient.builder()
                .region(Region.of(awsRegion))
                .endpointOverride(URI.create(localstackEndpoint))
                .credentialsProvider(localCredentialsProvider())
                .build();
    }

    /**
     * SQS Client for production environment.
     */
    @Bean
    @Profile("!local")
    public SqsClient prodSqsClient() {
        return SqsClient.builder()
                .region(Region.of(awsRegion))
                .build();
    }

    /**
     * OpenSearch Client for local development with LocalStack.
     */
    @Bean
    @Profile("local")
    public OpenSearchClient localOpenSearchClient() {
        return OpenSearchClient.builder()
                .region(Region.of(awsRegion))
                .endpointOverride(URI.create(localstackEndpoint))
                .credentialsProvider(localCredentialsProvider())
                .build();
    }

    /**
     * OpenSearch Client for production environment.
     */
    @Bean
    @Profile("!local")
    public OpenSearchClient prodOpenSearchClient() {
        return OpenSearchClient.builder()
                .region(Region.of(awsRegion))
                .build();
    }

    /**
     * Credentials provider for LocalStack.
     */
    private AwsCredentialsProvider localCredentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(localstackAccessKey, localstackSecretKey)
        );
    }
} 