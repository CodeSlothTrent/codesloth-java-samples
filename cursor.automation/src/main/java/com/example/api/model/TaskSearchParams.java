package com.example.api.model;

import java.time.LocalDateTime;

public record TaskSearchParams(
    String id,
    String title,
    String description,
    Boolean completed,
    LocalDateTime createdAtStart,
    LocalDateTime createdAtEnd,
    LocalDateTime updatedAtStart,
    LocalDateTime updatedAtEnd
) {
    // Records automatically generate:
    // - Constructor
    // - Getters (named after the fields)
    // - equals(), hashCode(), and toString() methods
} 