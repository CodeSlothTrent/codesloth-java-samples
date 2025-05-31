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
        
        // Simplified alarm-based remediation - CloudWatch already validated thresholds
        String alarmSeverity = metricsAnalyzer.getAlarmSeverity(metrics);
        var alarm = metrics.getAlarms().get(0);
        String alarmName = alarm.getName().toLowerCase();
        
        // Determine action based on alarm type and severity
        if (alarmName.contains("cpu") || alarmName.contains("memory")) {
            if (cluster.canScale()) {
                actions.add(RemediationAction.builder()
                    .type(RemediationAction.ActionType.SCALE_OUT)
                    .priority("CRITICAL".equals(alarmSeverity) ? RemediationAction.Priority.CRITICAL : RemediationAction.Priority.HIGH)
                    .reason("CloudWatch alarm: " + alarm.getName())
                    .targetNodes(cluster.getSpec().getNodeCount() + 1)
                    .ruleName(alarmName.contains("cpu") ? "cpu-alarm-scale-out" : "memory-alarm-scale-out")
                    .build());
            }
        } else if (alarmName.contains("latency")) {
            if ("CRITICAL".equals(alarmSeverity)) {
                actions.add(RemediationAction.builder()
                    .type(RemediationAction.ActionType.CREATE_NEW_CLUSTER)
                    .priority(RemediationAction.Priority.CRITICAL)
                    .reason("Critical latency alarm: " + alarm.getName())
                    .ruleName("critical-latency-alarm-new-cluster")
                    .build());
            } else if (cluster.canScale()) {
                actions.add(RemediationAction.builder()
                    .type(RemediationAction.ActionType.SCALE_OUT)
                    .priority(RemediationAction.Priority.HIGH)
                    .reason("Latency alarm: " + alarm.getName())
                    .targetNodes(cluster.getSpec().getNodeCount() + 1)
                    .ruleName("latency-alarm-scale-out")
                    .build());
            } else {
                actions.add(RemediationAction.builder()
                    .type(RemediationAction.ActionType.OPTIMIZE_CLUSTER)
                    .priority(RemediationAction.Priority.MEDIUM)
                    .reason("Latency alarm but cannot scale: " + alarm.getName())
                    .optimizations(List.of("refresh_interval", "merge_policy", "index_settings"))
                    .ruleName("latency-alarm-optimization")
                    .build());
            }
        } else if (alarmName.contains("disk")) {
            actions.add(RemediationAction.builder()
                .type(RemediationAction.ActionType.ALERT_CRITICAL)
                .priority(RemediationAction.Priority.CRITICAL)
                .reason("Disk space alarm: " + alarm.getName())
                .alertLevel(RemediationAction.AlertLevel.CRITICAL)
                .ruleName("disk-space-alert")
                .build());
        }
        
        // Emergency scaling based on individual alarm severity
        String alarmSeverity = metricsAnalyzer.getAlarmSeverity(metrics);
        if ("CRITICAL".equals(alarmSeverity) && cluster.canScale()) {
            actions.add(RemediationAction.builder()
                .type(RemediationAction.ActionType.EMERGENCY_SCALE)
                .priority(RemediationAction.Priority.IMMEDIATE)
                .reason("Critical alarm triggered: " + (metrics.getAlarms().get(0).getName()))
                .targetNodes(Math.min(cluster.getSpec().getNodeCount() * 2, cluster.getMaxNodes()))
                .ruleName("emergency-scale")
                .build());
        }
        

        
        // Note: Custom rules removed - CloudWatch alarms provide sufficient triggering logic
        
        return actions;
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
        // Store alarm information instead of assuming multiple metrics
        var alarm = metrics.getAlarms().get(0);
        String lastAlarmInfo = String.format("%s: %s (%s)", 
            alarm.getName(), alarm.getState(), alarm.getReason());
        
        if (cluster.getStatus() == null) {
            cluster.setStatus(OpenSearchCluster.ClusterStatus.builder().build());
        }
        
        // Update last alarm information instead of detailed metrics
        cluster.getStatus().setLastAlarmInfo(lastAlarmInfo);
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
    

    

    

    

} 