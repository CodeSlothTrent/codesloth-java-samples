package com.example.awsk8ssqs.model.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ObjectMeta {
    private String name;
    private String namespace;
    private Map<String, String> labels;
    private Map<String, String> annotations;
    private LocalDateTime creationTimestamp;
} 