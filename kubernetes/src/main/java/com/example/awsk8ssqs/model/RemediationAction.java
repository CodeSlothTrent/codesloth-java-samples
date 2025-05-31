package com.example.awsk8ssqs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemediationAction {
    
    private ActionType type;
    private Priority priority;
    private String reason;
    private Integer targetNodes;
    private Duration delay;
    private String ruleName;
    private Map<String, Object> parameters;
    private List<String> optimizations;
    private AlertLevel alertLevel;
    private LocalDateTime scheduledTime;
    private boolean executed;
    private LocalDateTime executedTime;
    private boolean success;
    private String errorMessage;
    
    public enum ActionType {
        SCALE_OUT("Scale out cluster"),
        SCALE_IN("Scale in cluster"), 
        CREATE_NEW_CLUSTER("Create new cluster"),
        MIGRATE_WORKLOAD("Migrate workload"),
        OPTIMIZE_CLUSTER("Optimize cluster settings"),
        ALERT("Send alert"),
        EMERGENCY_SCALE("Emergency scaling"),
        PREEMPTIVE_SCALE("Preemptive scaling"),
        ALERT_CRITICAL("Send critical alert"),
        REBALANCE_CLUSTER("Rebalance cluster"),
        UPDATE_INSTANCE_TYPE("Update instance type"),
        INCREASE_STORAGE("Increase storage"),
        NO_ACTION("No action required");
        
        private final String description;
        
        ActionType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum Priority {
        CRITICAL(1, "Critical - Immediate action required"),
        HIGH(2, "High - Action required soon"),
        MEDIUM(3, "Medium - Action recommended"),
        LOW(4, "Low - Action optional"),
        IMMEDIATE(0, "Immediate - Emergency action");
        
        private final int level;
        private final String description;
        
        Priority(int level, String description) {
            this.level = level;
            this.description = description;
        }
        
        public int getLevel() {
            return level;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum AlertLevel {
        INFO("Information"),
        WARNING("Warning"),
        ERROR("Error"),
        CRITICAL("Critical");
        
        private final String description;
        
        AlertLevel(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Helper methods
    public boolean shouldExecuteNow() {
        return scheduledTime == null || LocalDateTime.now().isAfter(scheduledTime);
    }
    
    public boolean isHighPriority() {
        return priority == Priority.CRITICAL || priority == Priority.IMMEDIATE;
    }
    
    public boolean requiresClusterModification() {
        return type == ActionType.SCALE_OUT || 
               type == ActionType.SCALE_IN || 
               type == ActionType.CREATE_NEW_CLUSTER ||
               type == ActionType.UPDATE_INSTANCE_TYPE ||
               type == ActionType.INCREASE_STORAGE;
    }
} 