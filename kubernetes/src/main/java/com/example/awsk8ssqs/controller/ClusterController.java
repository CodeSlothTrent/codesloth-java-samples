package com.example.awsk8ssqs.controller;

import com.example.awsk8ssqs.model.ClusterRequest;
import com.example.awsk8ssqs.service.ClusterProvisioningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for cluster management operations.
 * Provides endpoints for testing and monitoring cluster operations.
 */
@RestController
@RequestMapping("/api/clusters")
@RequiredArgsConstructor
@Slf4j
public class ClusterController {

    private final ClusterProvisioningService clusterProvisioningService;

    /**
     * Creates a cluster directly via REST API (for testing).
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCluster(@Valid @RequestBody ClusterRequest request) {
        log.info("Received cluster creation request via REST API: {}", request.getClusterName());
        
        try {
            // Set default values if not provided
            if (request.getRequestId() == null) {
                request.setRequestId(UUID.randomUUID().toString());
            }
            if (request.getRequestedAt() == null) {
                request.setRequestedAt(LocalDateTime.now());
            }
            if (request.getRequestedBy() == null) {
                request.setRequestedBy("rest-api");
            }
            
            // Process the request asynchronously
            clusterProvisioningService.provisionCluster(request);
            
            Map<String, Object> response = Map.of(
                "status", "accepted",
                "message", "Cluster provisioning request accepted",
                "requestId", request.getRequestId(),
                "clusterName", request.getClusterName()
            );
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            log.error("Error processing cluster creation request: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = Map.of(
                "status", "error",
                "message", "Failed to process cluster creation request",
                "error", e.getMessage()
            );
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Deletes a cluster via REST API.
     */
    @DeleteMapping("/{clusterName}")
    public ResponseEntity<Map<String, Object>> deleteCluster(
            @PathVariable String clusterName,
            @RequestParam(defaultValue = "default") String namespace) {
        
        log.info("Received cluster deletion request via REST API: {} in namespace: {}", clusterName, namespace);
        
        try {
            clusterProvisioningService.deleteCluster(clusterName, namespace);
            
            Map<String, Object> response = Map.of(
                "status", "accepted",
                "message", "Cluster deletion request accepted",
                "clusterName", clusterName,
                "namespace", namespace
            );
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            log.error("Error processing cluster deletion request: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = Map.of(
                "status", "error",
                "message", "Failed to process cluster deletion request",
                "error", e.getMessage()
            );
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Gets the status of an OpenSearch cluster.
     */
    @GetMapping("/{clusterName}/status")
    public ResponseEntity<Map<String, Object>> getClusterStatus(
            @PathVariable String clusterName,
            @RequestParam(defaultValue = "default") String namespace) {
        
        log.info("Getting status for OpenSearch cluster: {}", clusterName);
        
        try {
            String status = clusterProvisioningService.getClusterStatus(clusterName, namespace);
            String endpoint = clusterProvisioningService.getClusterEndpoint(clusterName);
            
            Map<String, Object> response = Map.of(
                "clusterName", clusterName,
                "status", status,
                "endpoint", endpoint != null ? endpoint : "Not available",
                "timestamp", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting cluster status: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = Map.of(
                "status", "error",
                "message", "Failed to get cluster status",
                "error", e.getMessage()
            );
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Gets the endpoint URL of an OpenSearch cluster.
     */
    @GetMapping("/{clusterName}/endpoint")
    public ResponseEntity<Map<String, Object>> getClusterEndpoint(@PathVariable String clusterName) {
        log.info("Getting endpoint for OpenSearch cluster: {}", clusterName);
        
        try {
            String endpoint = clusterProvisioningService.getClusterEndpoint(clusterName);
            
            if (endpoint != null) {
                Map<String, Object> response = Map.of(
                    "clusterName", clusterName,
                    "endpoint", endpoint,
                    "timestamp", LocalDateTime.now()
                );
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = Map.of(
                    "clusterName", clusterName,
                    "message", "Cluster endpoint not available",
                    "timestamp", LocalDateTime.now()
                );
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error getting cluster endpoint: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = Map.of(
                "status", "error",
                "message", "Failed to get cluster endpoint",
                "error", e.getMessage()
            );
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "service", "cluster-controller"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a sample OpenSearch cluster request for testing.
     */
    @PostMapping("/sample")
    public ResponseEntity<Map<String, Object>> createSampleCluster() {
        log.info("Creating sample OpenSearch cluster for testing");
        
        ClusterRequest sampleRequest = ClusterRequest.builder()
                .clusterName("sample-opensearch-" + System.currentTimeMillis())
                .clusterType("opensearch")
                .nodeCount(1)
                .version("2.11")
                .namespace("default")
                .resources(ClusterRequest.ResourceRequirements.builder()
                        .cpuRequest("500m")
                        .cpuLimit("1000m")
                        .memoryRequest("1Gi")
                        .memoryLimit("2Gi")
                        .storageSize("10Gi")
                        .storageClass("gp2")
                        .build())
                .requestId(UUID.randomUUID().toString())
                .requestedAt(LocalDateTime.now())
                .requestedBy("sample-api")
                .build();
        
        return createCluster(sampleRequest);
    }

    /**
     * Lists all OpenSearch clusters.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listClusters() {
        log.info("Listing all OpenSearch clusters");
        
        try {
            // This would need to be implemented in the service layer
            Map<String, Object> response = Map.of(
                "message", "List clusters endpoint - implementation needed",
                "timestamp", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error listing clusters: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = Map.of(
                "status", "error",
                "message", "Failed to list clusters",
                "error", e.getMessage()
            );
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
} 