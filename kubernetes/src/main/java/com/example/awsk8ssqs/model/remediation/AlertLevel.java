package com.example.awsk8ssqs.model.remediation;

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