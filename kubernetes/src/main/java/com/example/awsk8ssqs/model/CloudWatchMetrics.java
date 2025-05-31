package com.example.awsk8ssqs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudWatchMetrics {
    
    @JsonProperty("messageType")
    private String messageType;
    
    @JsonProperty("clusterName")
    private String clusterName;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("metrics")
    private MetricsData metrics;
    
    @JsonProperty("alarms")
    private List<AlarmData> alarms;
    
    @JsonProperty("additionalContext")
    private Map<String, Object> additionalContext;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricsData {
        
        @JsonProperty("cpu")
        private MetricValue cpu;
        
        @JsonProperty("memory")
        private MetricValue memory;
        
        @JsonProperty("disk")
        private MetricValue disk;
        
        @JsonProperty("searchLatency")
        private LatencyMetric searchLatency;
        
        @JsonProperty("indexingLatency")
        private LatencyMetric indexingLatency;
        
        @JsonProperty("indexingRate")
        private MetricValue indexingRate;
        
        @JsonProperty("queryRate")
        private MetricValue queryRate;
        
        @JsonProperty("errorRate")
        private MetricValue errorRate;
        
        @JsonProperty("diskIOPS")
        private MetricValue diskIOPS;
        
        @JsonProperty("networkIn")
        private MetricValue networkIn;
        
        @JsonProperty("networkOut")
        private MetricValue networkOut;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricValue {
        
        @JsonProperty("average")
        private Double average;
        
        @JsonProperty("maximum")
        private Double maximum;
        
        @JsonProperty("minimum")
        private Double minimum;
        
        @JsonProperty("period")
        private String period;
        
        @JsonProperty("unit")
        private String unit;
        
        @JsonProperty("datapoints")
        private Integer datapoints;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LatencyMetric {
        
        @JsonProperty("average")
        private Double average;
        
        @JsonProperty("p50")
        private Double p50;
        
        @JsonProperty("p95")
        private Double p95;
        
        @JsonProperty("p99")
        private Double p99;
        
        @JsonProperty("maximum")
        private Double maximum;
        
        @JsonProperty("period")
        private String period;
        
        @JsonProperty("unit")
        private String unit = "milliseconds";
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlarmData {
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("state")
        private String state; // ALARM, OK, INSUFFICIENT_DATA
        
        @JsonProperty("threshold")
        private Double threshold;
        
        @JsonProperty("value")
        private Double value;
        
        @JsonProperty("comparisonOperator")
        private String comparisonOperator; // GreaterThanThreshold, LessThanThreshold, etc.
        
        @JsonProperty("evaluationPeriods")
        private Integer evaluationPeriods;
        
        @JsonProperty("datapointsToAlarm")
        private Integer datapointsToAlarm;
        
        @JsonProperty("reason")
        private String reason;
    }
} 