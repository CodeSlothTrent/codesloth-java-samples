package com.example.awsk8ssqs.service;

import com.example.awsk8ssqs.model.RemediationAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {
    
    /**
     * Send an alert with specified level
     */
    public void sendAlert(String message, RemediationAction.AlertLevel level) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String formattedMessage = String.format("[%s] %s: %s", timestamp, level.getDescription(), message);
        
        switch (level) {
            case CRITICAL:
                sendCriticalAlert(formattedMessage);
                break;
            case ERROR:
                sendErrorAlert(formattedMessage);
                break;
            case WARNING:
                sendWarningAlert(formattedMessage);
                break;
            case INFO:
                sendInfoAlert(formattedMessage);
                break;
        }
        
        // Store alert in local history (in production, send to external systems)
        storeAlert(formattedMessage, level);
    }
    
    /**
     * Send critical alert - highest priority
     */
    private void sendCriticalAlert(String message) {
        log.error("🚨 CRITICAL ALERT: {}", message);
        
        // In production, this would:
        // - Send to PagerDuty/OpsGenie
        // - Send SMS to on-call engineers
        // - Post to Slack with @channel
        // - Send to monitoring systems (Datadog, New Relic, etc.)
        
        // For demo, just log prominently
        System.err.println("=".repeat(80));
        System.err.println("🚨 CRITICAL OPENSEARCH CLUSTER ALERT 🚨");
        System.err.println(message);
        System.err.println("=".repeat(80));
    }
    
    /**
     * Send error alert
     */
    private void sendErrorAlert(String message) {
        log.error("❌ ERROR ALERT: {}", message);
        
        // In production:
        // - Send to Slack
        // - Email to team
        // - Post to monitoring dashboard
        
        System.out.println("❌ OpenSearch Cluster Error: " + message);
    }
    
    /**
     * Send warning alert
     */
    private void sendWarningAlert(String message) {
        log.warn("⚠️ WARNING ALERT: {}", message);
        
        // In production:
        // - Send to Slack
        // - Add to monitoring dashboard
        
        System.out.println("⚠️ OpenSearch Cluster Warning: " + message);
    }
    
    /**
     * Send info alert
     */
    private void sendInfoAlert(String message) {
        log.info("ℹ️ INFO ALERT: {}", message);
        
        // In production:
        // - Send to monitoring systems
        // - Log to audit trail
        
        System.out.println("ℹ️ OpenSearch Cluster Info: " + message);
    }
    
    /**
     * Send alert about cluster scaling event
     */
    public void sendScalingAlert(String clusterName, int oldNodeCount, int newNodeCount, String reason) {
        String message = String.format(
            "OpenSearch cluster '%s' scaled from %d to %d nodes. Reason: %s",
            clusterName, oldNodeCount, newNodeCount, reason
        );
        
        RemediationAction.AlertLevel level = determineScalingAlertLevel(oldNodeCount, newNodeCount);
        sendAlert(message, level);
    }
    
    /**
     * Send alert about new cluster creation
     */
    public void sendClusterCreationAlert(String clusterName, String reason) {
        String message = String.format(
            "New OpenSearch cluster '%s' created. Reason: %s",
            clusterName, reason
        );
        
        sendAlert(message, RemediationAction.AlertLevel.WARNING);
    }
    
    /**
     * Send alert about cluster optimization
     */
    public void sendOptimizationAlert(String clusterName, java.util.List<String> optimizations) {
        String message = String.format(
            "OpenSearch cluster '%s' optimized with: %s",
            clusterName, String.join(", ", optimizations)
        );
        
        sendAlert(message, RemediationAction.AlertLevel.INFO);
    }
    
    /**
     * Send alert about metrics threshold breach
     */
    public void sendThresholdBreachAlert(String clusterName, String metric, double value, double threshold) {
        String message = String.format(
            "OpenSearch cluster '%s' %s %.1f%% exceeds threshold %.1f%%",
            clusterName, metric, value, threshold
        );
        
        RemediationAction.AlertLevel level = determineThresholdAlertLevel(value, threshold);
        sendAlert(message, level);
    }
    
    /**
     * Send alert about remediation action failure
     */
    public void sendActionFailureAlert(String clusterName, String actionType, String reason) {
        String message = String.format(
            "Failed to execute %s on OpenSearch cluster '%s'. Reason: %s",
            actionType, clusterName, reason
        );
        
        sendAlert(message, RemediationAction.AlertLevel.ERROR);
    }
    
    /**
     * Send health check alert
     */
    public void sendHealthAlert(String clusterName, String healthStatus, Map<String, Object> details) {
        String message = String.format(
            "OpenSearch cluster '%s' health: %s. Details: %s",
            clusterName, healthStatus, details
        );
        
        RemediationAction.AlertLevel level = healthStatus.equalsIgnoreCase("red") ? 
            RemediationAction.AlertLevel.CRITICAL : 
            healthStatus.equalsIgnoreCase("yellow") ? 
                RemediationAction.AlertLevel.WARNING : 
                RemediationAction.AlertLevel.INFO;
                
        sendAlert(message, level);
    }
    
    /**
     * Store alert in history for auditing
     */
    private void storeAlert(String message, RemediationAction.AlertLevel level) {
        // In production, store in:
        // - Database for audit trail
        // - Time-series database for metrics
        // - External logging system (ELK, Splunk, etc.)
        
        log.debug("Stored alert: [{}] {}", level, message);
    }
    
    /**
     * Determine appropriate alert level for scaling events
     */
    private RemediationAction.AlertLevel determineScalingAlertLevel(int oldCount, int newCount) {
        int difference = Math.abs(newCount - oldCount);
        double percentageChange = (double) difference / oldCount * 100;
        
        if (percentageChange > 100) {
            return RemediationAction.AlertLevel.CRITICAL; // Doubling or more
        } else if (percentageChange > 50) {
            return RemediationAction.AlertLevel.ERROR; // Major scaling
        } else if (percentageChange > 25) {
            return RemediationAction.AlertLevel.WARNING; // Moderate scaling
        } else {
            return RemediationAction.AlertLevel.INFO; // Minor scaling
        }
    }
    
    /**
     * Determine appropriate alert level for threshold breaches
     */
    private RemediationAction.AlertLevel determineThresholdAlertLevel(double value, double threshold) {
        double overage = (value - threshold) / threshold * 100;
        
        if (overage > 50) {
            return RemediationAction.AlertLevel.CRITICAL; // 50% over threshold
        } else if (overage > 25) {
            return RemediationAction.AlertLevel.ERROR; // 25% over threshold
        } else if (overage > 10) {
            return RemediationAction.AlertLevel.WARNING; // 10% over threshold
        } else {
            return RemediationAction.AlertLevel.INFO; // Just over threshold
        }
    }
    
    /**
     * Send summary alert about multiple actions taken
     */
    public void sendActionSummaryAlert(String clusterName, java.util.List<RemediationAction> actions) {
        if (actions.isEmpty()) {
            return;
        }
        
        long successfulActions = actions.stream().filter(RemediationAction::isSuccess).count();
        long failedActions = actions.size() - successfulActions;
        
        String message = String.format(
            "OpenSearch cluster '%s' remediation summary: %d successful, %d failed actions. " +
            "Actions taken: %s",
            clusterName, 
            successfulActions, 
            failedActions,
            actions.stream()
                .map(a -> a.getType().name())
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("none")
        );
        
        RemediationAction.AlertLevel level = failedActions > 0 ? 
            RemediationAction.AlertLevel.WARNING : 
            RemediationAction.AlertLevel.INFO;
            
        sendAlert(message, level);
    }
    
    /**
     * Test method to verify alert system is working
     */
    public void sendTestAlert() {
        sendAlert("OpenSearch Controller Alert System Test", RemediationAction.AlertLevel.INFO);
    }
} 