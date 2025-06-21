package com.example.awsk8ssqs.model.remediation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RemediationRule {
    private String name;
    private String condition;
    private String action;
    private String cooldown;
    private String priority;
} 