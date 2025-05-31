package com.example.awsk8ssqs.service;

import com.example.awsk8ssqs.model.CloudWatchMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MetricsAnalyzer {
    
    // Store recent metrics for trend analysis
    private final Map<String, Map<LocalDateTime, CloudWatchMetrics>> metricsHistory = new ConcurrentHashMap<>();
    
    /**
     * Store metrics for historical comparison and debugging
     * Note: Not used for trend analysis since CloudWatch alarms handle that
     */
    public void storeMetrics(String clusterName, CloudWatchMetrics metrics) {
        metricsHistory.computeIfAbsent(clusterName, k -> new ConcurrentHashMap<>())
                     .put(LocalDateTime.now(), metrics);
        
        // Keep only recent metrics (last 2 hours) for comparison and debugging
        cleanOldMetrics(clusterName);
    }
    

    
    /**
     * Check if the single CloudWatch alarm indicates stress
     * Note: Each SQS message typically contains one alarm state transition
     */
    public boolean isUnderStress(CloudWatchMetrics metrics) {
        // Since each message represents one alarm, check if this specific alarm indicates stress
        if (metrics.getAlarms() == null || metrics.getAlarms().isEmpty()) {
            return false;
        }
        
        // Get the primary alarm (should be only one per message)
        var alarm = metrics.getAlarms().get(0);
        return "ALARM".equals(alarm.getState()) && 
               (alarm.getName().toLowerCase().contains("critical") || 
                alarm.getName().toLowerCase().contains("high"));
    }
    
    /**
     * Get alarm severity for the single alarm in this message
     * Note: Each SQS message represents one alarm state transition
     */
    public String getAlarmSeverity(CloudWatchMetrics metrics) {
        if (metrics.getAlarms() == null || metrics.getAlarms().isEmpty()) {
            return "NONE";
        }
        
        // Get the primary alarm (should be only one per message)
        var alarm = metrics.getAlarms().get(0);
        
        if (!"ALARM".equals(alarm.getState())) {
            return "OK"; // Alarm is not active
        }
        
        // Determine severity from alarm name
        String alarmName = alarm.getName().toLowerCase();
        if (alarmName.contains("critical")) {
            return "CRITICAL";
        } else if (alarmName.contains("high") || alarmName.contains("error")) {
            return "HIGH";
        } else if (alarmName.contains("warning")) {
            return "WARNING";
        } else {
            return "MEDIUM";
        }
    }
    
    /**
     * Get metrics comparison with previous measurement
     */
    public Map<String, Double> getMetricsComparison(String clusterName, CloudWatchMetrics currentMetrics) {
        Map<String, Double> comparison = new HashMap<>();
        
        Map<LocalDateTime, CloudWatchMetrics> clusterHistory = metricsHistory.get(clusterName);
        if (clusterHistory == null || clusterHistory.size() < 2) {
            return comparison; // No comparison possible
        }
        
        // Get previous metrics
        var previousEntry = clusterHistory.entrySet().stream()
            .sorted(Map.Entry.<LocalDateTime, CloudWatchMetrics>comparingByKey().reversed())
            .skip(1) // Skip current (most recent)
            .findFirst();
            
        if (previousEntry.isEmpty()) {
            return comparison;
        }
        
        CloudWatchMetrics previousMetrics = previousEntry.get().getValue();
        
        // Compare CPU
        if (currentMetrics.getMetrics().getCpu() != null && previousMetrics.getMetrics().getCpu() != null) {
            double currentCpu = currentMetrics.getMetrics().getCpu().getAverage();
            double previousCpu = previousMetrics.getMetrics().getCpu().getAverage();
            comparison.put("cpu_change", currentCpu - previousCpu);
            comparison.put("cpu_change_percent", ((currentCpu - previousCpu) / previousCpu) * 100);
        }
        
        // Compare Memory
        if (currentMetrics.getMetrics().getMemory() != null && previousMetrics.getMetrics().getMemory() != null) {
            double currentMemory = currentMetrics.getMetrics().getMemory().getAverage();
            double previousMemory = previousMetrics.getMetrics().getMemory().getAverage();
            comparison.put("memory_change", currentMemory - previousMemory);
            comparison.put("memory_change_percent", ((currentMemory - previousMemory) / previousMemory) * 100);
        }
        
        // Compare Latency
        if (currentMetrics.getMetrics().getSearchLatency() != null && previousMetrics.getMetrics().getSearchLatency() != null) {
            double currentLatency = currentMetrics.getMetrics().getSearchLatency().getP95();
            double previousLatency = previousMetrics.getMetrics().getSearchLatency().getP95();
            comparison.put("latency_change", currentLatency - previousLatency);
            comparison.put("latency_change_percent", ((currentLatency - previousLatency) / previousLatency) * 100);
        }
        
        return comparison;
    }
    
    /**
     * Check if this specific CloudWatch alarm indicates anomalous behavior
     * Note: Each message contains one alarm - check if it's an anomaly detector alarm
     */
    public boolean hasAnomalies(String clusterName, CloudWatchMetrics metrics) {
        if (metrics.getAlarms() == null || metrics.getAlarms().isEmpty()) {
            return false;
        }
        
        // Check the single alarm in this message
        var alarm = metrics.getAlarms().get(0);
        return alarm.getName().toLowerCase().contains("anomaly") ||
               "ANOMALY_DETECTOR".equals(alarm.getComparisonOperator());
    }
    
    /**
     * Get peak usage times analysis
     */
    public Map<String, Object> getPeakUsageAnalysis(String clusterName) {
        Map<String, Object> analysis = new HashMap<>();
        Map<LocalDateTime, CloudWatchMetrics> clusterHistory = metricsHistory.get(clusterName);
        
        if (clusterHistory == null || clusterHistory.isEmpty()) {
            return analysis;
        }
        
        // Find peak CPU usage
        var peakCpu = clusterHistory.entrySet().stream()
            .max((e1, e2) -> {
                double cpu1 = e1.getValue().getMetrics().getCpu() != null ? 
                    e1.getValue().getMetrics().getCpu().getAverage() : 0;
                double cpu2 = e2.getValue().getMetrics().getCpu() != null ? 
                    e2.getValue().getMetrics().getCpu().getAverage() : 0;
                return Double.compare(cpu1, cpu2);
            });
            
        if (peakCpu.isPresent()) {
            analysis.put("peak_cpu_time", peakCpu.get().getKey());
            analysis.put("peak_cpu_value", peakCpu.get().getValue().getMetrics().getCpu().getAverage());
        }
        
        // Find peak memory usage
        var peakMemory = clusterHistory.entrySet().stream()
            .max((e1, e2) -> {
                double mem1 = e1.getValue().getMetrics().getMemory() != null ? 
                    e1.getValue().getMetrics().getMemory().getAverage() : 0;
                double mem2 = e2.getValue().getMetrics().getMemory() != null ? 
                    e2.getValue().getMetrics().getMemory().getAverage() : 0;
                return Double.compare(mem1, mem2);
            });
            
        if (peakMemory.isPresent()) {
            analysis.put("peak_memory_time", peakMemory.get().getKey());
            analysis.put("peak_memory_value", peakMemory.get().getValue().getMetrics().getMemory().getAverage());
        }
        
        return analysis;
    }
    

    
    /**
     * Clean old metrics to prevent memory leaks
     */
    private void cleanOldMetrics(String clusterName) {
        Map<LocalDateTime, CloudWatchMetrics> clusterHistory = metricsHistory.get(clusterName);
        if (clusterHistory == null) {
            return;
        }
        
        LocalDateTime cutoff = LocalDateTime.now().minusHours(2);
        clusterHistory.entrySet().removeIf(entry -> entry.getKey().isBefore(cutoff));
        
        // Remove cluster history if empty
        if (clusterHistory.isEmpty()) {
            metricsHistory.remove(clusterName);
        }
    }
    
    /**
     * Get all metrics history for a cluster (for debugging/monitoring)
     */
    public Map<LocalDateTime, CloudWatchMetrics> getMetricsHistory(String clusterName) {
        return Map.copyOf(metricsHistory.getOrDefault(clusterName, Map.of()));
    }
    
    /**
     * Clear all metrics history
     */
    public void clearHistory() {
        metricsHistory.clear();
        log.info("Cleared all metrics history");
    }
    
    /**
     * Clear metrics history for specific cluster
     */
    public void clearClusterHistory(String clusterName) {
        metricsHistory.remove(clusterName);
        log.info("Cleared metrics history for cluster: {}", clusterName);
    }
} 