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
     * Store metrics for trend analysis
     */
    public void storeMetrics(String clusterName, CloudWatchMetrics metrics) {
        metricsHistory.computeIfAbsent(clusterName, k -> new ConcurrentHashMap<>())
                     .put(LocalDateTime.now(), metrics);
        
        // Keep only recent metrics (last 2 hours)
        cleanOldMetrics(clusterName);
    }
    
    /**
     * Analyze if metrics show an upward trend
     */
    public boolean isUpwardTrend(String clusterName, String metricType) {
        Map<LocalDateTime, CloudWatchMetrics> clusterHistory = metricsHistory.get(clusterName);
        if (clusterHistory == null || clusterHistory.size() < 3) {
            return false; // Need at least 3 data points
        }
        
        // Get last 3 metrics
        var recentMetrics = clusterHistory.entrySet().stream()
            .sorted(Map.Entry.<LocalDateTime, CloudWatchMetrics>comparingByKey().reversed())
            .limit(3)
            .toList();
        
        if (recentMetrics.size() < 3) {
            return false;
        }
        
        // Check if metric is trending upward
        double[] values = new double[3];
        for (int i = 0; i < 3; i++) {
            values[i] = extractMetricValue(recentMetrics.get(i).getValue(), metricType);
        }
        
        // Trend analysis: check if values are consistently increasing
        return values[0] > values[1] && values[1] > values[2];
    }
    
    /**
     * Check if metrics indicate cluster is under stress
     */
    public boolean isUnderStress(CloudWatchMetrics metrics) {
        var metricsData = metrics.getMetrics();
        int stressIndicators = 0;
        
        // CPU stress
        if (metricsData.getCpu() != null && metricsData.getCpu().getAverage() > 75) {
            stressIndicators++;
        }
        
        // Memory stress
        if (metricsData.getMemory() != null && metricsData.getMemory().getAverage() > 80) {
            stressIndicators++;
        }
        
        // Latency stress
        if (metricsData.getSearchLatency() != null && metricsData.getSearchLatency().getP95() > 500) {
            stressIndicators++;
        }
        
        // Disk stress
        if (metricsData.getDisk() != null && metricsData.getDisk().getAverage() > 85) {
            stressIndicators++;
        }
        
        // Error rate stress
        if (metricsData.getErrorRate() != null && metricsData.getErrorRate().getAverage() > 2.0) {
            stressIndicators++;
        }
        
        // Under stress if 2 or more indicators
        return stressIndicators >= 2;
    }
    
    /**
     * Calculate composite health score based on multiple metrics
     */
    public double calculateHealthScore(CloudWatchMetrics metrics) {
        var metricsData = metrics.getMetrics();
        double score = 100.0;
        
        // CPU impact (max 25 points)
        if (metricsData.getCpu() != null) {
            double cpuUsage = metricsData.getCpu().getAverage();
            if (cpuUsage > 50) {
                score -= Math.min(25, (cpuUsage - 50) / 50 * 25);
            }
        }
        
        // Memory impact (max 25 points)
        if (metricsData.getMemory() != null) {
            double memoryUsage = metricsData.getMemory().getAverage();
            if (memoryUsage > 60) {
                score -= Math.min(25, (memoryUsage - 60) / 40 * 25);
            }
        }
        
        // Latency impact (max 25 points)
        if (metricsData.getSearchLatency() != null) {
            double latency = metricsData.getSearchLatency().getP95();
            if (latency > 100) {
                score -= Math.min(25, (latency - 100) / 400 * 25);
            }
        }
        
        // Disk impact (max 15 points)
        if (metricsData.getDisk() != null) {
            double diskUsage = metricsData.getDisk().getAverage();
            if (diskUsage > 70) {
                score -= Math.min(15, (diskUsage - 70) / 30 * 15);
            }
        }
        
        // Error rate impact (max 10 points)
        if (metricsData.getErrorRate() != null) {
            double errorRate = metricsData.getErrorRate().getAverage();
            score -= Math.min(10, errorRate * 2);
        }
        
        return Math.max(0, score);
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
     * Detect anomalies in metrics
     */
    public boolean hasAnomalies(String clusterName, CloudWatchMetrics metrics) {
        Map<LocalDateTime, CloudWatchMetrics> clusterHistory = metricsHistory.get(clusterName);
        if (clusterHistory == null || clusterHistory.size() < 5) {
            return false; // Need historical data for anomaly detection
        }
        
        // Anomaly detection: identify values that deviate significantly from historical average
        var recentMetrics = clusterHistory.values().stream()
            .limit(10) // Last 10 measurements
            .toList();
            
        // Calculate averages
        double avgCpu = recentMetrics.stream()
            .mapToDouble(m -> m.getMetrics().getCpu() != null ? m.getMetrics().getCpu().getAverage() : 0)
            .average().orElse(0);
            
        double avgMemory = recentMetrics.stream()
            .mapToDouble(m -> m.getMetrics().getMemory() != null ? m.getMetrics().getMemory().getAverage() : 0)
            .average().orElse(0);
            
        double avgLatency = recentMetrics.stream()
            .mapToDouble(m -> m.getMetrics().getSearchLatency() != null ? m.getMetrics().getSearchLatency().getP95() : 0)
            .average().orElse(0);
        
        // Check for anomalies (more than 50% deviation from average)
        boolean cpuAnomaly = metrics.getMetrics().getCpu() != null && 
            Math.abs(metrics.getMetrics().getCpu().getAverage() - avgCpu) > avgCpu * 0.5;
            
        boolean memoryAnomaly = metrics.getMetrics().getMemory() != null && 
            Math.abs(metrics.getMetrics().getMemory().getAverage() - avgMemory) > avgMemory * 0.5;
            
        boolean latencyAnomaly = metrics.getMetrics().getSearchLatency() != null && 
            Math.abs(metrics.getMetrics().getSearchLatency().getP95() - avgLatency) > avgLatency * 0.5;
        
        return cpuAnomaly || memoryAnomaly || latencyAnomaly;
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
     * Extract specific metric value from CloudWatch metrics
     */
    private double extractMetricValue(CloudWatchMetrics metrics, String metricType) {
        var metricsData = metrics.getMetrics();
        
        return switch (metricType.toLowerCase()) {
            case "cpu" -> metricsData.getCpu() != null ? metricsData.getCpu().getAverage() : 0;
            case "memory" -> metricsData.getMemory() != null ? metricsData.getMemory().getAverage() : 0;
            case "latency" -> metricsData.getSearchLatency() != null ? metricsData.getSearchLatency().getP95() : 0;
            case "disk" -> metricsData.getDisk() != null ? metricsData.getDisk().getAverage() : 0;
            case "queryrate" -> metricsData.getQueryRate() != null ? metricsData.getQueryRate().getAverage() : 0;
            default -> 0;
        };
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