package com.example.awsk8ssqs.model.cluster;

import com.example.awsk8ssqs.model.action.LastAction;
import com.example.awsk8ssqs.model.condition.ClusterCondition;
import com.example.awsk8ssqs.model.metrics.CurrentMetrics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ClusterStatus {
    private ClusterPhase phase;
    private Integer nodeCount;
    private String endpoint;
    private CurrentMetrics currentMetrics;
    private LastAction lastAction;
    private List<ClusterCondition> conditions;
    private LocalDateTime lastUpdated;
    
    // Additional method for alarm info (for compatibility)
    public void setLastAlarmInfo(String alarmInfo) {
        // This could be stored in a separate field or as part of conditions
        // For now, we'll just log it or ignore it
    }
} 