# AWS OpenSearch SQS Demo - LocalStack Integration

This project demonstrates how to use **AWS SDK** to provision **managed OpenSearch clusters** via **SQS messages**, using **LocalStack** for local development. This mimics how you would provision AWS OpenSearch Service clusters in production.

## Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   LocalStack    │    │   Kubernetes    │    │   LocalStack    │
│   SQS Queues    │───▶│  Spring Boot    │───▶│ OpenSearch API  │
│                 │    │   Application   │    │                 │
│ cluster-requests│    │  (Running in    │    │ Managed Domains │
│ cluster-deletions│   │   K8s Pod)      │    │ (Simulated)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

**Key Points:**
- **Spring Boot app runs IN Kubernetes** (as a pod)
- **App listens to SQS messages** from LocalStack
- **App uses AWS SDK** to create OpenSearch clusters in LocalStack
- **No Kubernetes pods are created for OpenSearch** - only managed domains

## Key Differences from Kubernetes Version

### **Previous (Kubernetes-based)**:
- Created Kubernetes pods running Elasticsearch containers
- Used Kubernetes Java Client
- Managed deployments, services, ConfigMaps manually

### **Current (AWS OpenSearch SDK)**:
- Uses AWS OpenSearch SDK to create managed clusters
- Provisions OpenSearch domains via AWS API calls
- LocalStack simulates AWS OpenSearch Service
- Mimics production AWS OpenSearch behavior

## What Gets Created

When you send an SQS message, the application uses AWS SDK to create:

1. **OpenSearch Domain** (managed cluster)
2. **Cluster Configuration** (instance types, storage, etc.)
3. **Domain Endpoint** (URL to access the cluster)
4. **Security Settings** (simplified for local development)

## Quick Start

### 1. **Automated Setup** (Recommended)
```bash
cd kubernetes
chmod +x scripts/setup-demo.sh
./scripts/setup-demo.sh
```

This will:
- Create kind Kubernetes cluster
- Start LocalStack services
- Build and deploy the Spring Boot application to Kubernetes
- Wait for everything to be ready

### 2. **Manual Setup** (Alternative)
```bash
# Start LocalStack
docker-compose up -d

# Create kind cluster
kind create cluster --name opensearch-sqs-demo

# Build application
mvn clean package

# Build and load Docker image
docker build -t opensearch-sqs-demo:latest .
kind load docker-image opensearch-sqs-demo:latest --name opensearch-sqs-demo

# Deploy to Kubernetes
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/deployment.yaml

# Wait for deployment
kubectl wait --for=condition=available --timeout=300s deployment/opensearch-sqs-demo -n opensearch-sqs-demo
```

### 3. **Test OpenSearch Cluster Creation**

#### **Via SQS Message** (Primary Method):
```powershell
# Send cluster creation message
$clusterRequest = @'
{
  "clusterName": "my-opensearch-cluster",
  "clusterType": "opensearch",
  "nodeCount": 1,
  "version": "2.11",
  "namespace": "default",
  "resources": {
    "cpuRequest": "500m",
    "cpuLimit": "1000m",
    "memoryRequest": "1Gi",
    "memoryLimit": "2Gi",
    "storageSize": "10Gi",
    "storageClass": "gp2"
  },
  "requestId": "test-001",
  "requestedBy": "demo-user"
}
'@

awslocal sqs send-message --queue-url http://localhost:4566/000000000000/cluster-requests --message-body $clusterRequest
```

#### **Via REST API** (Alternative):
```powershell
# Create sample cluster
Invoke-RestMethod -Uri "http://localhost:8080/api/clusters/sample" -Method POST

# Create custom cluster
$clusterData = @{
    clusterName = "my-custom-opensearch"
    clusterType = "opensearch"
    nodeCount = 1
    version = "2.11"
    namespace = "default"
    resources = @{
        memoryRequest = "2Gi"
        storageSize = "20Gi"
        storageClass = "gp2"
    }
} | ConvertTo-Json -Depth 3

Invoke-RestMethod -Uri "http://localhost:8080/api/clusters" -Method POST -Body $clusterData -ContentType "application/json"
```

### 4. **Monitor Cluster Status**

```powershell
# Check cluster status
Invoke-RestMethod -Uri "http://localhost:8080/api/clusters/my-opensearch-cluster/status"

# Get cluster endpoint
Invoke-RestMethod -Uri "http://localhost:8080/api/clusters/my-opensearch-cluster/endpoint"

# Check application logs
kubectl logs -f deployment/opensearch-sqs-demo -n opensearch-sqs-demo
```

### 5. **Access OpenSearch Cluster**

Once the cluster is ready, you can access it via the endpoint:

```powershell
# Get the endpoint URL from the status API
$status = Invoke-RestMethod -Uri "http://localhost:8080/api/clusters/my-opensearch-cluster/status"
$endpoint = $status.endpoint

# Access OpenSearch directly (if endpoint is available)
Invoke-RestMethod -Uri "https://$endpoint/_cluster/health"
```

## API Endpoints

### **Cluster Management**
- `POST /api/clusters` - Create cluster
- `POST /api/clusters/sample` - Create sample cluster
- `GET /api/clusters/{name}/status` - Get cluster status
- `GET /api/clusters/{name}/endpoint` - Get cluster endpoint
- `DELETE /api/clusters/{name}` - Delete cluster

### **Health & Monitoring**
- `GET /api/clusters/health` - Service health check
- `GET /actuator/health` - Spring Boot health

## Configuration

### **application.yml**
```yaml
aws:
  region: us-east-1
  localstack:
    endpoint: http://localhost:4566
    access-key: test
    secret-key: test
  sqs:
    cluster-requests-queue: cluster-requests
    cluster-deletion-queue: cluster-deletions

spring.cloud.aws:
  region:
    static: ${aws.region}
  credentials:
    access-key: ${aws.localstack.access-key}
    secret-key: ${aws.localstack.secret-key}
  sqs:
    endpoint: ${aws.localstack.endpoint}
```

## OpenSearch Cluster Configuration

The application maps your cluster request to AWS OpenSearch parameters:

### **Instance Type Mapping**:
- `1Gi memory` → `t3.small.search`
- `2Gi memory` → `m6g.medium.search`
- `4Gi memory` → `m6g.large.search`

### **Version Mapping**:
- `8.11.0` (Elasticsearch) → `OpenSearch_2.11`
- `7.17.0` (Elasticsearch) → `OpenSearch_1.3`

### **Storage**:
- Uses EBS GP2 volumes
- Size parsed from `storageSize` field (e.g., "10Gi" → 10GB)

## Monitoring and Debugging

### **Application Logs**
```bash
# Check SQS message processing
grep -i "sqs" application.log

# Check OpenSearch operations
grep -i "opensearch" application.log

# Check cluster provisioning
grep -i "cluster" application.log
```

### **LocalStack Logs**
```bash
# Check LocalStack OpenSearch service
docker logs aws-k8s-localstack | grep -i opensearch

# Check SQS operations
docker logs aws-k8s-localstack | grep -i sqs
```

### **AWS CLI Commands**
```bash
# List OpenSearch domains
awslocal opensearch list-domain-names

# Describe specific domain
awslocal opensearch describe-domain --domain-name my-opensearch-cluster

# Check SQS queues
awslocal sqs list-queues

# Get queue messages
awslocal sqs receive-message --queue-url http://localhost:4566/000000000000/cluster-requests
```

## Production Deployment

To deploy to real AWS:

### 1. **Update Configuration**
```yaml
spring:
  profiles:
    active: prod

# Remove LocalStack endpoints
spring.cloud.aws:
  sqs:
    endpoint: # Use default AWS endpoints
```

### 2. **Use Real AWS Credentials**
```bash
# Set environment variables
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
export AWS_REGION=us-east-1
```

### 3. **IAM Permissions**
Your application needs these AWS permissions:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "opensearch:CreateDomain",
        "opensearch:DeleteDomain",
        "opensearch:DescribeDomain",
        "opensearch:ListDomainNames",
        "sqs:ReceiveMessage",
        "sqs:DeleteMessage",
        "sqs:SendMessage"
      ],
      "Resource": "*"
    }
  ]
}
```

## Troubleshooting

### **Common Issues**

1. **LocalStack OpenSearch not available**
   ```bash
   # Check LocalStack health
   curl http://localhost:4566/_localstack/health
   
   # Restart LocalStack
   docker-compose restart localstack
   ```

2. **Cluster creation fails**
   ```bash
   # Check application logs
   grep -i "error" application.log
   
   # Verify LocalStack OpenSearch service
   awslocal opensearch list-domain-names
   ```

3. **SQS messages not processed**
   ```bash
   # Check queue exists
   awslocal sqs list-queues
   
   # Check message visibility
   awslocal sqs get-queue-attributes --queue-url http://localhost:4566/000000000000/cluster-requests --attribute-names All
   ```

## Benefits of This Approach

### **Development**:
- ✅ Test AWS OpenSearch SDK locally
- ✅ No need for real AWS account during development
- ✅ Fast iteration and testing
- ✅ Identical code for local and production

### **Production**:
- ✅ Uses managed AWS OpenSearch Service
- ✅ Automatic scaling, backups, monitoring
- ✅ AWS security and compliance
- ✅ No infrastructure management

### **Learning**:
- ✅ Understand AWS SDK usage
- ✅ Learn OpenSearch cluster management
- ✅ Practice event-driven architecture
- ✅ Experience with SQS message processing

This approach gives you the best of both worlds: local development convenience with production-ready AWS service integration! 

graph TD
    A["👀 Watch API Server"] --> B["📊 Compare Current vs Desired State"]
    B --> C{"States Match?"}
    C -->|No| D["🔧 Take Corrective Action"]
    C -->|Yes| A
    D --> A
    
    E["📝 Desired State<br/>(YAML manifests)"] --> B
    F["⚡ Current State<br/>(Running pods, services)"] --> B 