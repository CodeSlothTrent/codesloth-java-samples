package com.example.awsk8ssqs.model.condition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ClusterCondition {
    private String type;
    private String status;
    private LocalDateTime lastTransitionTime;
    private String reason;
    private String message;
} 