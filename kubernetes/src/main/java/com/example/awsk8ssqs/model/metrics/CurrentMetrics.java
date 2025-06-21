package com.example.awsk8ssqs.model.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CurrentMetrics {
    private Double cpu;
    private Double memory;
    private Double disk;
    private Double latency;
    private Double queryRate;
    private LocalDateTime lastUpdated;
} 