package com.example.awsk8ssqs.model.remediation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder(toBuilder = true)
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