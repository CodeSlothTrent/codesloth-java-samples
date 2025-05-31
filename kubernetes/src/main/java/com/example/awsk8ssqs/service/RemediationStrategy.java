package com.example.awsk8ssqs.service;

import com.example.awsk8ssqs.model.CloudWatchMetrics;
import com.example.awsk8ssqs.model.OpenSearchCluster;
import com.example.awsk8ssqs.model.RemediationAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RemediationStrategy {
    
    /**
     * Build comprehensive remediation strategy based on cluster health
     */
    public List<RemediationAction> buildStrategy(OpenSearchCluster cluster, CloudWatchMetrics metrics) {
        ClusterHealth health = analyzeOverallHealth(metrics, cluster);
        
        log.info("Cluster {} health level: {}, score: {}", 
            cluster.getMetadata().getName(), health.getLevel(), health.getScore());
        
        switch (health.getLevel()) {
            case CRITICAL:
                return criticalRemediationPlan(cluster, metrics, health);
                
            case WARNING:
                return warningRemediationPlan(cluster, metrics, health);
                
            case OPTIMIZATION:
                return optimizationPlan(cluster, metrics, health);
                
            case HEALTHY:
            default:
                return Collections.emptyList();
        }
    }
    
    /**
     * Analyze overall cluster health based on multiple metrics
     */
    public ClusterHealth analyzeOverallHealth(CloudWatchMetrics metrics, OpenSearchCluster cluster) {
        var metricsData = metrics.getMetrics();
        var thresholds = cluster.getSpec().getThresholds();
        
        double healthScore = 100.0;
        HealthLevel level = HealthLevel.HEALTHY;
        
        // CPU health impact
        if (metricsData.getCpu() != null) {
            double cpuUsage = metricsData.getCpu().getAverage();
            if (cpuUsage > thresholds.getCpuHigh()) {
                double cpuImpact = Math.min(30, (cpuUsage - thresholds.getCpuHigh()) / thresholds.getCpuHigh() * 30);
                healthScore -= cpuImpact;
                level = HealthLevel.max(level, cpuUsage > thresholds.getCpuHigh() * 1.5 ? HealthLevel.CRITICAL : HealthLevel.WARNING);
            }
        }
        
        // Memory health impact
        if (metricsData.getMemory() != null) {
            double memoryUsage = metricsData.getMemory().getAverage();
            if (memoryUsage > thresholds.getMemoryHigh()) {
                double memoryImpact = Math.min(25, (memoryUsage - thresholds.getMemoryHigh()) / thresholds.getMemoryHigh() * 25);
                healthScore -= memoryImpact;
                level = HealthLevel.max(level, memoryUsage > thresholds.getMemoryHigh() * 1.4 ? HealthLevel.CRITICAL : HealthLevel.WARNING);
            }
        }
        
        // Latency health impact
        if (metricsData.getSearchLatency() != null) {
            double latency = metricsData.getSearchLatency().getP95();
            if (latency > thresholds.getLatencyHigh()) {
                double latencyImpact = Math.min(25, (latency - thresholds.getLatencyHigh()) / thresholds.getLatencyHigh() * 25);
                healthScore -= latencyImpact;
                level = HealthLevel.max(level, latency > thresholds.getLatencyHigh() * 2 ? HealthLevel.CRITICAL : HealthLevel.WARNING);
            }
        }
        
        // Disk health impact
        if (metricsData.getDisk() != null) {
            double diskUsage = metricsData.getDisk().getAverage();
            if (diskUsage > thresholds.getDiskHigh()) {
                double diskImpact = Math.min(20, (diskUsage - thresholds.getDiskHigh()) / (100 - thresholds.getDiskHigh()) * 20);
                healthScore -= diskImpact;
                level = HealthLevel.max(level, diskUsage > 95 ? HealthLevel.CRITICAL : HealthLevel.WARNING);
            }
        }
        
        // Query rate health impact
        if (metricsData.getQueryRate() != null) {
            double queryRate = metricsData.getQueryRate().getAverage();
            if (queryRate > thresholds.getQueryRateHigh()) {
                double queryImpact = Math.min(15, (queryRate - thresholds.getQueryRateHigh()) / thresholds.getQueryRateHigh() * 15);
                healthScore -= queryImpact;
                level = HealthLevel.max(level, HealthLevel.WARNING);
            }
        }
        
        // Error rate impact (if available)
        if (metricsData.getErrorRate() != null) {
            double errorRate = metricsData.getErrorRate().getAverage();
            if (errorRate > 1.0) { // > 1% error rate
                double errorImpact = Math.min(30, errorRate * 5);
                healthScore -= errorImpact;
                level = HealthLevel.max(level, errorRate > 5.0 ? HealthLevel.CRITICAL : HealthLevel.WARNING);
            }
        }
        
        // Determine final health level based on score
        if (healthScore < 60) {
            level = HealthLevel.max(level, HealthLevel.CRITICAL);
        } else if (healthScore < 80) {
            level = HealthLevel.max(level, HealthLevel.WARNING);
        } else if (healthScore < 95) {
            level = HealthLevel.max(level, HealthLevel.OPTIMIZATION);
        }
        
        return ClusterHealth.builder()
            .level(level)
            .score(Math.max(0, healthScore))
            .build();
    }
    
    /**
     * Critical remediation plan - immediate action required
     */
    private List<RemediationAction> criticalRemediationPlan(OpenSearchCluster cluster, CloudWatchMetrics metrics, ClusterHealth health) {
        log.warn("Creating CRITICAL remediation plan for cluster: {}", cluster.getMetadata().getName());
        
        return Arrays.asList(
            // Immediate relief through scaling
            RemediationAction.builder()
                .type(RemediationAction.ActionType.EMERGENCY_SCALE)
                .priority(RemediationAction.Priority.IMMEDIATE)
                .reason("Critical cluster health - emergency scaling")
                .targetNodes(Math.min(cluster.getSpec().getNodeCount() * 2, cluster.getMaxNodes()))
                .ruleName("emergency-scale")
                .build(),
                
            // Create backup cluster for failover
            RemediationAction.builder()
                .type(RemediationAction.ActionType.CREATE_NEW_CLUSTER)
                .priority(RemediationAction.Priority.CRITICAL)
                .reason("Critical health - creating backup cluster")
                .ruleName("critical-backup-cluster")
                .build(),
                
            // Immediate critical alert
            RemediationAction.builder()
                .type(RemediationAction.ActionType.ALERT_CRITICAL)
                .priority(RemediationAction.Priority.IMMEDIATE)
                .reason(String.format("Critical cluster health (score: %.1f)", health.getScore()))
                .alertLevel(RemediationAction.AlertLevel.CRITICAL)
                .ruleName("critical-alert")
                .build()
        );
    }
    
    /**
     * Warning remediation plan - proactive action needed
     */
    private List<RemediationAction> warningRemediationPlan(OpenSearchCluster cluster, CloudWatchMetrics metrics, ClusterHealth health) {
        log.info("Creating WARNING remediation plan for cluster: {}", cluster.getMetadata().getName());
        
        var actions = Arrays.asList(
            // Gradual scaling based on specific bottlenecks
            determineScalingAction(cluster, metrics),
            
            // Optimization based on specific issues
            determineOptimizationAction(cluster, metrics),
            
            // Alert for awareness
            RemediationAction.builder()
                .type(RemediationAction.ActionType.ALERT)
                .priority(RemediationAction.Priority.HIGH)
                .reason(String.format("Cluster health degraded (score: %.1f)", health.getScore()))
                .alertLevel(RemediationAction.AlertLevel.WARNING)
                .ruleName("warning-alert")
                .build()
        );
        
        return actions.stream()
            .filter(action -> action != null)
            .toList();
    }
    
    /**
     * Optimization plan - fine-tuning for better performance
     */
    private List<RemediationAction> optimizationPlan(OpenSearchCluster cluster, CloudWatchMetrics metrics, ClusterHealth health) {
        log.info("Creating OPTIMIZATION plan for cluster: {}", cluster.getMetadata().getName());
        
        var actions = Arrays.asList(
            // Performance optimizations
            determineOptimizationAction(cluster, metrics),
            
            // Preemptive scaling if trends indicate future issues
            determinePreemptiveAction(cluster, metrics),
            
            // Info alert about optimization
            RemediationAction.builder()
                .type(RemediationAction.ActionType.ALERT)
                .priority(RemediationAction.Priority.LOW)
                .reason(String.format("Cluster optimization opportunity (score: %.1f)", health.getScore()))
                .alertLevel(RemediationAction.AlertLevel.INFO)
                .ruleName("optimization-alert")
                .build()
        );
        
        return actions.stream()
            .filter(action -> action != null)
            .toList();
    }
    
    /**
     * Determine appropriate scaling action based on metrics
     */
    private RemediationAction determineScalingAction(OpenSearchCluster cluster, CloudWatchMetrics metrics) {
        if (!cluster.canScale()) {
            return null;
        }
        
        var metricsData = metrics.getMetrics();
        var thresholds = cluster.getSpec().getThresholds();
        
        // Determine if scaling out is needed
        boolean needsScaleOut = false;
        int targetNodes = cluster.getSpec().getNodeCount();
        
        if (metricsData.getCpu() != null && metricsData.getCpu().getAverage() > thresholds.getCpuHigh()) {
            needsScaleOut = true;
            targetNodes = Math.max(targetNodes, cluster.getSpec().getNodeCount() + 1);
        }
        
        if (metricsData.getMemory() != null && metricsData.getMemory().getAverage() > thresholds.getMemoryHigh()) {
            needsScaleOut = true;
            targetNodes = Math.max(targetNodes, cluster.getSpec().getNodeCount() + 1);
        }
        
        if (metricsData.getSearchLatency() != null && metricsData.getSearchLatency().getP95() > thresholds.getLatencyHigh()) {
            needsScaleOut = true;
            targetNodes = Math.max(targetNodes, cluster.getSpec().getNodeCount() + 1);
        }
        
        if (needsScaleOut) {
            return RemediationAction.builder()
                .type(RemediationAction.ActionType.SCALE_OUT)
                .priority(RemediationAction.Priority.HIGH)
                .reason("Resource utilization exceeds thresholds")
                .targetNodes(Math.min(targetNodes, cluster.getMaxNodes()))
                .ruleName("threshold-scale-out")
                .build();
        }
        
        return null;
    }
    
    /**
     * Determine optimization action based on performance patterns
     */
    private RemediationAction determineOptimizationAction(OpenSearchCluster cluster, CloudWatchMetrics metrics) {
        var metricsData = metrics.getMetrics();
        var optimizations = new java.util.ArrayList<String>();
        
        // High latency optimizations
        if (metricsData.getSearchLatency() != null && metricsData.getSearchLatency().getP95() > cluster.getSpec().getThresholds().getLatencyHigh()) {
            optimizations.add("query_cache");
            optimizations.add("field_data_cache");
            if (metricsData.getQueryRate() != null && metricsData.getQueryRate().getAverage() < 50) {
                optimizations.add("refresh_interval");
            }
        }
        
        // High indexing rate optimizations
        if (metricsData.getIndexingRate() != null && metricsData.getIndexingRate().getAverage() > 1000) {
            optimizations.add("bulk_size");
            optimizations.add("flush_threshold");
            optimizations.add("merge_policy");
        }
        
        // High memory usage optimizations
        if (metricsData.getMemory() != null && metricsData.getMemory().getAverage() > cluster.getSpec().getThresholds().getMemoryHigh() * 0.8) {
            optimizations.add("fielddata_limit");
            optimizations.add("circuit_breaker");
        }
        
        if (optimizations.isEmpty()) {
            return null;
        }
        
        return RemediationAction.builder()
            .type(RemediationAction.ActionType.OPTIMIZE_CLUSTER)
            .priority(RemediationAction.Priority.MEDIUM)
            .reason("Performance optimization opportunity detected")
            .optimizations(optimizations)
            .ruleName("performance-optimization")
            .build();
    }
    
    /**
     * Determine preemptive action based on trends
     */
    private RemediationAction determinePreemptiveAction(OpenSearchCluster cluster, CloudWatchMetrics metrics) {
        // Simple trend analysis - in production, use more sophisticated time-series analysis
        var metricsData = metrics.getMetrics();
        var thresholds = cluster.getSpec().getThresholds();
        
        // Check if metrics are approaching thresholds (within 10% of threshold)
        boolean approachingLimits = false;
        
        if (metricsData.getCpu() != null) {
            double cpuUsage = metricsData.getCpu().getAverage();
            if (cpuUsage > thresholds.getCpuHigh() * 0.9) {
                approachingLimits = true;
            }
        }
        
        if (metricsData.getMemory() != null) {
            double memoryUsage = metricsData.getMemory().getAverage();
            if (memoryUsage > thresholds.getMemoryHigh() * 0.9) {
                approachingLimits = true;
            }
        }
        
        if (approachingLimits && cluster.canScale()) {
            return RemediationAction.builder()
                .type(RemediationAction.ActionType.PREEMPTIVE_SCALE)
                .priority(RemediationAction.Priority.LOW)
                .reason("Metrics approaching thresholds - preemptive scaling")
                .targetNodes(cluster.getSpec().getNodeCount() + 1)
                .ruleName("preemptive-scale")
                .build();
        }
        
        return null;
    }
    
    /**
     * Health level enumeration
     */
    public enum HealthLevel {
        HEALTHY(4),
        OPTIMIZATION(3),
        WARNING(2),
        CRITICAL(1);
        
        private final int priority;
        
        HealthLevel(int priority) {
            this.priority = priority;
        }
        
        public static HealthLevel max(HealthLevel a, HealthLevel b) {
            return a.priority < b.priority ? a : b;
        }
    }
    
    /**
     * Cluster health assessment
     */
    public static class ClusterHealth {
        private final HealthLevel level;
        private final double score;
        
        private ClusterHealth(HealthLevel level, double score) {
            this.level = level;
            this.score = score;
        }
        
        public static ClusterHealth.Builder builder() {
            return new Builder();
        }
        
        public HealthLevel getLevel() {
            return level;
        }
        
        public double getScore() {
            return score;
        }
        
        public static class Builder {
            private HealthLevel level;
            private double score;
            
            public Builder level(HealthLevel level) {
                this.level = level;
                return this;
            }
            
            public Builder score(double score) {
                this.score = score;
                return this;
            }
            
            public ClusterHealth build() {
                return new ClusterHealth(level, score);
            }
        }
    }
} 