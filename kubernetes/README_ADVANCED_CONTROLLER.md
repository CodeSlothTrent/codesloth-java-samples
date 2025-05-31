# Advanced OpenSearch Cluster Controller - Metrics-Based Auto-Remediation

This project demonstrates an **advanced Kubernetes controller pattern** that automatically manages OpenSearch clusters in AWS LocalStack based on **CloudWatch metrics** and **intelligent remediation strategies**. This mimics production-grade cluster management systems used by companies like Netflix, Uber, and AWS.

## 🏗️ Architecture Overview

```
┌─────────────────────┐    ┌─────────────────────┐    ┌─────────────────────┐
│   CloudWatch        │    │   Kubernetes        │    │   LocalStack        │
│   Metrics           │────▶│   Controller        │────▶│   OpenSearch        │
│   (SQS Messages)    │    │   (Auto-Remediation)│    │   Clusters          │
│                     │    │                     │    │                     │
│ • CPU Usage         │    │ • Metrics Analysis  │    │ • Managed Domains   │
│ • Memory Usage      │    │ • Threshold Checks  │    │ • Auto-Scaling      │
│ • Search Latency    │    │ • Action Planning   │    │ • Optimization      │
│ • Query Rate        │    │ • Cooldown Mgmt     │    │ • Health Monitoring │
│ • Error Rate        │    │ • Alert System     │    │                     │
└─────────────────────┘    └─────────────────────┘    └─────────────────────┘
```

## ⚡ Kubernetes API Integration

This controller uses **real Kubernetes API** and **etcd** for cluster state management instead of simple in-memory storage:

### **Production-Grade Storage**
```java
// ❌ Old: Simple HashMap (demo only)
private final Map<String, OpenSearchCluster> clusters = new HashMap<>();

// ✅ New: Kubernetes API + etcd (production ready)
private final KubernetesClusterService kubernetesClusterService;
```

### **How It Works**
1. **CloudWatch metrics** arrive via SQS
2. **Controller queries Kubernetes API** for cluster definitions
3. **API Server reads from etcd** (distributed database)
4. **Controller analyzes metrics** and plans remediation
5. **Updates stored back to etcd** via Kubernetes API

### **Benefits**
- ✅ **Persistent storage** (survives restarts)
- ✅ **Multiple controllers** can share same state
- ✅ **High availability** with etcd clustering
- ✅ **Full audit trail** of all changes
- ✅ **RBAC integration** for security
- ✅ **Real-time notifications** via Kubernetes Watch API

📚 **Learn more**: See [KUBERNETES_API_INTEGRATION.md](KUBERNETES_API_INTEGRATION.md) for detailed explanation.

## 🚀 Key Features

### **Intelligent Auto-Scaling**
- **Proactive scaling** based on metrics trends
- **Multi-factor analysis** (CPU, memory, latency, query rate)
- **Cooldown periods** to prevent oscillation
- **Emergency scaling** for critical situations

### **Advanced Remediation Actions**
- **Scale Out/In**: Adjust node count based on load
- **Create New Clusters**: For extreme load or failover
- **Optimize Performance**: Tune cluster settings
- **Critical Alerts**: Notify operations team
- **Health Monitoring**: Continuous cluster assessment

### **Production-Ready Features**
- **Custom Resource Definitions (CRDs)** for Kubernetes-native management
- **Sophisticated alerting system** with multiple levels
- **Metrics history and trend analysis**
- **Anomaly detection** capabilities
- **Comprehensive audit trails**

## 📁 Project Structure

```
kubernetes/
├── k8s/
│   ├── elasticsearch-cluster-crd.yaml      # Custom Resource Definition
│   ├── elasticsearch-cluster-example.yaml  # Example cluster configs
│   ├── namespace.yaml                       # Kubernetes namespace
│   └── deployment.yaml                      # Controller deployment
├── src/main/java/com/example/awsk8ssqs/
│   ├── model/
│   │   ├── CloudWatchMetrics.java          # Metrics data model
│   │   ├── RemediationAction.java          # Action definitions
│   │   └── OpenSearchCluster.java          # Cluster CRD model
│   └── service/
│       ├── OpenSearchClusterController.java # Main controller logic
│       ├── RemediationStrategy.java         # Action planning
│       ├── CooldownManager.java            # Prevent oscillation
│       ├── AlertService.java               # Notifications
│       ├── MetricsAnalyzer.java            # Trend analysis
│       └── OpenSearchService.java          # AWS integration
└── scripts/
    └── setup-demo.sh                       # Automated setup
```

## 🛠️ Quick Start

### 1. **Setup Kubernetes API Integration** ⭐
```bash
# Windows PowerShell
cd kubernetes
.\scripts\setup-kubernetes-local.ps1

# This will:
# - Verify kubectl and Kubernetes cluster access
# - Apply the OpenSearchCluster Custom Resource Definition (CRD)
# - Test cluster resource creation/deletion
# - Verify RBAC permissions
```

### 2. **Deploy the Controller**
```bash
cd kubernetes
chmod +x scripts/setup-demo.sh
./scripts/setup-demo.sh
```

### 3. **Create OpenSearch Cluster Definitions**
```bash
# Apply the Custom Resource Definition (if not done above)
kubectl apply -f k8s/elasticsearch-cluster-crd.yaml

# Create production cluster with auto-scaling
kubectl apply -f k8s/elasticsearch-cluster-example.yaml
```

### 4. **Send CloudWatch Metrics**
```bash
# Example high CPU metrics triggering scale-out
awslocal sqs send-message --queue-url http://localhost:4566/000000000000/cluster-metrics --message-body '{
  "messageType": "cloudwatch-metrics",
  "clusterName": "production-es-cluster",
  "timestamp": "2024-01-15T10:30:00Z",
  "metrics": {
    "cpu": {"average": 85.5, "maximum": 92.1, "period": "5m"},
    "memory": {"average": 78.3, "maximum": 89.7, "period": "5m"},
    "searchLatency": {"average": 145.2, "p95": 287.5, "period": "5m"},
    "queryRate": {"average": 120, "period": "5m"}
  },
  "alarms": [{
    "name": "HighCPUUtilization",
    "state": "ALARM",
    "threshold": 80,
    "value": 85.5
  }]
}'
```

## 🎯 Remediation Strategies

### **Health-Based Action Plans**

#### **🚨 CRITICAL (Health Score < 60)**
```yaml
actions:
  - type: EMERGENCY_SCALE
    priority: IMMEDIATE
    target_nodes: "double current nodes"
    
  - type: CREATE_NEW_CLUSTER
    priority: CRITICAL
    reason: "Backup cluster for failover"
    
  - type: ALERT_CRITICAL
    priority: IMMEDIATE
    channels: ["pagerduty", "slack", "sms"]
```

#### **⚠️ WARNING (Health Score 60-80)**
```yaml
actions:
  - type: SCALE_OUT
    priority: HIGH
    target_nodes: "+1 node"
    
  - type: OPTIMIZE_CLUSTER
    priority: MEDIUM
    optimizations: ["query_cache", "refresh_interval"]
    
  - type: ALERT
    priority: HIGH
    channels: ["slack", "email"]
```

#### **🔧 OPTIMIZATION (Health Score 80-95)**
```yaml
actions:
  - type: PREEMPTIVE_SCALE
    priority: LOW
    condition: "trending toward limits"
    
  - type: OPTIMIZE_CLUSTER
    priority: MEDIUM
    optimizations: ["performance_tuning"]
```

## 📊 Metrics Analysis

### **Multi-Dimensional Health Scoring**
```java
// Health score calculation (0-100)
double healthScore = 100.0;

// CPU impact (max 30 points)
if (cpuUsage > threshold) {
    healthScore -= Math.min(30, (cpuUsage - threshold) / threshold * 30);
}

// Memory impact (max 25 points)
if (memoryUsage > threshold) {
    healthScore -= Math.min(25, (memoryUsage - threshold) / threshold * 25);
}

// Latency impact (max 25 points)
if (latency > threshold) {
    healthScore -= Math.min(25, (latency - threshold) / threshold * 25);
}

// Determine action based on score
if (healthScore < 60) return CRITICAL_PLAN;
if (healthScore < 80) return WARNING_PLAN;
if (healthScore < 95) return OPTIMIZATION_PLAN;
```

### **Trend Analysis**
- **Upward trends**: Preemptive scaling
- **Anomaly detection**: Alert on unusual patterns
- **Historical comparison**: Learn from past behavior
- **Peak usage analysis**: Optimize for known patterns

## 🛡️ Cooldown Management

### **Prevents Oscillation**
```yaml
cooldown_periods:
  scale_out: 10m      # Prevent rapid scaling
  scale_in: 30m       # Conservative scale-in
  emergency_scale: 5m # Quick response for emergencies
  create_cluster: 60m # Major action cooldown
  optimization: 15m   # Setting changes
```

### **Priority Override**
- **IMMEDIATE actions** bypass cooldowns
- **CRITICAL situations** can force actions
- **Emergency situations** have reduced cooldowns

## 🔔 Alert System

### **Multi-Level Alerting**
```yaml
alert_levels:
  CRITICAL:
    channels: ["pagerduty", "sms", "slack_critical"]
    escalation: "immediate"
    
  ERROR:
    channels: ["slack", "email"]
    escalation: "15 minutes"
    
  WARNING:
    channels: ["slack"]
    escalation: "1 hour"
    
  INFO:
    channels: ["monitoring_dashboard"]
    escalation: "none"
```

### **Smart Alert Routing**
- **Threshold breach severity** determines channel
- **Time-based escalation** for unresolved issues
- **Alert suppression** during maintenance windows
- **Correlation** with other system events

## 📋 Custom Resource Examples

### **Production Cluster**
```yaml
apiVersion: elasticsearch.example.com/v1
kind: ElasticsearchCluster
metadata:
  name: production-cluster
spec:
  clusterName: production-es-cluster
  nodeCount: 3
  version: "OpenSearch_2.11"
  instanceType: "m6g.large.search"
  thresholds:
    cpuHigh: 80.0
    memoryHigh: 85.0
    latencyHigh: 200.0
  autoScaling:
    enabled: true
    minNodes: 2
    maxNodes: 10
  remediationRules:
    - name: "high-cpu-memory"
      condition: "cpu > 80 AND memory > 75"
      action: "scale_out"
      priority: "high"
    - name: "critical-latency"
      condition: "latency_p95 > 500"
      action: "create_new_cluster"
      priority: "critical"
```

## 🧪 Testing Scenarios

### **1. High CPU Load**
```bash
# Simulate high CPU usage
awslocal sqs send-message --queue-url http://localhost:4566/000000000000/cluster-metrics --message-body '{
  "clusterName": "production-es-cluster",
  "metrics": {
    "cpu": {"average": 90.0},
    "memory": {"average": 70.0},
    "searchLatency": {"p95": 150.0}
  }
}'

# Expected: Scale out action
```

### **2. Critical Latency**
```bash
# Simulate critical search latency
awslocal sqs send-message --queue-url http://localhost:4566/000000000000/cluster-metrics --message-body '{
  "clusterName": "production-es-cluster",
  "metrics": {
    "cpu": {"average": 75.0},
    "memory": {"average": 80.0},
    "searchLatency": {"p95": 600.0}
  }
}'

# Expected: Create new cluster + critical alert
```

### **3. Combined Stress**
```bash
# Simulate multiple stress indicators
awslocal sqs send-message --queue-url http://localhost:4566/000000000000/cluster-metrics --message-body '{
  "clusterName": "production-es-cluster",
  "metrics": {
    "cpu": {"average": 95.0},
    "memory": {"average": 90.0},
    "searchLatency": {"p95": 800.0},
    "errorRate": {"average": 5.0}
  }
}'

# Expected: Emergency scaling + critical alert
```

## 📈 Monitoring & Observability

### **Controller Metrics**
```bash
# Check controller logs
kubectl logs -f deployment/opensearch-sqs-demo -n opensearch-sqs-demo

# Monitor cluster status
kubectl get opensearchclusters -n elasticsearch-operator

# Check remediation history
kubectl describe opensearchcluster production-cluster -n elasticsearch-operator
```

### **Cluster Health Dashboard**
```bash
# Access application endpoints
kubectl port-forward -n opensearch-sqs-demo service/opensearch-sqs-demo-service 8080:80

# Check cluster status
curl http://localhost:8080/api/clusters/production-es-cluster/status

# Get metrics history
curl http://localhost:8080/api/clusters/production-es-cluster/metrics-history
```

## 🏭 Production Deployment

### **Real AWS Environment**
```yaml
spring:
  profiles:
    active: prod

aws:
  region: us-west-2
  # Use IAM roles instead of access keys
  
# Real CloudWatch integration
cloudwatch:
  enabled: true
  namespace: "OpenSearch/ClusterMetrics"
  
# Real alert integration
alerts:
  pagerduty:
    integration_key: "${PAGERDUTY_KEY}"
  slack:
    webhook_url: "${SLACK_WEBHOOK}"
```

### **Required IAM Permissions**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "opensearch:*",
        "sqs:ReceiveMessage",
        "sqs:DeleteMessage",
        "sqs:SendMessage",
        "cloudwatch:GetMetricStatistics",
        "cloudwatch:ListMetrics"
      ],
      "Resource": "*"
    }
  ]
}
```

## 🎓 Learning Outcomes

This project demonstrates:

### **Enterprise Patterns**
- ✅ **Operator Pattern** for managing complex applications
- ✅ **Event-Driven Architecture** for reactive systems
- ✅ **Multi-Factor Decision Making** for intelligent automation
- ✅ **Circuit Breaker Pattern** via cooldown management

### **Production Practices**
- ✅ **Kubernetes-Native Management** with CRDs
- ✅ **Comprehensive Monitoring** and alerting
- ✅ **Graceful Degradation** during failures
- ✅ **Audit Trails** for compliance

### **Advanced Concepts**
- ✅ **Predictive Scaling** based on trends
- ✅ **Anomaly Detection** for unusual patterns
- ✅ **Multi-Cluster Management** for high availability
- ✅ **Cost Optimization** through intelligent scaling

This approach transforms simple cluster provisioning into **intelligent, self-healing infrastructure** that adapts to changing conditions automatically - exactly what you'd see in production platforms managing thousands of clusters! 🚀 