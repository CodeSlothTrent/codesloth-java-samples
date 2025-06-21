package com.example.awsk8ssqs.model.remediation;

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