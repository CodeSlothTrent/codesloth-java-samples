package com.example.awsk8ssqs.service;

import com.example.awsk8ssqs.model.ClusterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.opensearch.OpenSearchClient;
import software.amazon.awssdk.services.opensearch.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing AWS OpenSearch clusters using the AWS SDK.
 * This mimics how you would provision managed OpenSearch clusters in AWS.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpenSearchService {

    private final OpenSearchClient openSearchClient;

    /**
     * Provisions a new OpenSearch cluster using AWS SDK.
     */
    public CompletableFuture<String> provisionCluster(ClusterRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Starting OpenSearch cluster provisioning: {}", request.getClusterName());
                
                // Create the OpenSearch domain (cluster)
                CreateDomainRequest createRequest = buildCreateDomainRequest(request);
                CreateDomainResponse response = openSearchClient.createDomain(createRequest);
                
                log.info("OpenSearch cluster creation initiated: {}", response.domainStatus().domainName());
                log.info("Cluster ARN: {}", response.domainStatus().arn());
                log.info("Cluster endpoint will be available at: {}", response.domainStatus().endpoint());
                
                // Wait for cluster to be ready
                waitForClusterReady(request.getClusterName());
                
                log.info("OpenSearch cluster provisioned successfully: {}", request.getClusterName());
                return response.domainStatus().arn();
                
            } catch (Exception e) {
                log.error("Failed to provision OpenSearch cluster: {}", request.getClusterName(), e);
                throw new RuntimeException("OpenSearch cluster provisioning failed", e);
            }
        });
    }

    /**
     * Deletes an OpenSearch cluster.
     */
    public CompletableFuture<Void> deleteCluster(String clusterName) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Starting OpenSearch cluster deletion: {}", clusterName);
                
                DeleteDomainRequest deleteRequest = DeleteDomainRequest.builder()
                        .domainName(clusterName)
                        .build();
                
                DeleteDomainResponse response = openSearchClient.deleteDomain(deleteRequest);
                log.info("OpenSearch cluster deletion initiated: {}", response.domainStatus().domainName());
                
                // Wait for deletion to complete
                waitForClusterDeleted(clusterName);
                
                log.info("OpenSearch cluster deleted successfully: {}", clusterName);
                
            } catch (Exception e) {
                log.error("Failed to delete OpenSearch cluster: {}", clusterName, e);
                throw new RuntimeException("OpenSearch cluster deletion failed", e);
            }
        });
    }

    /**
     * Gets the status of an OpenSearch cluster.
     */
    public String getClusterStatus(String clusterName) {
        try {
            DescribeDomainRequest request = DescribeDomainRequest.builder()
                    .domainName(clusterName)
                    .build();
            
            DescribeDomainResponse response = openSearchClient.describeDomain(request);
            DomainStatus status = response.domainStatus();
            
            log.info("Cluster {} status: processing={}, created={}, deleted={}", 
                    clusterName, status.processing(), status.created(), status.deleted());
            
            if (status.deleted()) {
                return "DELETED";
            } else if (status.processing()) {
                return "PROCESSING";
            } else if (status.created()) {
                return "ACTIVE";
            } else {
                return "UNKNOWN";
            }
            
        } catch (ResourceNotFoundException e) {
            return "NOT_FOUND";
        } catch (Exception e) {
            log.error("Failed to get cluster status: {}", clusterName, e);
            return "ERROR";
        }
    }

    /**
     * Gets cluster endpoint URL.
     */
    public String getClusterEndpoint(String clusterName) {
        try {
            DescribeDomainRequest request = DescribeDomainRequest.builder()
                    .domainName(clusterName)
                    .build();
            
            DescribeDomainResponse response = openSearchClient.describeDomain(request);
            return response.domainStatus().endpoint();
            
        } catch (Exception e) {
            log.error("Failed to get cluster endpoint: {}", clusterName, e);
            return null;
        }
    }

    /**
     * Lists all OpenSearch clusters.
     */
    public Map<String, String> listClusters() {
        try {
            ListDomainNamesRequest request = ListDomainNamesRequest.builder().build();
            ListDomainNamesResponse response = openSearchClient.listDomainNames(request);
            
            Map<String, String> clusters = new HashMap<>();
            response.domainNames().forEach(domain -> {
                String status = getClusterStatus(domain.domainName());
                clusters.put(domain.domainName(), status);
            });
            
            return clusters;
            
        } catch (Exception e) {
            log.error("Failed to list clusters", e);
            return new HashMap<>();
        }
    }

    /**
     * Builds the CreateDomainRequest from ClusterRequest.
     */
    private CreateDomainRequest buildCreateDomainRequest(ClusterRequest request) {
        // Build cluster configuration
        ClusterConfig.Builder clusterConfigBuilder = ClusterConfig.builder()
                .instanceType(mapInstanceType(request))
                .instanceCount(request.getNodeCount())
                .dedicatedMasterEnabled(false); // Simplified for demo

        // Build EBS options for storage
        EBSOptions.Builder ebsOptionsBuilder = EBSOptions.builder()
                .ebsEnabled(true)
                .volumeType(VolumeType.GP2)
                .volumeSize(parseStorageSize(request.getResources().getStorageSize()));

        // Build domain endpoint options
        DomainEndpointOptions domainEndpointOptions = DomainEndpointOptions.builder()
                .enforceHTTPS(false) // Simplified for local development
                .build();

        // Build node-to-node encryption options
        NodeToNodeEncryptionOptions nodeToNodeEncryption = NodeToNodeEncryptionOptions.builder()
                .enabled(false) // Simplified for local development
                .build();

        // Build encryption at rest options
        EncryptionAtRestOptions encryptionAtRest = EncryptionAtRestOptions.builder()
                .enabled(false) // Simplified for local development
                .build();

        return CreateDomainRequest.builder()
                .domainName(request.getClusterName())
                .engineVersion("OpenSearch_" + mapOpenSearchVersion(request.getVersion()))
                .clusterConfig(clusterConfigBuilder.build())
                .ebsOptions(ebsOptionsBuilder.build())
                .domainEndpointOptions(domainEndpointOptions)
                .nodeToNodeEncryptionOptions(nodeToNodeEncryption)
                .encryptionAtRestOptions(encryptionAtRest)
                .build();
    }

    /**
     * Maps cluster request to OpenSearch instance type.
     */
    private OpenSearchPartitionInstanceType mapInstanceType(ClusterRequest request) {
        // Simple mapping based on resource requirements
        if (request.getResources() != null && request.getResources().getMemoryRequest() != null) {
            String memory = request.getResources().getMemoryRequest();
            if (memory.contains("4Gi") || memory.contains("4G")) {
                return OpenSearchPartitionInstanceType.M6G_LARGE_SEARCH;
            } else if (memory.contains("2Gi") || memory.contains("2G")) {
                return OpenSearchPartitionInstanceType.M6G_MEDIUM_SEARCH;
            }
        }
        
        // Default to small instance
        return OpenSearchPartitionInstanceType.T3_SMALL_SEARCH;
    }

    /**
     * Maps Elasticsearch version to OpenSearch version.
     */
    private String mapOpenSearchVersion(String elasticsearchVersion) {
        // Map common Elasticsearch versions to OpenSearch versions
        switch (elasticsearchVersion) {
            case "8.11.0":
            case "8.10.0":
                return "2.11";
            case "7.17.0":
                return "1.3";
            default:
                return "2.11"; // Default to latest
        }
    }

    /**
     * Parses storage size string to integer (GB).
     */
    private Integer parseStorageSize(String storageSize) {
        if (storageSize == null) return 10; // Default 10GB
        
        String size = storageSize.toLowerCase().replaceAll("[^0-9]", "");
        try {
            return Integer.parseInt(size);
        } catch (NumberFormatException e) {
            return 10; // Default 10GB
        }
    }

    /**
     * Waits for cluster to be ready.
     */
    private void waitForClusterReady(String clusterName) {
        int maxAttempts = 30; // 5 minutes with 10-second intervals
        int attempt = 0;
        
        while (attempt < maxAttempts) {
            try {
                String status = getClusterStatus(clusterName);
                if ("ACTIVE".equals(status)) {
                    log.info("Cluster {} is now active", clusterName);
                    return;
                }
                
                log.info("Waiting for cluster {} to be ready... Status: {} (attempt {}/{})", 
                        clusterName, status, attempt + 1, maxAttempts);
                
                Thread.sleep(10000); // Wait 10 seconds
                attempt++;
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for cluster", e);
            }
        }
        
        throw new RuntimeException("Cluster " + clusterName + " did not become ready within timeout");
    }

    /**
     * Waits for cluster to be deleted.
     */
    private void waitForClusterDeleted(String clusterName) {
        int maxAttempts = 30; // 5 minutes with 10-second intervals
        int attempt = 0;
        
        while (attempt < maxAttempts) {
            try {
                String status = getClusterStatus(clusterName);
                if ("NOT_FOUND".equals(status) || "DELETED".equals(status)) {
                    log.info("Cluster {} has been deleted", clusterName);
                    return;
                }
                
                log.info("Waiting for cluster {} to be deleted... Status: {} (attempt {}/{})", 
                        clusterName, status, attempt + 1, maxAttempts);
                
                Thread.sleep(10000); // Wait 10 seconds
                attempt++;
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for cluster deletion", e);
            }
        }
        
        log.warn("Cluster {} deletion did not complete within timeout", clusterName);
    }
} 