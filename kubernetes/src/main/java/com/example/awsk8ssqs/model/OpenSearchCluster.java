package com.example.awsk8ssqs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OpenSearchCluster {
    
    // Kubernetes API fields
    private String apiVersion;
    private String kind;
    
    private ObjectMeta metadata;
    private ClusterSpec spec;
    private ClusterStatus status;
    
    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ObjectMeta {
        private String name;
        private String namespace;
        private Map<String, String> labels;
        private Map<String, String> annotations;
        private LocalDateTime creationTimestamp;
    }
    
    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClusterSpec {
        private String clusterName;
        private Integer nodeCount;
        private String version;
        private String instanceType;
        private ClusterThresholds thresholds;
        private AutoScalingConfig autoScaling;
        private List<RemediationRule> remediationRules;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClusterThresholds {
        private Double cpuHigh;
        private Double cpuLow;
        private Double memoryHigh;
        private Double memoryLow;
        private Double diskHigh;
        private Double latencyHigh;
        private Double queryRateHigh;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AutoScalingConfig {
        private Boolean enabled;
        private Integer minNodes;
        private Integer maxNodes;
        private String cooldownPeriod;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RemediationRule {
        private String name;
        private String condition;
        private String action;
        private String cooldown;
        private String priority;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClusterStatus {
        private ClusterPhase phase;
        private Integer nodeCount;
        private String endpoint;
        private CurrentMetrics currentMetrics;
        private LastAction lastAction;
        private List<ClusterCondition> conditions;
        private LocalDateTime lastUpdated;
    }
    
    public enum ClusterPhase {
        PENDING("Pending"),
        CREATING("Creating"),
        READY("Ready"),
        SCALING("Scaling"),
        ERROR("Error"),
        DELETING("Deleting");
        
        private final String value;
        
        ClusterPhase(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentMetrics {
        private Double cpu;
        private Double memory;
        private Double disk;
        private Double latency;
        private Double queryRate;
        private LocalDateTime lastUpdated;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LastAction {
        private String type;
        private String reason;
        private LocalDateTime timestamp;
        private Boolean success;
        private String message;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClusterCondition {
        private String type;
        private String status;
        private LocalDateTime lastTransitionTime;
        private String reason;
        private String message;
    }
    
    // Helper methods
    public boolean isAutoScalingEnabled() {
        return spec != null && 
               spec.getAutoScaling() != null && 
               Boolean.TRUE.equals(spec.getAutoScaling().getEnabled());
    }
    
    public boolean isReady() {
        return status != null && status.getPhase() == ClusterPhase.READY;
    }
    
    public boolean canScale() {
        return isAutoScalingEnabled() && isReady();
    }
    
    public int getMaxNodes() {
        if (spec == null || spec.getAutoScaling() == null) {
            return spec != null ? spec.getNodeCount() : 1;
        }
        return spec.getAutoScaling().getMaxNodes();
    }
    
    public int getMinNodes() {
        if (spec == null || spec.getAutoScaling() == null) {
            return 1;
        }
        return spec.getAutoScaling().getMinNodes();
    }
} 