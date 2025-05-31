package com.example.awsk8ssqs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a cluster provisioning request received from SQS.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusterRequest {

    @NotBlank(message = "Cluster name is required")
    @JsonProperty("clusterName")
    private String clusterName;

    @NotBlank(message = "Cluster type is required")
    @JsonProperty("clusterType")
    private String clusterType; // e.g., "opensearch", "elasticsearch"

    @NotNull(message = "Node count is required")
    @Positive(message = "Node count must be positive")
    @JsonProperty("nodeCount")
    private Integer nodeCount;

    @JsonProperty("version")
    private String version; // e.g., "2.11" for OpenSearch

    @JsonProperty("namespace")
    private String namespace; // Not used for OpenSearch, kept for compatibility

    @JsonProperty("resources")
    private ResourceRequirements resources;

    @JsonProperty("configuration")
    private Map<String, Object> configuration;

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("requestedAt")
    private LocalDateTime requestedAt;

    @JsonProperty("requestedBy")
    private String requestedBy;

    /**
     * Resource requirements for the cluster.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceRequirements {
        @JsonProperty("cpuRequest")
        private String cpuRequest; // e.g., "500m"

        @JsonProperty("cpuLimit")
        private String cpuLimit; // e.g., "1000m"

        @JsonProperty("memoryRequest")
        private String memoryRequest; // e.g., "1Gi"

        @JsonProperty("memoryLimit")
        private String memoryLimit; // e.g., "2Gi"

        @JsonProperty("storageSize")
        private String storageSize; // e.g., "10Gi"

        @JsonProperty("storageClass")
        private String storageClass; // e.g., "standard"
    }
} 