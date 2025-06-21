package com.example.awsk8ssqs.model.cluster;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AutoScalingConfig {
    private Boolean enabled;
    private Integer minNodes;
    private Integer maxNodes;
    private String cooldownPeriod;
} 