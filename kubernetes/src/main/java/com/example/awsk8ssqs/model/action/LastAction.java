package com.example.awsk8ssqs.model.action;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class LastAction {
    private String type;
    private String reason;
    private LocalDateTime timestamp;
    private Boolean success;
    private String message;
} 