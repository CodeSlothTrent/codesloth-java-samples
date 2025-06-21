package com.example.awsk8ssqs.model.cluster;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ClusterThresholds {
    private Double cpuHigh;
    private Double cpuLow;
    private Double memoryHigh;
    private Double memoryLow;
    private Double diskHigh;
    private Double latencyHigh;
    private Double queryRateHigh;
} 