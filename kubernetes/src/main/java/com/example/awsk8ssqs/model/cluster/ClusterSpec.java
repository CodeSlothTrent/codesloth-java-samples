package com.example.awsk8ssqs.model.cluster;

import com.example.awsk8ssqs.model.remediation.RemediationRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ClusterSpec {
    private String clusterName;
    private Integer nodeCount;
    private String version;
    private String instanceType;
    private ClusterThresholds thresholds;
    private AutoScalingConfig autoScaling;
    private List<RemediationRule> remediationRules;
} 