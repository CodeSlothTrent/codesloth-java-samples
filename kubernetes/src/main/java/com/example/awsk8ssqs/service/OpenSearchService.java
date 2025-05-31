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
     * Scale an existing cluster
     */
    public boolean scaleCluster(String clusterName, int newNodeCount) {
        try {
            log.info("Scaling cluster {} to {} nodes", clusterName, newNodeCount);
            
            UpdateDomainConfigRequest updateRequest = UpdateDomainConfigRequest.builder()
                .domainName(clusterName)
                .clusterConfig(ClusterConfig.builder()
                    .instanceCount(newNodeCount)
                    .build())
                .build();
            
            UpdateDomainConfigResponse response = openSearchClient.updateDomainConfig(updateRequest);
            log.info("Cluster scaling initiated: {}", response.domainConfig().clusterConfig().instanceCount());
            return true;
            
        } catch (Exception e) {
            log.error("Failed to scale cluster {}: {}", clusterName, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Create cluster from OpenSearchCluster specification
     */
    public boolean createCluster(com.example.awsk8ssqs.model.OpenSearchCluster.ClusterSpec spec) {
        try {
            log.info("Creating OpenSearch cluster: {}", spec.getClusterName());
            
            CreateDomainRequest createRequest = CreateDomainRequest.builder()
                .domainName(spec.getClusterName())
                .engineVersion(spec.getVersion())
                .clusterConfig(ClusterConfig.builder()
                    .instanceType(OpenSearchPartitionInstanceType.fromValue(spec.getInstanceType()))
                    .instanceCount(spec.getNodeCount())
                    .dedicatedMasterEnabled(spec.getNodeCount() >= 3)
                    .masterInstanceType(spec.getNodeCount() >= 3 ? OpenSearchPartitionInstanceType.fromValue(spec.getInstanceType()) : null)
                    .masterInstanceCount(spec.getNodeCount() >= 3 ? 3 : null)
                    .build())
                .ebsOptions(EBSOptions.builder()
                    .ebsEnabled(true)
                    .volumeType(VolumeType.GP2)
                    .volumeSize(20) // Default 20GB
                    .build())
                .accessPolicies(createAccessPolicy(spec.getClusterName()))
                .build();
            
            CreateDomainResponse response = openSearchClient.createDomain(createRequest);
            log.info("OpenSearch cluster creation initiated: {}", response.domainStatus().domainName());
            return true;
            
        } catch (Exception e) {
            log.error("Failed to create cluster {}: {}", spec.getClusterName(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Optimize cluster settings based on performance issues
     */
    public boolean optimizeCluster(String clusterName, java.util.List<String> optimizations) {
        try {
            log.info("Optimizing cluster {} with: {}", clusterName, optimizations);
            
            // In a real implementation, these would update cluster settings via OpenSearch API
            // For demo purposes, we'll simulate the optimization
            for (String optimization : optimizations) {
                switch (optimization) {
                    case "query_cache":
                        log.info("Enabling query cache optimization for cluster: {}", clusterName);
                        break;
                    case "field_data_cache":
                        log.info("Optimizing field data cache for cluster: {}", clusterName);
                        break;
                    case "refresh_interval":
                        log.info("Adjusting refresh interval for cluster: {}", clusterName);
                        break;
                    case "bulk_size":
                        log.info("Optimizing bulk size for cluster: {}", clusterName);
                        break;
                    case "merge_policy":
                        log.info("Optimizing merge policy for cluster: {}", clusterName);
                        break;
                    case "fielddata_limit":
                        log.info("Adjusting field data limit for cluster: {}", clusterName);
                        break;
                    case "circuit_breaker":
                        log.info("Configuring circuit breaker for cluster: {}", clusterName);
                        break;
                    default:
                        log.warn("Unknown optimization: {}", optimization);
                }
            }
            
            // In LocalStack, we can't actually change OpenSearch settings,
            // but in AWS we would use the OpenSearch client to update cluster configuration
            log.info("Cluster optimization completed for: {}", clusterName);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to optimize cluster {}: {}", clusterName, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Create access policy for cluster
     */
    private String createAccessPolicy(String clusterName) {
        return """
            {
                "Version": "2012-10-17",
                "Statement": [
                    {
                        "Effect": "Allow",
                        "Principal": {
                            "AWS": "*"
                        },
                        "Action": "es:*",
                        "Resource": "arn:aws:es:*:*:domain/%s/*"
                    }
                ]
            }
            """.formatted(clusterName);
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