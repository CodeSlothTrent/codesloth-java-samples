package com.example.awsk8ssqs.service;

import com.example.awsk8ssqs.model.CloudWatchMetrics;
import com.example.awsk8ssqs.model.OpenSearchCluster;
import com.example.awsk8ssqs.model.RemediationAction;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenSearchClusterController {
    
    private final ObjectMapper objectMapper;
    private final OpenSearchService openSearchService;
    private final RemediationStrategy remediationStrategy;
    private final AlertService alertService;
    private final MetricsAnalyzer metricsAnalyzer;
    private final CooldownManager cooldownManager;
    
    // Kubernetes API service for managing cluster definitions in etcd via API server
    private final KubernetesClusterService kubernetesClusterService;
    
    @SqsListener("${aws.sqs.metrics-queue}")
    public void processCloudWatchMetrics(@Payload String message) {
        try {
            CloudWatchMetrics metrics = objectMapper.readValue(message, CloudWatchMetrics.class);
            log.info("Processing CloudWatch metrics for cluster: {}", metrics.getClusterName());
            
            // Get the cluster definition from Kubernetes API
            OpenSearchCluster cluster = kubernetesClusterService.getClusterByName(metrics.getClusterName());
            if (cluster == null) {
                log.warn("No OpenSearchCluster found for: {}. Creating default cluster definition.", 
                    metrics.getClusterName());
                cluster = kubernetesClusterService.createDefaultClusterDefinition(metrics.getClusterName());
            }
            
            // Update cluster metrics
            updateClusterMetrics(cluster, metrics);
            
            // Analyze metrics and determine remediation actions
            List<RemediationAction> actions = analyzeAndPlanRemediation(cluster, metrics);
            
            // Filter actions by cooldown periods
            List<RemediationAction> executableActions = filterByCooldown(cluster, actions);
            
            // Execute actions in priority order
            executeRemediationActions(cluster, executableActions, metrics);
            
            // Update cluster status in Kubernetes API
            updateClusterStatus(cluster, metrics, executableActions);
            
        } catch (Exception e) {
            log.error("Error processing CloudWatch metrics: {}", e.getMessage(), e);
        }
    }
    
    private List<RemediationAction> analyzeAndPlanRemediation(OpenSearchCluster cluster, CloudWatchMetrics metrics) {
        List<RemediationAction> actions = new ArrayList<>();
        
        if (cluster.getSpec() == null || cluster.getSpec().getThresholds() == null) {
            log.warn("No thresholds configured for cluster: {}", cluster.getMetadata().getName());
            return actions;
        }
        
        var thresholds = cluster.getSpec().getThresholds();
        var metricsData = metrics.getMetrics();
        
        // CPU Analysis
        if (metricsData.getCpu() != null) {
            double cpuAvg = metricsData.getCpu().getAverage();
            
            if (cpuAvg > thresholds.getCpuHigh() && cluster.canScale()) {
                actions.add(RemediationAction.builder()
                    .type(RemediationAction.ActionType.SCALE_OUT)
                    .priority(RemediationAction.Priority.HIGH)
                    .reason(String.format("CPU utilization %.1f%% exceeds threshold %.1f%%", cpuAvg, thresholds.getCpuHigh()))
                    .targetNodes(calculateScaleOutNodes(cluster, cpuAvg, thresholds.getCpuHigh()))
                    .ruleName("cpu-scale-out")
                    .build());
            } else if (cpuAvg < thresholds.getCpuLow() && canScaleIn(cluster)) {
                actions.add(RemediationAction.builder()
                    .type(RemediationAction.ActionType.SCALE_IN)
                    .priority(RemediationAction.Priority.LOW)
                    .reason(String.format("CPU utilization %.1f%% below threshold %.1f%%", cpuAvg, thresholds.getCpuLow()))
                    .targetNodes(calculateScaleInNodes(cluster, cpuAvg, thresholds.getCpuLow()))
                    .ruleName("cpu-scale-in")
                    .build());
            }
        }
        
        // Memory Analysis
        if (metricsData.getMemory() != null) {
            double memoryAvg = metricsData.getMemory().getAverage();
            
            if (memoryAvg > thresholds.getMemoryHigh() && cluster.canScale()) {
                actions.add(RemediationAction.builder()
                    .type(RemediationAction.ActionType.SCALE_OUT)
                    .priority(RemediationAction.Priority.HIGH)
                    .reason(String.format("Memory utilization %.1f%% exceeds threshold %.1f%%", memoryAvg, thresholds.getMemoryHigh()))
                    .targetNodes(calculateScaleOutNodes(cluster, memoryAvg, thresholds.getMemoryHigh()))
                    .ruleName("memory-scale-out")
                    .build());
            }
        }
        
        // Latency Analysis
        if (metricsData.getSearchLatency() != null) {
            double latencyP95 = metricsData.getSearchLatency().getP95();
            
            if (latencyP95 > thresholds.getLatencyHigh()) {
                if (latencyP95 > thresholds.getLatencyHigh() * 2.5) {
                    // Critical latency - create new cluster
                    actions.add(RemediationAction.builder()
                        .type(RemediationAction.ActionType.CREATE_NEW_CLUSTER)
                        .priority(RemediationAction.Priority.CRITICAL)
                        .reason(String.format("Critical search latency %.1fms, creating new cluster", latencyP95))
                        .ruleName("critical-latency-new-cluster")
                        .build());
                } else if (cluster.canScale()) {
                    // High latency - scale out
                    actions.add(RemediationAction.builder()
                        .type(RemediationAction.ActionType.SCALE_OUT)
                        .priority(RemediationAction.Priority.HIGH)
                        .reason(String.format("Search latency %.1fms exceeds threshold %.1fms", latencyP95, thresholds.getLatencyHigh()))
                        .targetNodes(cluster.getSpec().getNodeCount() + 1)
                        .ruleName("latency-scale-out")
                        .build());
                } else {
                    // Can't scale - optimize cluster
                    actions.add(RemediationAction.builder()
                        .type(RemediationAction.ActionType.OPTIMIZE_CLUSTER)
                        .priority(RemediationAction.Priority.MEDIUM)
                        .reason(String.format("Search latency %.1fms high but cannot scale", latencyP95))
                        .optimizations(List.of("refresh_interval", "merge_policy", "index_settings"))
                        .ruleName("latency-optimization")
                        .build());
                }
            }
        }
        
        // Combined stress analysis
        if (isUnderCombinedStress(cluster, metrics)) {
            actions.add(RemediationAction.builder()
                .type(RemediationAction.ActionType.EMERGENCY_SCALE)
                .priority(RemediationAction.Priority.IMMEDIATE)
                .reason("Cluster under combined resource stress")
                .targetNodes(Math.min(cluster.getSpec().getNodeCount() * 2, cluster.getMaxNodes()))
                .ruleName("emergency-scale")
                .build());
        }
        
        // Disk space analysis
        if (metricsData.getDisk() != null && metricsData.getDisk().getAverage() > thresholds.getDiskHigh()) {
            actions.add(RemediationAction.builder()
                .type(RemediationAction.ActionType.ALERT_CRITICAL)
                .priority(RemediationAction.Priority.CRITICAL)
                .reason(String.format("Disk utilization %.1f%% exceeds threshold %.1f%%", 
                    metricsData.getDisk().getAverage(), thresholds.getDiskHigh()))
                .alertLevel(RemediationAction.AlertLevel.CRITICAL)
                .ruleName("disk-space-alert")
                .build());
        }
        
        // Apply custom remediation rules
        actions.addAll(applyCustomRules(cluster, metrics));
        
        return actions;
    }
    
    private boolean isUnderCombinedStress(OpenSearchCluster cluster, CloudWatchMetrics metrics) {
        var thresholds = cluster.getSpec().getThresholds();
        var metricsData = metrics.getMetrics();
        
        boolean highCpu = metricsData.getCpu() != null && 
            metricsData.getCpu().getAverage() > thresholds.getCpuHigh() * 0.9;
        boolean highMemory = metricsData.getMemory() != null && 
            metricsData.getMemory().getAverage() > thresholds.getMemoryHigh() * 0.9;
        boolean highLatency = metricsData.getSearchLatency() != null && 
            metricsData.getSearchLatency().getP95() > thresholds.getLatencyHigh() * 1.5;
        
        // At least 2 out of 3 stress indicators
        return (highCpu && highMemory) || (highCpu && highLatency) || (highMemory && highLatency);
    }
    
    private List<RemediationAction> applyCustomRules(OpenSearchCluster cluster, CloudWatchMetrics metrics) {
        List<RemediationAction> actions = new ArrayList<>();
        
        if (cluster.getSpec().getRemediationRules() == null) {
            return actions;
        }
        
        for (var rule : cluster.getSpec().getRemediationRules()) {
            if (evaluateRuleCondition(rule.getCondition(), cluster, metrics)) {
                RemediationAction action = createActionFromRule(rule, cluster, metrics);
                if (action != null) {
                    actions.add(action);
                }
            }
        }
        
        return actions;
    }
    
    private boolean evaluateRuleCondition(String condition, OpenSearchCluster cluster, CloudWatchMetrics metrics) {
        // Basic condition evaluator for custom remediation rules
        try {
            var metricsData = metrics.getMetrics();
            var thresholds = cluster.getSpec().getThresholds();
            
            // Replace variables in condition
            String evaluableCondition = condition
                .replace("cpu", String.valueOf(metricsData.getCpu() != null ? metricsData.getCpu().getAverage() : 0))
                .replace("memory", String.valueOf(metricsData.getMemory() != null ? metricsData.getMemory().getAverage() : 0))
                .replace("latency_p95", String.valueOf(metricsData.getSearchLatency() != null ? metricsData.getSearchLatency().getP95() : 0))
                .replace("queryRate", String.valueOf(metricsData.getQueryRate() != null ? metricsData.getQueryRate().getAverage() : 0))
                .replace("nodeCount", String.valueOf(cluster.getSpec().getNodeCount()))
                .replace("minNodes", String.valueOf(cluster.getMinNodes()));
            
            // Boolean expression evaluation for remediation rule conditions
            return evaluateSimpleExpression(evaluableCondition);
            
        } catch (Exception e) {
            log.warn("Failed to evaluate rule condition: {}", condition, e);
            return false;
        }
    }
    
    private boolean evaluateSimpleExpression(String expression) {
        // Basic expression evaluator for AND/OR conditions
        try {
            if (expression.contains(" AND ")) {
                String[] parts = expression.split(" AND ");
                return evaluateComparison(parts[0].trim()) && evaluateComparison(parts[1].trim());
            } else if (expression.contains(" OR ")) {
                String[] parts = expression.split(" OR ");
                return evaluateComparison(parts[0].trim()) || evaluateComparison(parts[1].trim());
            } else {
                return evaluateComparison(expression.trim());
            }
        } catch (Exception e) {
            log.warn("Failed to evaluate expression: {}", expression, e);
            return false;
        }
    }
    
    private boolean evaluateComparison(String comparison) {
        if (comparison.contains(" > ")) {
            String[] parts = comparison.split(" > ");
            return Double.parseDouble(parts[0].trim()) > Double.parseDouble(parts[1].trim());
        } else if (comparison.contains(" < ")) {
            String[] parts = comparison.split(" < ");
            return Double.parseDouble(parts[0].trim()) < Double.parseDouble(parts[1].trim());
        }
        return false;
    }
    
    private RemediationAction createActionFromRule(OpenSearchCluster.RemediationRule rule, 
                                                  OpenSearchCluster cluster, CloudWatchMetrics metrics) {
        try {
            RemediationAction.ActionType actionType = RemediationAction.ActionType.valueOf(rule.getAction().toUpperCase());
            RemediationAction.Priority priority = rule.getPriority() != null ? 
                RemediationAction.Priority.valueOf(rule.getPriority().toUpperCase()) : RemediationAction.Priority.MEDIUM;
            
            return RemediationAction.builder()
                .type(actionType)
                .priority(priority)
                .reason("Custom rule: " + rule.getName())
                .ruleName(rule.getName())
                .build();
        } catch (Exception e) {
            log.warn("Failed to create action from rule: {}", rule.getName(), e);
            return null;
        }
    }
    
    private List<RemediationAction> filterByCooldown(OpenSearchCluster cluster, List<RemediationAction> actions) {
        return actions.stream()
            .filter(action -> cooldownManager.canExecuteAction(cluster.getMetadata().getName(), action.getRuleName()))
            .toList();
    }
    
    private void executeRemediationActions(OpenSearchCluster cluster, List<RemediationAction> actions, CloudWatchMetrics metrics) {
        // Sort by priority
        actions.stream()
            .sorted((a, b) -> Integer.compare(a.getPriority().getLevel(), b.getPriority().getLevel()))
            .forEach(action -> {
                try {
                    log.info("Executing remediation action: {} for cluster: {}", 
                        action.getType(), cluster.getMetadata().getName());
                    
                    CompletableFuture<Boolean> future = executeAction(cluster, action, metrics);
                    
                    // For critical actions, wait for completion
                    if (action.isHighPriority()) {
                        boolean success = future.get();
                        action.setSuccess(success);
                        action.setExecuted(true);
                        action.setExecutedTime(LocalDateTime.now());
                        
                        if (success) {
                            cooldownManager.recordAction(cluster.getMetadata().getName(), action.getRuleName());
                        }
                    }
                    
                } catch (Exception e) {
                    log.error("Failed to execute action: {} for cluster: {}", 
                        action.getType(), cluster.getMetadata().getName(), e);
                    action.setSuccess(false);
                    action.setErrorMessage(e.getMessage());
                }
            });
    }
    
    private CompletableFuture<Boolean> executeAction(OpenSearchCluster cluster, RemediationAction action, CloudWatchMetrics metrics) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                switch (action.getType()) {
                    case SCALE_OUT:
                        return scaleOutCluster(cluster, action.getTargetNodes());
                        
                    case SCALE_IN:
                        return scaleInCluster(cluster, action.getTargetNodes());
                        
                    case CREATE_NEW_CLUSTER:
                        return createNewCluster(cluster, action);
                        
                    case OPTIMIZE_CLUSTER:
                        return optimizeCluster(cluster, action.getOptimizations());
                        
                    case ALERT:
                    case ALERT_CRITICAL:
                        return sendAlert(cluster, action, metrics);
                        
                    case EMERGENCY_SCALE:
                        return emergencyScale(cluster, action.getTargetNodes());
                        
                    default:
                        log.warn("Unknown action type: {}", action.getType());
                        return false;
                }
            } catch (Exception e) {
                log.error("Error executing action: {}", action.getType(), e);
                return false;
            }
        });
    }
    
    private boolean scaleOutCluster(OpenSearchCluster cluster, int targetNodes) {
        int currentNodes = cluster.getSpec().getNodeCount();
        int newNodeCount = Math.min(targetNodes != null ? targetNodes : currentNodes + 1, cluster.getMaxNodes());
        
        if (newNodeCount <= currentNodes) {
            log.info("Cluster {} already at or above target size", cluster.getMetadata().getName());
            return true;
        }
        
        log.info("Scaling out cluster {} from {} to {} nodes", 
            cluster.getMetadata().getName(), currentNodes, newNodeCount);
        
        try {
            // Update cluster spec
            cluster.getSpec().setNodeCount(newNodeCount);
            cluster.getStatus().setPhase(OpenSearchCluster.ClusterPhase.SCALING);
            
            // Scale the actual OpenSearch cluster in LocalStack
            boolean success = openSearchService.scaleCluster(cluster.getSpec().getClusterName(), newNodeCount);
            
            if (success) {
                cluster.getStatus().setNodeCount(newNodeCount);
                cluster.getStatus().setPhase(OpenSearchCluster.ClusterPhase.READY);
            } else {
                cluster.getStatus().setPhase(OpenSearchCluster.ClusterPhase.ERROR);
            }
            
            return success;
        } catch (Exception e) {
            log.error("Failed to scale out cluster: {}", cluster.getMetadata().getName(), e);
            cluster.getStatus().setPhase(OpenSearchCluster.ClusterPhase.ERROR);
            return false;
        }
    }
    
    private boolean scaleInCluster(OpenSearchCluster cluster, int targetNodes) {
        int currentNodes = cluster.getSpec().getNodeCount();
        int newNodeCount = Math.max(targetNodes != null ? targetNodes : currentNodes - 1, cluster.getMinNodes());
        
        if (newNodeCount >= currentNodes) {
            log.info("Cluster {} already at or below target size", cluster.getMetadata().getName());
            return true;
        }
        
        log.info("Scaling in cluster {} from {} to {} nodes", 
            cluster.getMetadata().getName(), currentNodes, newNodeCount);
        
        try {
            cluster.getSpec().setNodeCount(newNodeCount);
            cluster.getStatus().setPhase(OpenSearchCluster.ClusterPhase.SCALING);
            
            boolean success = openSearchService.scaleCluster(cluster.getSpec().getClusterName(), newNodeCount);
            
            if (success) {
                cluster.getStatus().setNodeCount(newNodeCount);
                cluster.getStatus().setPhase(OpenSearchCluster.ClusterPhase.READY);
            } else {
                cluster.getStatus().setPhase(OpenSearchCluster.ClusterPhase.ERROR);
            }
            
            return success;
        } catch (Exception e) {
            log.error("Failed to scale in cluster: {}", cluster.getMetadata().getName(), e);
            cluster.getStatus().setPhase(OpenSearchCluster.ClusterPhase.ERROR);
            return false;
        }
    }
    
    private boolean createNewCluster(OpenSearchCluster cluster, RemediationAction action) {
        String newClusterName = cluster.getSpec().getClusterName() + "-replica-" + System.currentTimeMillis();
        log.info("Creating new OpenSearch cluster: {}", newClusterName);
        
        try {
            // Create new cluster specification
            var newClusterSpec = cluster.getSpec().toBuilder()
                .clusterName(newClusterName)
                .nodeCount(Math.min(cluster.getSpec().getNodeCount(), 3)) // Start smaller
                .build();
            
            boolean success = openSearchService.createCluster(newClusterSpec);
            
            if (success) {
                // Create new cluster object
                OpenSearchCluster newCluster = cluster.toBuilder()
                    .metadata(cluster.getMetadata().toBuilder()
                        .name(newClusterName)
                        .creationTimestamp(LocalDateTime.now())
                        .build())
                    .spec(newClusterSpec)
                    .status(OpenSearchCluster.ClusterStatus.builder()
                        .phase(OpenSearchCluster.ClusterPhase.CREATING)
                        .build())
                    .build();
                
                // Store new cluster in Kubernetes API
                kubernetesClusterService.createOrUpdateCluster(newCluster);
                
                // Send alert about new cluster creation
                alertService.sendAlert(
                    String.format("Created new OpenSearch cluster: %s due to %s", newClusterName, action.getReason()),
                    RemediationAction.AlertLevel.WARNING
                );
            }
            
            return success;
        } catch (Exception e) {
            log.error("Failed to create new cluster: {}", newClusterName, e);
            return false;
        }
    }
    
    private boolean optimizeCluster(OpenSearchCluster cluster, List<String> optimizations) {
        log.info("Optimizing cluster {} with: {}", cluster.getMetadata().getName(), optimizations);
        
        try {
            return openSearchService.optimizeCluster(cluster.getSpec().getClusterName(), optimizations);
        } catch (Exception e) {
            log.error("Failed to optimize cluster: {}", cluster.getMetadata().getName(), e);
            return false;
        }
    }
    
    private boolean sendAlert(OpenSearchCluster cluster, RemediationAction action, CloudWatchMetrics metrics) {
        String message = String.format("OpenSearch Cluster Alert: %s - %s", 
            cluster.getMetadata().getName(), action.getReason());
        
        alertService.sendAlert(message, action.getAlertLevel());
        return true;
    }
    
    private boolean emergencyScale(OpenSearchCluster cluster, int targetNodes) {
        log.warn("EMERGENCY SCALING cluster {} to {} nodes", cluster.getMetadata().getName(), targetNodes);
        
        // Emergency scaling bypasses normal validation
        try {
            cluster.getSpec().setNodeCount(targetNodes);
            cluster.getStatus().setPhase(OpenSearchCluster.ClusterPhase.SCALING);
            
            boolean success = openSearchService.scaleCluster(cluster.getSpec().getClusterName(), targetNodes);
            
            if (success) {
                cluster.getStatus().setNodeCount(targetNodes);
                cluster.getStatus().setPhase(OpenSearchCluster.ClusterPhase.READY);
                
                // Send critical alert
                alertService.sendAlert(
                    String.format("EMERGENCY: Scaled cluster %s to %d nodes", 
                        cluster.getMetadata().getName(), targetNodes),
                    RemediationAction.AlertLevel.CRITICAL
                );
            }
            
            return success;
        } catch (Exception e) {
            log.error("Emergency scaling failed for cluster: {}", cluster.getMetadata().getName(), e);
            return false;
        }
    }
    
    private void updateClusterMetrics(OpenSearchCluster cluster, CloudWatchMetrics metrics) {
        var metricsData = metrics.getMetrics();
        
        var currentMetrics = OpenSearchCluster.CurrentMetrics.builder()
            .cpu(metricsData.getCpu() != null ? metricsData.getCpu().getAverage() : null)
            .memory(metricsData.getMemory() != null ? metricsData.getMemory().getAverage() : null)
            .disk(metricsData.getDisk() != null ? metricsData.getDisk().getAverage() : null)
            .latency(metricsData.getSearchLatency() != null ? metricsData.getSearchLatency().getP95() : null)
            .queryRate(metricsData.getQueryRate() != null ? metricsData.getQueryRate().getAverage() : null)
            .lastUpdated(LocalDateTime.now())
            .build();
        
        if (cluster.getStatus() == null) {
            cluster.setStatus(OpenSearchCluster.ClusterStatus.builder().build());
        }
        
        cluster.getStatus().setCurrentMetrics(currentMetrics);
        cluster.getStatus().setLastUpdated(LocalDateTime.now());
    }
    
    private void updateClusterStatus(OpenSearchCluster cluster, CloudWatchMetrics metrics, List<RemediationAction> actions) {
        var lastAction = actions.stream()
            .filter(RemediationAction::isExecuted)
            .findFirst()
            .map(action -> OpenSearchCluster.LastAction.builder()
                .type(action.getType().name())
                .reason(action.getReason())
                .timestamp(action.getExecutedTime())
                .success(action.isSuccess())
                .message(action.getErrorMessage())
                .build())
            .orElse(null);
        
        if (lastAction != null) {
            cluster.getStatus().setLastAction(lastAction);
        }
        
        // Update cluster status in Kubernetes API (etcd via API server)
        kubernetesClusterService.updateClusterStatus(cluster);
    }
    

    
    private int calculateScaleOutNodes(OpenSearchCluster cluster, double currentValue, double threshold) {
        int currentNodes = cluster.getSpec().getNodeCount();
        // Scale by 1-2 nodes based on how much over threshold
        double overage = (currentValue - threshold) / threshold;
        int additionalNodes = overage > 0.5 ? 2 : 1;
        return Math.min(currentNodes + additionalNodes, cluster.getMaxNodes());
    }
    
    private int calculateScaleInNodes(OpenSearchCluster cluster, double currentValue, double threshold) {
        int currentNodes = cluster.getSpec().getNodeCount();
        // Scale down by 1 node if significantly under threshold
        double underage = (threshold - currentValue) / threshold;
        return underage > 0.3 ? Math.max(currentNodes - 1, cluster.getMinNodes()) : currentNodes;
    }
    
    private boolean canScaleIn(OpenSearchCluster cluster) {
        return cluster.isAutoScalingEnabled() && 
               cluster.getSpec().getNodeCount() > cluster.getMinNodes() &&
               cluster.isReady();
    }
} 