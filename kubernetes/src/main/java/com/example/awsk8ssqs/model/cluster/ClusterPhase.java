package com.example.awsk8ssqs.model.cluster;

public enum ClusterPhase {
    PENDING("Pending"),
    CREATING("Creating"),
    READY("Ready"),
    SCALING("Scaling"),
    ERROR("Error"),
    DELETING("Deleting");
    
    private final String value;
    
    ClusterPhase(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
} 