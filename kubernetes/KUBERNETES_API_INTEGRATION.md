# Kubernetes API Integration

This document explains how our OpenSearch cluster controller integrates with the **Kubernetes API** and **etcd** for production-grade cluster management.

## 🏗️ Architecture Overview

### Production-Grade Storage Architecture
```java
// Kubernetes API + etcd integration for persistent, distributed state management
private final KubernetesClusterService kubernetesClusterService;
```

This approach provides enterprise-level capabilities including persistence, high availability, audit trails, and RBAC integration.

## 🔄 Data Flow

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   CloudWatch    │    │   SQS Message   │    │   Controller    │
│    Metrics      │───▶│     Queue       │───▶│   Application   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                                                        ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     etcd        │◀───│ Kubernetes API  │◀───│ OpenSearchCluster│
│   Database      │    │     Server      │    │   Resources     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🎯 Key Components

### 1. Kubernetes Client Configuration (`KubernetesConfig.java`)

```java
@Configuration
public class KubernetesConfig {
    
    @Bean
    public ApiClient kubernetesApiClient() throws IOException {
        // Configures connection to Kubernetes API server
        // Handles both in-cluster and local development scenarios
    }
    
    @Bean
    public GenericKubernetesApi<Object, Object> openSearchClusterApi(ApiClient apiClient) {
        // Type-safe API for our custom OpenSearchCluster resources
        return new GenericKubernetesApi<>(
            Object.class, Object.class,
            "opensearch.aws.com",    // API group
            "v1",                    // API version
            "opensearchclusters",    // Resource plural
            apiClient
        );
    }
}
```

### 2. Kubernetes Cluster Service (`KubernetesClusterService.java`)

This service abstracts all Kubernetes API operations:

#### **Getting a Cluster** (Read from etcd)
```java
public OpenSearchCluster getClusterByName(String clusterName) {
    // Equivalent to: kubectl get opensearchcluster my-cluster
    KubernetesApiResponse<Object> response = openSearchClusterApi.get(namespace, clusterName);
    return convertToOpenSearchCluster(response.getObject());
}
```

#### **Creating/Updating a Cluster** (Write to etcd)
```java
public OpenSearchCluster createOrUpdateCluster(OpenSearchCluster cluster) {
    // Equivalent to: kubectl apply -f cluster.yaml
    Map<String, Object> clusterObject = convertToKubernetesObject(cluster);
    
    if (existing != null) {
        response = openSearchClusterApi.patch(namespace, clusterName, clusterObject);
    } else {
        response = openSearchClusterApi.create(namespace, clusterObject);
    }
}
```

#### **Listing All Clusters** (Query etcd)
```java
public List<OpenSearchCluster> listClusters() {
    // Equivalent to: kubectl get opensearchclusters
    KubernetesApiResponse<Object> response = openSearchClusterApi.list(namespace);
    return convertToOpenSearchClusterList(response.getObject());
}
```

### 3. Production Controller (`OpenSearchClusterController.java`)

The controller leverages Kubernetes API for robust state management:

```java
@SqsListener("${aws.sqs.metrics-queue}")
public void processCloudWatchMetrics(@Payload String message) {
    CloudWatchMetrics metrics = objectMapper.readValue(message, CloudWatchMetrics.class);
    
    // Get cluster from Kubernetes API (reads from etcd)
    OpenSearchCluster cluster = kubernetesClusterService.getClusterByName(metrics.getClusterName());
    
    if (cluster == null) {
        // Create default cluster in Kubernetes API
        cluster = kubernetesClusterService.createDefaultClusterDefinition(metrics.getClusterName());
    }
    
    // ... remediation logic ...
    
    // Update cluster status in Kubernetes API (writes to etcd)
    kubernetesClusterService.updateClusterStatus(cluster);
}
```

## 🏗️ Custom Resource Definition (CRD)

Our `OpenSearchCluster` is defined as a Kubernetes Custom Resource:

```yaml
# k8s/elasticsearch-cluster-crd.yaml
apiVersion: apiextensions.k8s.io/v1
kind: CustomResourceDefinition
metadata:
  name: opensearchclusters.opensearch.aws.com
spec:
  group: opensearch.aws.com
  versions:
  - name: v1
    served: true
    storage: true
    schema:
      openAPIV3Schema:
        type: object
        properties:
          spec:
            type: object
            properties:
              clusterName:
                type: string
              nodeCount:
                type: integer
              # ... more fields
```

## 🔄 Kubernetes API vs etcd

### **What is the Kubernetes API Server?**

The **Kubernetes API Server** is the central management hub:

- **Single Entry Point**: All interactions go through the API server
- **Authentication & Authorization**: Controls who can access what
- **Validation**: Ensures all requests are valid
- **RESTful Interface**: HTTP-based API (GET, POST, PUT, DELETE)

**Example API Calls:**
```bash
# Get cluster
GET /apis/opensearch.aws.com/v1/namespaces/default/opensearchclusters/my-cluster

# Create cluster  
POST /apis/opensearch.aws.com/v1/namespaces/default/opensearchclusters

# Update cluster
PUT /apis/opensearch.aws.com/v1/namespaces/default/opensearchclusters/my-cluster
```

### **What is etcd?**

**etcd** is Kubernetes' distributed database:

- **Key-Value Store**: Stores all cluster state as key-value pairs
- **Distributed**: Runs on multiple nodes for high availability
- **Consistent**: All nodes see the same data (RAFT consensus)
- **Fast**: Optimized for frequent reads and writes

**Example etcd Storage:**
```
Key: /registry/opensearchclusters/default/my-cluster
Value: {
  "apiVersion": "opensearch.aws.com/v1",
  "kind": "OpenSearchCluster", 
  "metadata": { "name": "my-cluster" },
  "spec": { "nodeCount": 3, "version": "OpenSearch_2.11" },
  "status": { "phase": "Ready" }
}
```

## 🔄 How They Work Together

1. **Controller makes API request** → `kubernetesClusterService.getClusterByName("my-cluster")`
2. **API Server receives request** → Validates permissions and request format
3. **API Server queries etcd** → `GET /registry/opensearchclusters/default/my-cluster`
4. **etcd returns data** → Raw JSON cluster definition
5. **API Server formats response** → Converts to proper API response
6. **Controller receives cluster** → Converted to `OpenSearchCluster` object

## 📈 Enterprise Features

| Feature | Kubernetes API + etcd Benefits |
|---------|------------------------------|
| **Persistence** | ✅ Data survives restarts and system failures |
| **Consistency** | ✅ Multiple controllers can safely share state |
| **High Availability** | ✅ etcd cluster provides automatic failover |
| **Scalability** | ✅ Distributed storage scales horizontally |
| **Audit Trail** | ✅ Complete history of all cluster changes |
| **Access Control** | ✅ RBAC integration for fine-grained permissions |
| **Real-time Updates** | ✅ Watch API provides instant change notifications |
| **Multi-tenancy** | ✅ Namespace isolation for different environments |

## 🔧 Configuration

### Development Environment
```yaml
# application.yml
kubernetes:
  config:
    in-cluster: false  # Running outside Kubernetes
    kubeconfig-path:   # Uses ~/.kube/config
  namespace: default
```

### Production Environment (Pod running in Kubernetes)
```yaml
# application.yml
kubernetes:
  config:
    in-cluster: true   # Running inside Kubernetes pod
  namespace: production
```

## 🚀 Production Deployment

### 1. Apply the CRD
```bash
kubectl apply -f k8s/elasticsearch-cluster-crd.yaml
```

### 2. Deploy the Controller
```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/rbac.yaml
```

### 3. Verify Integration
```bash
# Check if controller can access API
kubectl logs deployment/opensearch-controller

# Create a test cluster
kubectl apply -f - <<EOF
apiVersion: opensearch.aws.com/v1
kind: OpenSearchCluster
metadata:
  name: test-cluster
  namespace: default
spec:
  clusterName: test-cluster
  nodeCount: 3
  version: OpenSearch_2.11
EOF

# Verify it's stored in etcd (via API)
kubectl get opensearchcluster test-cluster -o yaml
```

## 🔍 Debugging

### Check Kubernetes API Access
```bash
# Test API connection
kubectl auth can-i get opensearchclusters
kubectl auth can-i create opensearchclusters
kubectl auth can-i update opensearchclusters
```

### View etcd Storage Directly
```bash
# Connect to etcd (advanced debugging)
kubectl exec -it etcd-pod -- etcdctl get /registry/opensearchclusters --prefix
```

### Monitor API Calls
```bash
# Enable API audit logging to see all requests
kubectl logs kube-apiserver-pod | grep opensearchclusters
```

## 📚 Learning Outcomes

After implementing this integration, you understand:

1. **How Kubernetes operators work** - Real production controllers
2. **API Server role** - Central command and control
3. **etcd functionality** - Distributed state storage  
4. **Custom Resources** - Extending Kubernetes with domain-specific objects
5. **Production patterns** - How enterprise systems manage state
6. **Event-driven architecture** - Controllers responding to API changes

This is the **exact same pattern** used by:
- **Netflix** - Managing thousands of microservices
- **Uber** - Orchestrating global infrastructure  
- **AWS Controllers** - Managing cloud resources via Kubernetes
- **Istio** - Service mesh configuration
- **ArgoCD** - GitOps deployment management

You're now working with **production-grade Kubernetes patterns**! 🎉 

## ⚡ CloudWatch Integration Strategy

### **Avoiding Redundant Analysis**

Our controller is designed to work with **CloudWatch alarms** rather than raw metrics. This eliminates redundant trend analysis:

```java
// ❌ REDUNDANT: Re-analyzing trends that CloudWatch already detected
if (cpuUsage > threshold) {
    // CloudWatch alarm already fired because this condition was met
}

// ✅ EFFICIENT: Respond to CloudWatch alarm decisions
if (hasAlarmForMetric(metrics, "CPU")) {
    // CloudWatch already determined this is a problem - take action
}
```

### **CloudWatch Handles:**
- ✅ **Threshold monitoring** (evaluation periods, comparison operators)
- ✅ **Trend analysis** (sustained threshold breaches over time)
- ✅ **Anomaly detection** (machine learning-based pattern recognition)
- ✅ **Statistical evaluation** (average, max, percentiles over periods)

### **Our Controller Handles:**
- ✅ **Multi-metric correlation** (combining CPU + Memory + Latency alarms)
- ✅ **Remediation decisions** (what action to take when alarms fire)
- ✅ **Action prioritization** (critical vs warning vs optimization)
- ✅ **Cooldown management** (prevent action oscillation)
- ✅ **Cluster-specific logic** (can this cluster scale? what's the target size?)

This division of responsibility makes the system more efficient and avoids duplicating AWS's sophisticated monitoring capabilities.



## 🎯 Key Components

### 1. Kubernetes Client Configuration (`KubernetesConfig.java`)

```java
@Configuration
public class KubernetesConfig {
    
    @Bean
    public ApiClient kubernetesApiClient() throws IOException {
        // Configures connection to Kubernetes API server
        // Handles both in-cluster and local development scenarios
    }
    
    @Bean
    public GenericKubernetesApi<Object, Object> openSearchClusterApi(ApiClient apiClient) {
        // Type-safe API for our custom OpenSearchCluster resources
        return new GenericKubernetesApi<>(
            Object.class, Object.class,
            "opensearch.aws.com",    // API group
            "v1",                    // API version
            "opensearchclusters",    // Resource plural
            apiClient
        );
    }
}
```

### 2. Kubernetes Cluster Service (`KubernetesClusterService.java`)

This service abstracts all Kubernetes API operations:

#### **Getting a Cluster** (Read from etcd)
```java
public OpenSearchCluster getClusterByName(String clusterName) {
    // Equivalent to: kubectl get opensearchcluster my-cluster
    KubernetesApiResponse<Object> response = openSearchClusterApi.get(namespace, clusterName);
    return convertToOpenSearchCluster(response.getObject());
}
```

#### **Creating/Updating a Cluster** (Write to etcd)
```java
public OpenSearchCluster createOrUpdateCluster(OpenSearchCluster cluster) {
    // Equivalent to: kubectl apply -f cluster.yaml
    Map<String, Object> clusterObject = convertToKubernetesObject(cluster);
    
    if (existing != null) {
        response = openSearchClusterApi.patch(namespace, clusterName, clusterObject);
    } else {
        response = openSearchClusterApi.create(namespace, clusterObject);
    }
}
```

#### **Listing All Clusters** (Query etcd)
```java
public List<OpenSearchCluster> listClusters() {
    // Equivalent to: kubectl get opensearchclusters
    KubernetesApiResponse<Object> response = openSearchClusterApi.list(namespace);
    return convertToOpenSearchClusterList(response.getObject());
}
```

### 3. Production Controller (`OpenSearchClusterController.java`)

The controller leverages Kubernetes API for robust state management:

```java
@SqsListener("${aws.sqs.metrics-queue}")
public void processCloudWatchMetrics(@Payload String message) {
    CloudWatchMetrics metrics = objectMapper.readValue(message, CloudWatchMetrics.class);
    
    // Get cluster from Kubernetes API (reads from etcd)
    OpenSearchCluster cluster = kubernetesClusterService.getClusterByName(metrics.getClusterName());
    
    if (cluster == null) {
        // Create default cluster in Kubernetes API
        cluster = kubernetesClusterService.createDefaultClusterDefinition(metrics.getClusterName());
    }
    
    // ... remediation logic ...
    
    // Update cluster status in Kubernetes API (writes to etcd)
    kubernetesClusterService.updateClusterStatus(cluster);
}
```

## 🏗️ Custom Resource Definition (CRD)

Our `OpenSearchCluster` is defined as a Kubernetes Custom Resource:

```yaml
# k8s/elasticsearch-cluster-crd.yaml
apiVersion: apiextensions.k8s.io/v1
kind: CustomResourceDefinition
metadata:
  name: opensearchclusters.opensearch.aws.com
spec:
  group: opensearch.aws.com
  versions:
  - name: v1
    served: true
    storage: true
    schema:
      openAPIV3Schema:
        type: object
        properties:
          spec:
            type: object
            properties:
              clusterName:
                type: string
              nodeCount:
                type: integer
              # ... more fields
```

## 🔄 Kubernetes API vs etcd

### **What is the Kubernetes API Server?**

The **Kubernetes API Server** is the central management hub:

- **Single Entry Point**: All interactions go through the API server
- **Authentication & Authorization**: Controls who can access what
- **Validation**: Ensures all requests are valid
- **RESTful Interface**: HTTP-based API (GET, POST, PUT, DELETE)

**Example API Calls:**
```bash
# Get cluster
GET /apis/opensearch.aws.com/v1/namespaces/default/opensearchclusters/my-cluster

# Create cluster  
POST /apis/opensearch.aws.com/v1/namespaces/default/opensearchclusters

# Update cluster
PUT /apis/opensearch.aws.com/v1/namespaces/default/opensearchclusters/my-cluster
```

### **What is etcd?**

**etcd** is Kubernetes' distributed database:

- **Key-Value Store**: Stores all cluster state as key-value pairs
- **Distributed**: Runs on multiple nodes for high availability
- **Consistent**: All nodes see the same data (RAFT consensus)
- **Fast**: Optimized for frequent reads and writes

**Example etcd Storage:**
```
Key: /registry/opensearchclusters/default/my-cluster
Value: {
  "apiVersion": "opensearch.aws.com/v1",
  "kind": "OpenSearchCluster", 
  "metadata": { "name": "my-cluster" },
  "spec": { "nodeCount": 3, "version": "OpenSearch_2.11" },
  "status": { "phase": "Ready" }
}
```

## 🔄 How They Work Together

1. **Controller makes API request** → `kubernetesClusterService.getClusterByName("my-cluster")`
2. **API Server receives request** → Validates permissions and request format
3. **API Server queries etcd** → `GET /registry/opensearchclusters/default/my-cluster`
4. **etcd returns data** → Raw JSON cluster definition
5. **API Server formats response** → Converts to proper API response
6. **Controller receives cluster** → Converted to `OpenSearchCluster` object

## 📈 Enterprise Features

| Feature | Kubernetes API + etcd Benefits |
|---------|------------------------------|
| **Persistence** | ✅ Data survives restarts and system failures |
| **Consistency** | ✅ Multiple controllers can safely share state |
| **High Availability** | ✅ etcd cluster provides automatic failover |
| **Scalability** | ✅ Distributed storage scales horizontally |
| **Audit Trail** | ✅ Complete history of all cluster changes |
| **Access Control** | ✅ RBAC integration for fine-grained permissions |
| **Real-time Updates** | ✅ Watch API provides instant change notifications |
| **Multi-tenancy** | ✅ Namespace isolation for different environments |

## 🔧 Configuration

### Development Environment
```yaml
# application.yml
kubernetes:
  config:
    in-cluster: false  # Running outside Kubernetes
    kubeconfig-path:   # Uses ~/.kube/config
  namespace: default
```

### Production Environment (Pod running in Kubernetes)
```yaml
# application.yml
kubernetes:
  config:
    in-cluster: true   # Running inside Kubernetes pod
  namespace: production
```

## 🚀 Production Deployment

### 1. Apply the CRD
```bash
kubectl apply -f k8s/elasticsearch-cluster-crd.yaml
```

### 2. Deploy the Controller
```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/rbac.yaml
```

### 3. Verify Integration
```bash
# Check if controller can access API
kubectl logs deployment/opensearch-controller

# Create a test cluster
kubectl apply -f - <<EOF
apiVersion: opensearch.aws.com/v1
kind: OpenSearchCluster
metadata:
  name: test-cluster
  namespace: default
spec:
  clusterName: test-cluster
  nodeCount: 3
  version: OpenSearch_2.11
EOF

# Verify it's stored in etcd (via API)
kubectl get opensearchcluster test-cluster -o yaml
```

## 🔍 Debugging

### Check Kubernetes API Access
```bash
# Test API connection
kubectl auth can-i get opensearchclusters
kubectl auth can-i create opensearchclusters
kubectl auth can-i update opensearchclusters
```

### View etcd Storage Directly
```bash
# Connect to etcd (advanced debugging)
kubectl exec -it etcd-pod -- etcdctl get /registry/opensearchclusters --prefix
```

### Monitor API Calls
```bash
# Enable API audit logging to see all requests
kubectl logs kube-apiserver-pod | grep opensearchclusters
```

## 📚 Learning Outcomes

After implementing this integration, you understand:

1. **How Kubernetes operators work** - Real production controllers
2. **API Server role** - Central command and control
3. **etcd functionality** - Distributed state storage  
4. **Custom Resources** - Extending Kubernetes with domain-specific objects
5. **Production patterns** - How enterprise systems manage state
6. **Event-driven architecture** - Controllers responding to API changes

This is the **exact same pattern** used by:
- **Netflix** - Managing thousands of microservices
- **Uber** - Orchestrating global infrastructure  
- **AWS Controllers** - Managing cloud resources via Kubernetes
- **Istio** - Service mesh configuration
- **ArgoCD** - GitOps deployment management

You're now working with **production-grade Kubernetes patterns**! 🎉 