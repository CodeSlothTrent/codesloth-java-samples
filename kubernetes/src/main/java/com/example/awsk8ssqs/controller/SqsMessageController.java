package com.example.awsk8ssqs.controller;

import com.example.awsk8ssqs.model.ClusterRequest;
import com.example.awsk8ssqs.service.ClusterProvisioningService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import jakarta.validation.Valid;

/**
 * Controller that handles SQS messages for cluster provisioning requests.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class SqsMessageController {

    private final ClusterProvisioningService clusterProvisioningService;
    private final ObjectMapper objectMapper;

    /**
     * Handles cluster provisioning requests from SQS.
     * 
     * @param message The SQS message payload
     * @param messageId The SQS message ID
     * @param receiptHandle The SQS receipt handle
     */
    @SqsListener("${aws.sqs.cluster-requests-queue:cluster-requests}")
    public void handleClusterRequest(
            @Payload String message,
            @Header("MessageId") String messageId,
            @Header("ReceiptHandle") String receiptHandle) {
        
        log.info("Received SQS message with ID: {} for cluster provisioning", messageId);
        
        try {
            // Parse the JSON message into ClusterRequest object
            ClusterRequest clusterRequest = objectMapper.readValue(message, ClusterRequest.class);
            
            log.info("Processing cluster request: {}", clusterRequest);
            
            // Validate the request
            validateClusterRequest(clusterRequest);
            
            // Process the cluster provisioning request asynchronously
            clusterProvisioningService.provisionCluster(clusterRequest);
            
            log.info("Successfully queued cluster provisioning for: {}", clusterRequest.getClusterName());
            
        } catch (Exception e) {
            log.error("Error processing SQS message {}: {}", messageId, e.getMessage(), e);
            // In a production environment, you might want to send the message to a DLQ
            throw new RuntimeException("Failed to process cluster request", e);
        }
    }

    /**
     * Handles cluster deletion requests from SQS.
     * 
     * @param message The SQS message payload
     * @param messageId The SQS message ID
     */
    @SqsListener("${aws.sqs.cluster-deletion-queue:cluster-deletions}")
    public void handleClusterDeletion(
            @Payload String message,
            @Header("MessageId") String messageId) {
        
        log.info("Received SQS message with ID: {} for cluster deletion", messageId);
        
        try {
            // Parse the deletion request (could be just cluster name or full request)
            var deletionRequest = objectMapper.readTree(message);
            String clusterName = deletionRequest.get("clusterName").asText();
            String namespace = deletionRequest.has("namespace") ? 
                deletionRequest.get("namespace").asText() : "default";
            
            log.info("Processing cluster deletion for: {} in namespace: {}", clusterName, namespace);
            
            // Process the cluster deletion request
            clusterProvisioningService.deleteCluster(clusterName, namespace);
            
            log.info("Successfully queued cluster deletion for: {}", clusterName);
            
        } catch (Exception e) {
            log.error("Error processing deletion SQS message {}: {}", messageId, e.getMessage(), e);
            throw new RuntimeException("Failed to process cluster deletion", e);
        }
    }

    /**
     * Basic validation of cluster request.
     */
    private void validateClusterRequest(@Valid ClusterRequest request) {
        if (request.getClusterName() == null || request.getClusterName().trim().isEmpty()) {
            throw new IllegalArgumentException("Cluster name cannot be empty");
        }
        
        if (request.getClusterType() == null || request.getClusterType().trim().isEmpty()) {
            throw new IllegalArgumentException("Cluster type cannot be empty");
        }
        
        if (request.getNodeCount() == null || request.getNodeCount() <= 0) {
            throw new IllegalArgumentException("Node count must be positive");
        }
        
        // Set defaults if not provided
        if (request.getNamespace() == null || request.getNamespace().trim().isEmpty()) {
            request.setNamespace("default");
        }
        
        if (request.getVersion() == null || request.getVersion().trim().isEmpty()) {
            // Set default version based on cluster type
            switch (request.getClusterType().toLowerCase()) {
                case "elasticsearch":
                    request.setVersion("8.11.0");
                    break;
                default:
                    request.setVersion("latest");
            }
        }
    }
} 