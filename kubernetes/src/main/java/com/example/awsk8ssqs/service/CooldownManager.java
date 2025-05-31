package com.example.awsk8ssqs.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class CooldownManager {
    
    // Map: clusterName -> ruleName -> lastExecutionTime
    private final Map<String, Map<String, LocalDateTime>> cooldownTracker = new ConcurrentHashMap<>();
    
    // Default cooldown periods for different action types
    private static final Map<String, Duration> DEFAULT_COOLDOWNS = Map.of(
        "scale_out", Duration.ofMinutes(10),
        "scale_in", Duration.ofMinutes(30),
        "emergency_scale", Duration.ofMinutes(5),
        "create_new_cluster", Duration.ofMinutes(60),
        "optimize_cluster", Duration.ofMinutes(15),
        "alert", Duration.ofMinutes(5),
        "alert_critical", Duration.ofMinutes(2)
    );
    
    /**
     * Check if an action can be executed based on cooldown period
     */
    public boolean canExecuteAction(String clusterName, String ruleName) {
        Map<String, LocalDateTime> clusterCooldowns = cooldownTracker.get(clusterName);
        if (clusterCooldowns == null) {
            return true;
        }
        
        LocalDateTime lastExecution = clusterCooldowns.get(ruleName);
        if (lastExecution == null) {
            return true;
        }
        
        Duration cooldownPeriod = getCooldownPeriod(ruleName);
        LocalDateTime nextAllowedTime = lastExecution.plus(cooldownPeriod);
        
        boolean canExecute = LocalDateTime.now().isAfter(nextAllowedTime);
        
        if (!canExecute) {
            log.debug("Action {} for cluster {} is in cooldown until {}", 
                ruleName, clusterName, nextAllowedTime);
        }
        
        return canExecute;
    }
    
    /**
     * Record that an action was executed
     */
    public void recordAction(String clusterName, String ruleName) {
        cooldownTracker.computeIfAbsent(clusterName, k -> new ConcurrentHashMap<>())
                     .put(ruleName, LocalDateTime.now());
        
        log.debug("Recorded action {} for cluster {} at {}", 
            ruleName, clusterName, LocalDateTime.now());
    }
    
    /**
     * Get the cooldown period for a specific rule
     */
    private Duration getCooldownPeriod(String ruleName) {
        // Extract action type from rule name (e.g., "cpu-scale-out" -> "scale_out")
        String actionType = extractActionType(ruleName);
        return DEFAULT_COOLDOWNS.getOrDefault(actionType, Duration.ofMinutes(10));
    }
    
    /**
     * Extract action type from rule name for cooldown lookup
     */
    private String extractActionType(String ruleName) {
        if (ruleName == null) {
            return "default";
        }
        
        // Map rule names to action types
        String lowerRuleName = ruleName.toLowerCase();
        
        if (lowerRuleName.contains("scale-out") || lowerRuleName.contains("scale_out")) {
            return "scale_out";
        } else if (lowerRuleName.contains("scale-in") || lowerRuleName.contains("scale_in")) {
            return "scale_in";
        } else if (lowerRuleName.contains("emergency")) {
            return "emergency_scale";
        } else if (lowerRuleName.contains("new-cluster") || lowerRuleName.contains("new_cluster")) {
            return "create_new_cluster";
        } else if (lowerRuleName.contains("optim")) {
            return "optimize_cluster";
        } else if (lowerRuleName.contains("critical")) {
            return "alert_critical";
        } else if (lowerRuleName.contains("alert")) {
            return "alert";
        }
        
        return "default";
    }
    
    /**
     * Get time until next allowed execution for a rule
     */
    public Duration getTimeUntilNextExecution(String clusterName, String ruleName) {
        Map<String, LocalDateTime> clusterCooldowns = cooldownTracker.get(clusterName);
        if (clusterCooldowns == null) {
            return Duration.ZERO;
        }
        
        LocalDateTime lastExecution = clusterCooldowns.get(ruleName);
        if (lastExecution == null) {
            return Duration.ZERO;
        }
        
        Duration cooldownPeriod = getCooldownPeriod(ruleName);
        LocalDateTime nextAllowedTime = lastExecution.plus(cooldownPeriod);
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isAfter(nextAllowedTime)) {
            return Duration.ZERO;
        }
        
        return Duration.between(now, nextAllowedTime);
    }
    
    /**
     * Clear cooldown for a specific rule (useful for testing or emergency situations)
     */
    public void clearCooldown(String clusterName, String ruleName) {
        Map<String, LocalDateTime> clusterCooldowns = cooldownTracker.get(clusterName);
        if (clusterCooldowns != null) {
            clusterCooldowns.remove(ruleName);
            log.info("Cleared cooldown for rule {} on cluster {}", ruleName, clusterName);
        }
    }
    
    /**
     * Clear all cooldowns for a cluster (useful when cluster is deleted)
     */
    public void clearAllCooldowns(String clusterName) {
        cooldownTracker.remove(clusterName);
        log.info("Cleared all cooldowns for cluster {}", clusterName);
    }
    
    /**
     * Get all active cooldowns for monitoring/debugging
     */
    public Map<String, Map<String, LocalDateTime>> getAllCooldowns() {
        return Map.copyOf(cooldownTracker);
    }
    
    /**
     * Check if any actions are currently in cooldown for a cluster
     */
    public boolean hasActiveCooldowns(String clusterName) {
        Map<String, LocalDateTime> clusterCooldowns = cooldownTracker.get(clusterName);
        if (clusterCooldowns == null || clusterCooldowns.isEmpty()) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        return clusterCooldowns.entrySet().stream()
            .anyMatch(entry -> {
                String ruleName = entry.getKey();
                LocalDateTime lastExecution = entry.getValue();
                Duration cooldownPeriod = getCooldownPeriod(ruleName);
                return now.isBefore(lastExecution.plus(cooldownPeriod));
            });
    }
    
    /**
     * Force an action to execute by temporarily clearing its cooldown
     */
    public void forceExecuteAction(String clusterName, String ruleName) {
        clearCooldown(clusterName, ruleName);
        log.warn("FORCED execution allowed for rule {} on cluster {} - cooldown bypassed", 
            ruleName, clusterName);
    }
} 