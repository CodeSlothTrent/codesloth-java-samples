package com.example.awsk8ssqs.service;

import com.example.awsk8ssqs.model.ClusterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for orchestrating cluster provisioning operations.
 * Now uses AWS OpenSearch SDK to provision managed clusters.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClusterProvisioningService {

    private final OpenSearchService openSearchService;

    /**
     * Provisions a cluster based on the request using AWS OpenSearch SDK.
     * This method is async to avoid blocking SQS message processing.
     * 
     * @param request The cluster provisioning request
     * @return CompletableFuture for async processing
     */
    @Async
    public CompletableFuture<String> provisionCluster(ClusterRequest request) {
        log.info("Starting OpenSearch cluster provisioning for: {}", request.getClusterName());
        
        try {
            // Route to appropriate service based on cluster type
            switch (request.getClusterType().toLowerCase()) {
                case "elasticsearch":
                case "opensearch":
                    return openSearchService.provisionCluster(request);
                default:
                    return CompletableFuture.failedFuture(
                        new IllegalArgumentException("Unsupported cluster type: " + request.getClusterType() + 
                            ". Supported types: opensearch, elasticsearch"));
            }
            
        } catch (Exception e) {
            log.error("Failed to provision cluster {}: {}", request.getClusterName(), e.getMessage(), e);
            return CompletableFuture.failedFuture(new RuntimeException("Cluster provisioning failed", e));
        }
    }

    /**
     * Deletes an OpenSearch cluster.
     * 
     * @param clusterName The name of the cluster to delete
     * @param namespace The namespace where the cluster is located (not used for OpenSearch)
     * @return CompletableFuture for async processing
     */
    @Async
    public CompletableFuture<Void> deleteCluster(String clusterName, String namespace) {
        log.info("Starting OpenSearch cluster deletion for: {}", clusterName);
        
        try {
            return openSearchService.deleteCluster(clusterName);
            
        } catch (Exception e) {
            log.error("Failed to delete cluster {}: {}", clusterName, e.getMessage(), e);
            return CompletableFuture.failedFuture(new RuntimeException("Cluster deletion failed", e));
        }
    }

    /**
     * Gets the status of an OpenSearch cluster.
     * 
     * @param clusterName The name of the cluster
     * @param namespace The namespace where the cluster is located (not used for OpenSearch)
     * @return The cluster status
     */
    public String getClusterStatus(String clusterName, String namespace) {
        try {
            return openSearchService.getClusterStatus(clusterName);
        } catch (Exception e) {
            log.error("Failed to get cluster status for {}: {}", clusterName, e.getMessage());
            return "UNKNOWN";
        }
    }

    /**
     * Gets the endpoint URL of an OpenSearch cluster.
     * 
     * @param clusterName The name of the cluster
     * @return The cluster endpoint URL
     */
    public String getClusterEndpoint(String clusterName) {
        try {
            return openSearchService.getClusterEndpoint(clusterName);
        } catch (Exception e) {
            log.error("Failed to get cluster endpoint for {}: {}", clusterName, e.getMessage());
            return null;
        }
    }
} 