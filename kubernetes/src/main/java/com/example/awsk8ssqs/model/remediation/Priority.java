package com.example.awsk8ssqs.model.remediation;

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