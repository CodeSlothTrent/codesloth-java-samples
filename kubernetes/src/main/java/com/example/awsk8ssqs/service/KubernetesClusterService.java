package com.example.awsk8ssqs.service;

import com.example.awsk8ssqs.model.OpenSearchCluster;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import io.kubernetes.client.util.generic.KubernetesApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for managing OpenSearchCluster custom resources through the Kubernetes API
 * This replaces the simple in-memory HashMap with real Kubernetes API integration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KubernetesClusterService {
    
    private final CustomObjectsApi customObjectsApi;
    private final GenericKubernetesApi<Object, Object> openSearchClusterApi;
    private final ObjectMapper objectMapper;
    
    // Kubernetes API constants
    private static final String API_GROUP = "opensearch.aws.com";
    private static final String API_VERSION = "v1";
    private static final String RESOURCE_PLURAL = "opensearchclusters";
    private static final String DEFAULT_NAMESPACE = "default";
    
    /**
     * Retrieve an OpenSearchCluster by name from Kubernetes API
     * This is equivalent to: kubectl get opensearchcluster my-cluster
     */
    public OpenSearchCluster getClusterByName(String clusterName) {
        return getClusterByName(clusterName, DEFAULT_NAMESPACE);
    }
    
    public OpenSearchCluster getClusterByName(String clusterName, String namespace) {
        try {
            log.debug("Getting OpenSearchCluster: {} from namespace: {}", clusterName, namespace);
            
            KubernetesApiResponse<Object> response = openSearchClusterApi.get(namespace, clusterName);
            
            if (response.isSuccess() && response.getObject() != null) {
                // Convert the raw object to our OpenSearchCluster model
                return convertToOpenSearchCluster(response.getObject());
            } else {
                log.debug("OpenSearchCluster not found: {} in namespace: {}", clusterName, namespace);
                return null;
            }
            
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                log.debug("OpenSearchCluster not found: {} in namespace: {}", clusterName, namespace);
                return null;
            } else {
                log.error("Error getting OpenSearchCluster: {} from Kubernetes API: {}", clusterName, e.getMessage(), e);
                throw new RuntimeException("Failed to get cluster from Kubernetes API", e);
            }
        } catch (Exception e) {
            log.error("Unexpected error getting cluster: {}", clusterName, e);
            throw new RuntimeException("Failed to get cluster from Kubernetes API", e);
        }
    }
    
    /**
     * Create or update an OpenSearchCluster in Kubernetes API
     * This is equivalent to: kubectl apply -f cluster.yaml
     */
    public OpenSearchCluster createOrUpdateCluster(OpenSearchCluster cluster) {
        return createOrUpdateCluster(cluster, DEFAULT_NAMESPACE);
    }
    
    public OpenSearchCluster createOrUpdateCluster(OpenSearchCluster cluster, String namespace) {
        try {
            String clusterName = cluster.getMetadata().getName();
            log.info("Creating/updating OpenSearchCluster: {} in namespace: {}", clusterName, namespace);
            
            // Convert our model to Kubernetes object
            Map<String, Object> clusterObject = convertToKubernetesObject(cluster);
            
            // Try to get existing cluster first
            OpenSearchCluster existing = getClusterByName(clusterName, namespace);
            
            KubernetesApiResponse<Object> response;
            if (existing != null) {
                // Update existing cluster
                log.debug("Updating existing OpenSearchCluster: {}", clusterName);
                response = openSearchClusterApi.patch(namespace, clusterName, clusterObject);
            } else {
                // Create new cluster
                log.debug("Creating new OpenSearchCluster: {}", clusterName);
                response = openSearchClusterApi.create(namespace, clusterObject);
            }
            
            if (response.isSuccess()) {
                log.info("Successfully created/updated OpenSearchCluster: {}", clusterName);
                return convertToOpenSearchCluster(response.getObject());
            } else {
                log.error("Failed to create/update OpenSearchCluster: {}. Status: {}", 
                    clusterName, response.getStatus());
                throw new RuntimeException("Failed to create/update cluster in Kubernetes API");
            }
            
        } catch (ApiException e) {
            log.error("Kubernetes API error creating/updating cluster: {} - Code: {}, Message: {}", 
                cluster.getMetadata().getName(), e.getCode(), e.getMessage(), e);
            throw new RuntimeException("Failed to create/update cluster in Kubernetes API", e);
        } catch (Exception e) {
            log.error("Unexpected error creating/updating cluster: {}", cluster.getMetadata().getName(), e);
            throw new RuntimeException("Failed to create/update cluster in Kubernetes API", e);
        }
    }
    
    /**
     * List all OpenSearchClusters in the default namespace
     * This is equivalent to: kubectl get opensearchclusters
     */
    public List<OpenSearchCluster> listClusters() {
        return listClusters(DEFAULT_NAMESPACE);
    }
    
    public List<OpenSearchCluster> listClusters(String namespace) {
        try {
            log.debug("Listing OpenSearchClusters in namespace: {}", namespace);
            
            KubernetesApiResponse<Object> response = openSearchClusterApi.list(namespace);
            
            if (response.isSuccess() && response.getObject() != null) {
                return convertToOpenSearchClusterList(response.getObject());
            } else {
                log.warn("Failed to list OpenSearchClusters. Status: {}", response.getStatus());
                return Collections.emptyList();
            }
            
        } catch (ApiException e) {
            log.error("Kubernetes API error listing clusters in namespace: {} - Code: {}, Message: {}", 
                namespace, e.getCode(), e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Unexpected error listing clusters in namespace: {}", namespace, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Delete an OpenSearchCluster from Kubernetes API
     * This is equivalent to: kubectl delete opensearchcluster my-cluster
     */
    public boolean deleteCluster(String clusterName) {
        return deleteCluster(clusterName, DEFAULT_NAMESPACE);
    }
    
    public boolean deleteCluster(String clusterName, String namespace) {
        try {
            log.info("Deleting OpenSearchCluster: {} from namespace: {}", clusterName, namespace);
            
            KubernetesApiResponse<Object> response = openSearchClusterApi.delete(namespace, clusterName);
            
            if (response.isSuccess()) {
                log.info("Successfully deleted OpenSearchCluster: {}", clusterName);
                return true;
            } else {
                log.error("Failed to delete OpenSearchCluster: {}. Status: {}", clusterName, response.getStatus());
                return false;
            }
            
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                log.warn("OpenSearchCluster not found for deletion: {}", clusterName);
                return true; // Consider it successful if it doesn't exist
            } else {
                log.error("Kubernetes API error deleting cluster: {} - Code: {}, Message: {}", 
                    clusterName, e.getCode(), e.getMessage(), e);
                return false;
            }
        } catch (Exception e) {
            log.error("Unexpected error deleting cluster: {}", clusterName, e);
            return false;
        }
    }
    
    /**
     * Update only the status of an OpenSearchCluster
     * This is used by controllers to update cluster status without changing spec
     */
    public OpenSearchCluster updateClusterStatus(OpenSearchCluster cluster) {
        return updateClusterStatus(cluster, DEFAULT_NAMESPACE);
    }
    
    public OpenSearchCluster updateClusterStatus(OpenSearchCluster cluster, String namespace) {
        try {
            String clusterName = cluster.getMetadata().getName();
            log.debug("Updating status for OpenSearchCluster: {}", clusterName);
            
            // Get current cluster to preserve spec
            OpenSearchCluster current = getClusterByName(clusterName, namespace);
            if (current == null) {
                log.warn("Cannot update status - cluster not found: {}", clusterName);
                return null;
            }
            
            // Update only the status
            current.setStatus(cluster.getStatus());
            
            // Convert and update
            Map<String, Object> clusterObject = convertToKubernetesObject(current);
            
            KubernetesApiResponse<Object> response = openSearchClusterApi.patch(namespace, clusterName, clusterObject);
            
            if (response.isSuccess()) {
                return convertToOpenSearchCluster(response.getObject());
            } else {
                log.error("Failed to update OpenSearchCluster status: {}. Status: {}", 
                    clusterName, response.getStatus());
                return null;
            }
            
        } catch (Exception e) {
            log.error("Error updating cluster status: {}", cluster.getMetadata().getName(), e);
            return null;
        }
    }
    
    /**
     * Convert Kubernetes API object to our OpenSearchCluster model
     */
    @SuppressWarnings("unchecked")
    private OpenSearchCluster convertToOpenSearchCluster(Object kubernetesObject) {
        try {
            // Convert to JSON and back to our model
            String json = objectMapper.writeValueAsString(kubernetesObject);
            return objectMapper.readValue(json, OpenSearchCluster.class);
        } catch (Exception e) {
            log.error("Error converting Kubernetes object to OpenSearchCluster", e);
            throw new RuntimeException("Failed to convert Kubernetes object", e);
        }
    }
    
    /**
     * Convert our OpenSearchCluster model to Kubernetes API object
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToKubernetesObject(OpenSearchCluster cluster) {
        try {
            // Ensure Kubernetes metadata is set
            if (cluster.getApiVersion() == null) {
                cluster.setApiVersion(API_GROUP + "/" + API_VERSION);
            }
            if (cluster.getKind() == null) {
                cluster.setKind("OpenSearchCluster");
            }
            
            // Convert to Map for Kubernetes API
            String json = objectMapper.writeValueAsString(cluster);
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.error("Error converting OpenSearchCluster to Kubernetes object", e);
            throw new RuntimeException("Failed to convert to Kubernetes object", e);
        }
    }
    
    /**
     * Convert Kubernetes list response to our OpenSearchCluster list
     */
    @SuppressWarnings("unchecked")
    private List<OpenSearchCluster> convertToOpenSearchClusterList(Object listObject) {
        try {
            Map<String, Object> listMap = (Map<String, Object>) listObject;
            List<Object> items = (List<Object>) listMap.get("items");
            
            if (items == null) {
                return Collections.emptyList();
            }
            
            List<OpenSearchCluster> clusters = new ArrayList<>();
            for (Object item : items) {
                clusters.add(convertToOpenSearchCluster(item));
            }
            
            return clusters;
        } catch (Exception e) {
            log.error("Error converting Kubernetes list to OpenSearchCluster list", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Create a default cluster definition and store it in Kubernetes API
     * This replaces the old in-memory approach
     */
    public OpenSearchCluster createDefaultClusterDefinition(String clusterName) {
        log.info("Creating default OpenSearchCluster definition: {}", clusterName);
        
        var cluster = OpenSearchCluster.builder()
            .apiVersion(API_GROUP + "/" + API_VERSION)
            .kind("OpenSearchCluster")
            .metadata(OpenSearchCluster.ObjectMeta.builder()
                .name(clusterName)
                .namespace(DEFAULT_NAMESPACE)
                .creationTimestamp(LocalDateTime.now())
                .build())
            .spec(OpenSearchCluster.ClusterSpec.builder()
                .clusterName(clusterName)
                .nodeCount(3)
                .version("OpenSearch_2.11")
                .instanceType("m6g.large.search")
                .thresholds(OpenSearchCluster.ClusterThresholds.builder()
                    .cpuHigh(80.0)
                    .cpuLow(30.0)
                    .memoryHigh(85.0)
                    .memoryLow(40.0)
                    .diskHigh(90.0)
                    .latencyHigh(200.0)
                    .queryRateHigh(100.0)
                    .build())
                .autoScaling(OpenSearchCluster.AutoScalingConfig.builder()
                    .enabled(true)
                    .minNodes(2)
                    .maxNodes(10)
                    .cooldownPeriod("10m")
                    .build())
                .build())
            .status(OpenSearchCluster.ClusterStatus.builder()
                .phase(OpenSearchCluster.ClusterPhase.READY)
                .nodeCount(3)
                .build())
            .build();
        
        return createOrUpdateCluster(cluster);
    }
} 