# AWS Kubernetes SQS Local Development Setup Guide

This guide documents the step-by-step process of creating a demo project that integrates AWS SQS with Kubernetes controllers to provision Elasticsearch clusters locally.

## Project Overview

We're building a Spring Boot application that:
- Listens to SQS messages for cluster provisioning requests
- Uses Kubernetes Java client to manage cluster resources
- Provisions Elasticsearch clusters locally using AWS SDK
- Runs entirely in a local development environment

## Prerequisites

- Java 17+
- Docker and Docker Compose
- kubectl
- kind or minikube for local Kubernetes
- Maven

## Setup Steps

### Step 1: Project Structure Creation
- Created main project structure under `kubernetes/` subfolder
- Set up Maven project with proper directory structure
- Configured base `pom.xml` with essential dependencies

### Step 2: Maven Dependencies Configuration
- Added Spring Boot starters for web and actuator
- Integrated AWS SDK v2 for SQS and Elasticsearch
- Added Spring Cloud AWS for SQS integration
- Included Kubernetes Java client
- Added Testcontainers for local testing

### Step 3: Local Development Infrastructure
- Created Docker Compose configuration for LocalStack
- Set up Elasticsearch container for local development
- Configured LocalStack with SQS and Elasticsearch services

### Step 4: Spring Boot Application Structure
- Created main application class with Spring Boot configuration
- Set up SQS message listeners and controllers
- Implemented Kubernetes service for cluster management
- Added Elasticsearch provisioning service

### Step 5: Kubernetes Resources
- Created Custom Resource Definitions (CRDs) for Elasticsearch clusters
- Set up deployment manifests for the application
- Configured services and ingress for local access

### Step 6: Configuration Files
- Created application.yml for Spring Boot configuration
- Set up AWS configuration for LocalStack integration
- Configured Kubernetes client for local cluster access

### Step 7: Local Setup Scripts
- Created initialization scripts for LocalStack
- Set up SQS queue creation scripts
- Added helper scripts for local development

---

## Detailed Implementation Steps

### Step 1: Project Structure Creation ✅
**Completed:** Created comprehensive Maven project structure under `kubernetes/` subfolder
- **Files Created:**
  - `kubernetes/pom.xml` - Maven configuration with all necessary dependencies
  - `kubernetes/src/main/java/com/example/awsk8ssqs/AwsKubernetesSqsDemoApplication.java` - Main Spring Boot application
  - Directory structure for Java packages (model, config, controller, service)

**Key Dependencies Added:**
- Spring Boot 3.2.0 (Web, Actuator, Validation)
- Spring Cloud AWS 3.0.3 (SQS integration)
- AWS SDK v2 2.21.29 (SQS, Elasticsearch, STS)
- Kubernetes Java Client 19.0.0
- Jackson for JSON processing
- Lombok for code generation

### Step 2: Model and Configuration Classes ✅
**Completed:** Created data models and configuration classes
- **Files Created:**
  - `kubernetes/src/main/java/com/example/awsk8ssqs/model/ClusterRequest.java` - SQS message model
  - `kubernetes/src/main/java/com/example/awsk8ssqs/config/AwsConfig.java` - AWS SDK configuration
  - `kubernetes/src/main/java/com/example/awsk8ssqs/config/KubernetesConfig.java` - Kubernetes client config

**Features Implemented:**
- Comprehensive ClusterRequest model with validation
- Profile-based AWS configuration (local vs production)
- LocalStack integration for local development
- Kubernetes client configuration with multiple connection modes

### Step 3: SQS Message Processing ✅
**Completed:** Implemented SQS message listeners and controllers
- **Files Created:**
  - `kubernetes/src/main/java/com/example/awsk8ssqs/controller/SqsMessageController.java` - SQS message handler
  - `kubernetes/src/main/java/com/example/awsk8ssqs/controller/ClusterController.java` - REST API controller

**Features Implemented:**
- Async SQS message processing with Spring Cloud AWS
- Separate queues for cluster creation and deletion
- Message validation and error handling
- REST API endpoints for testing and monitoring

### Step 4: Kubernetes Integration ✅
**Completed:** Implemented Kubernetes resource management
- **Files Created:**
  - `kubernetes/src/main/java/com/example/awsk8ssqs/service/KubernetesService.java` - K8s operations
  - `kubernetes/src/main/java/com/example/awsk8ssqs/service/ClusterProvisioningService.java` - Business logic
  - `kubernetes/src/main/java/com/example/awsk8ssqs/service/ElasticsearchService.java` - ES-specific logic

**Features Implemented:**
- Dynamic Kubernetes resource creation (Deployments, Services, ConfigMaps, PVCs)
- Namespace management
- Resource cleanup and deletion
- Deployment status monitoring
- Elasticsearch cluster provisioning

### Step 5: Local Development Infrastructure ✅
**Completed:** Set up complete local development environment
- **Files Created:**
  - `kubernetes/docker-compose.yml` - LocalStack and supporting services
  - `kubernetes/localstack-init/01-setup-sqs.sh` - AWS resource initialization
  - `kubernetes/src/main/resources/application.yml` - Spring Boot configuration

**Services Configured:**
- LocalStack (SQS, Elasticsearch, S3, IAM)
- Elasticsearch 8.11.0
- Redis (for future use)
- PostgreSQL (for application data)
- Prometheus & Grafana (monitoring)

### Step 6: Kubernetes Deployment Manifests ✅
**Completed:** Created production-ready Kubernetes manifests
- **Files Created:**
  - `kubernetes/k8s/namespace.yaml` - Application namespace
  - `kubernetes/k8s/deployment.yaml` - Application deployment and service
  - `kubernetes/k8s/rbac.yaml` - Service account and permissions

**Features Configured:**
- RBAC with minimal required permissions
- Health checks and resource limits
- ConfigMap integration
- Service account for Kubernetes API access

### Step 7: Development Scripts and Tools ✅
**Completed:** Created helper scripts for easy setup and testing
- **Files Created:**
  - `kubernetes/scripts/setup-local-env.sh` - Complete environment setup
  - `kubernetes/scripts/send-test-message.sh` - SQS testing script
  - `kubernetes/Dockerfile` - Application containerization

**Features Implemented:**
- Automated kind cluster creation
- Docker Compose service orchestration
- SQS message testing utilities
- Multi-stage Docker build with security best practices

## How to Use This Demo

### Prerequisites Installation

Before starting, ensure you have the following tools installed:

#### For Windows Users

```powershell
# 1. Install Java (JDK 17 or higher)
# Download from: https://adoptium.net/temurin/releases/
# Or you can use JDK 24 which you already installed

# 2. Install Docker Desktop for Windows
# Download from: https://www.docker.com/products/docker-desktop/
# Make sure to enable WSL 2 backend during installation

# 3. Install Chocolatey (Windows Package Manager) - Run as Administrator
Set-ExecutionPolicy Bypass -Scope Process -Force
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# 4. Install kubectl using Chocolatey
choco install kubernetes-cli

# 5. Install kind using Chocolatey
choco install kind

# 6. Install Python and pip (if not already installed)
choco install python

# 7. Refresh PowerShell environment to recognize Python/pip
refreshenv
# Or close and reopen PowerShell

# 8. Install awscli-local for testing
pip install awscli-local

# 8. Install Git (if not already installed)
choco install git
```

#### Alternative Manual Installation (Windows)

```powershell
# If you prefer manual installation:

# Install kubectl manually
curl.exe -LO "https://dl.k8s.io/release/v1.28.0/bin/windows/amd64/kubectl.exe"
# Move kubectl.exe to a directory in your PATH (e.g., C:\Windows\System32)

# Install kind manually
curl.exe -Lo kind-windows-amd64.exe https://kind.sigs.k8s.io/dl/v0.20.0/kind-windows-amd64
# Rename to kind.exe and move to a directory in your PATH

# Install Python from python.org if not using Chocolatey
# Then: pip install awscli-local
```

#### For macOS/Linux Users

```bash
# Install Java 17 (if not already installed)
# On macOS with Homebrew:
brew install openjdk@17

# On Ubuntu/Debian:
sudo apt update && sudo apt install openjdk-17-jdk

# Install Docker
# Follow instructions at: https://docs.docker.com/get-docker/

# Install kubectl
# On macOS:
brew install kubectl
# On Linux:
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# Install kind (Kubernetes in Docker)
# On macOS:
brew install kind
# On Linux:
curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.20.0/kind-linux-amd64
chmod +x ./kind
sudo mv ./kind /usr/local/bin/kind

# Install awscli-local for testing
pip install awscli-local
```

### Step-by-Step Setup Guide

#### 1. Clone and Navigate to Project

**For Windows (PowerShell):**
```powershell
# Navigate to the kubernetes subfolder
cd kubernetes

# Verify you're in the right directory
Get-ChildItem
```

**For macOS/Linux (Bash):**
```bash
# Navigate to the kubernetes subfolder
cd kubernetes

# Make scripts executable
chmod +x scripts/*.sh
chmod +x localstack-init/*.sh
```

#### 2. Set Up Local Environment

**For Windows (PowerShell):**
```powershell
# Since Windows doesn't run bash scripts directly, we'll run commands manually
# First, verify prerequisites
docker --version
kubectl version --client
kind version
python --version
awslocal --version

# Create kind cluster
kind create cluster --name aws-k8s-demo --config - @"
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  kubeadmConfigPatches:
  - |
    kind: InitConfiguration
    nodeRegistration:
      kubeletExtraArgs:
        node-labels: "ingress-ready=true"
  extraPortMappings:
  - containerPort: 80
    hostPort: 80
    protocol: TCP
  - containerPort: 443
    hostPort: 443
    protocol: TCP
- role: worker
- role: worker
"@

# Set kubectl context
kubectl config use-context kind-aws-k8s-demo

# Start Docker Compose services
docker-compose up -d

# Wait for services (you can check status manually)
Write-Host "Waiting for services to start..."
Start-Sleep -Seconds 30
```

**For macOS/Linux (Bash):**
```bash
# Run the automated setup script
./scripts/setup-local-env.sh
```

This will:
- Check all prerequisites
- Create a kind Kubernetes cluster
- Start LocalStack and supporting services via Docker Compose
- Create necessary Kubernetes namespaces and RBAC
- Wait for all services to be ready

#### 3. Build the Application

**For Windows (PowerShell):**
```powershell
# Build the Spring Boot application
mvn clean package

# Verify the build was successful
Get-ChildItem target\aws-kubernetes-sqs-demo-*.jar
```

**For macOS/Linux (Bash):**
```bash
# Build the Spring Boot application
mvn clean package

# Verify the build was successful
ls -la target/aws-kubernetes-sqs-demo-*.jar
```

#### 4. Create and Deploy Docker Image

**For Windows (PowerShell):**
```powershell
# Build Docker image
docker build -t aws-k8s-sqs-demo:latest .

# Load image into kind cluster
kind load docker-image aws-k8s-sqs-demo:latest --name aws-k8s-demo

# Verify image is loaded
docker exec -it aws-k8s-demo-control-plane crictl images | Select-String "aws-k8s-sqs-demo"
```

**For macOS/Linux (Bash):**
```bash
# Build Docker image
docker build -t aws-k8s-sqs-demo:latest .

# Load image into kind cluster
kind load docker-image aws-k8s-sqs-demo:latest --name aws-k8s-demo

# Verify image is loaded
docker exec -it aws-k8s-demo-control-plane crictl images | grep aws-k8s-sqs-demo
```

#### 5. Deploy Application to Kubernetes

**For Windows (PowerShell):**
```powershell
# Create Kubernetes resources first
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/rbac.yaml

# Deploy the application
kubectl apply -f k8s/deployment.yaml

# Wait for deployment to be ready
kubectl wait --for=condition=available --timeout=300s deployment/aws-k8s-sqs-demo -n aws-k8s-demo

# Check deployment status
kubectl get pods -n aws-k8s-demo
```

**For macOS/Linux (Bash):**
```bash
# Deploy the application
kubectl apply -f k8s/deployment.yaml

# Wait for deployment to be ready
kubectl wait --for=condition=available --timeout=300s deployment/aws-k8s-sqs-demo -n aws-k8s-demo

# Check deployment status
kubectl get pods -n aws-k8s-demo
```

#### 6. Verify Services are Running

**For Windows (PowerShell):**
```powershell
# Check LocalStack health
Invoke-RestMethod -Uri "http://localhost:4566/_localstack/health"

# Check Elasticsearch
Invoke-RestMethod -Uri "http://localhost:9200/_cluster/health"

# Check application health (start port forwarding in background)
Start-Job -ScriptBlock { kubectl port-forward -n aws-k8s-demo service/aws-k8s-sqs-demo-service 8080:80 }
Start-Sleep -Seconds 5
Invoke-RestMethod -Uri "http://localhost:8080/actuator/health"
```

**For macOS/Linux (Bash):**
```bash
# Check LocalStack health
curl http://localhost:4566/_localstack/health

# Check Elasticsearch
curl http://localhost:9200/_cluster/health

# Check application health
kubectl port-forward -n aws-k8s-demo service/aws-k8s-sqs-demo-service 8080:80 &
curl http://localhost:8080/actuator/health
```

### Testing the SQS Integration

#### Method 1: Using Test Scripts

**For Windows (PowerShell):**
```powershell
# Since we can't run bash scripts directly, we'll send messages manually
# First, check if LocalStack SQS is ready
awslocal sqs list-queues

# Send test cluster creation message
$clusterRequest = @'
{
  "clusterName": "test-elasticsearch-cluster",
  "clusterType": "elasticsearch",
  "nodeCount": 1,
  "version": "8.11.0",
  "namespace": "default",
  "resources": {
    "cpuRequest": "500m",
    "cpuLimit": "1000m",
    "memoryRequest": "1Gi",
    "memoryLimit": "2Gi",
    "storageSize": "10Gi",
    "storageClass": "standard"
  },
  "requestId": "test-request-001",
  "requestedAt": "2024-01-15T10:30:00Z",
  "requestedBy": "test-user"
}
'@

awslocal sqs send-message --queue-url http://localhost:4566/000000000000/cluster-requests --message-body $clusterRequest

# Monitor application logs
kubectl logs -f deployment/aws-k8s-sqs-demo -n aws-k8s-demo
```

**For macOS/Linux (Bash):**
```bash
# Send test messages to SQS
./scripts/send-test-message.sh

# Monitor application logs
kubectl logs -f deployment/aws-k8s-sqs-demo -n aws-k8s-demo
```

#### Method 2: Using REST API

**For Windows (PowerShell):**
```powershell
# Create a sample cluster via REST API
Invoke-RestMethod -Uri "http://localhost:8080/api/clusters/sample" -Method POST

# Create a custom cluster
$clusterData = @{
    clusterName = "my-elasticsearch-cluster"
    clusterType = "elasticsearch"
    nodeCount = 1
    version = "8.11.0"
    namespace = "default"
    resources = @{
        cpuRequest = "500m"
        cpuLimit = "1000m"
        memoryRequest = "1Gi"
        memoryLimit = "2Gi"
        storageSize = "10Gi"
        storageClass = "standard"
    }
} | ConvertTo-Json -Depth 3

Invoke-RestMethod -Uri "http://localhost:8080/api/clusters" -Method POST -Body $clusterData -ContentType "application/json"
```

**For macOS/Linux (Bash):**
```bash
# Create a sample cluster via REST API
curl -X POST http://localhost:8080/api/clusters/sample

# Create a custom cluster
curl -X POST http://localhost:8080/api/clusters \
  -H "Content-Type: application/json" \
  -d '{
    "clusterName": "my-elasticsearch-cluster",
    "clusterType": "elasticsearch",
    "nodeCount": 1,
    "version": "8.11.0",
    "namespace": "default",
    "resources": {
      "cpuRequest": "500m",
      "cpuLimit": "1000m",
      "memoryRequest": "1Gi",
      "memoryLimit": "2Gi",
      "storageSize": "10Gi",
      "storageClass": "standard"
    }
  }'
```

#### Method 3: Direct SQS Message Sending

**For Windows (PowerShell):**
```powershell
# Send cluster creation message
$messageBody = @'
{
  "clusterName": "direct-elasticsearch-cluster",
  "clusterType": "elasticsearch",
  "nodeCount": 1,
  "version": "8.11.0",
  "namespace": "default",
  "requestId": "direct-001",
  "requestedBy": "manual-test"
}
'@

awslocal sqs send-message --queue-url http://localhost:4566/000000000000/cluster-requests --message-body $messageBody
```

**For macOS/Linux (Bash):**
```bash
# Send cluster creation message
awslocal sqs send-message \
  --queue-url http://localhost:4566/000000000000/cluster-requests \
  --message-body '{
    "clusterName": "direct-elasticsearch-cluster",
    "clusterType": "elasticsearch",
    "nodeCount": 1,
    "version": "8.11.0",
    "namespace": "default",
    "requestId": "direct-001",
    "requestedBy": "manual-test"
  }'
```

### Monitoring and Verification

#### Check Created Clusters
```bash
# List all deployments in default namespace
kubectl get deployments,services,pods -n default

# Get specific cluster status
curl http://localhost:8080/api/clusters/my-elasticsearch-cluster/status

# Check Elasticsearch cluster health
kubectl port-forward service/my-elasticsearch-cluster 9200:9200 -n default &
curl http://localhost:9200/_cluster/health
```

#### View Application Logs
```bash
# Follow application logs
kubectl logs -f deployment/aws-k8s-sqs-demo -n aws-k8s-demo

# View logs from specific pod
kubectl logs -f <pod-name> -n aws-k8s-demo

# View logs with timestamps
kubectl logs --timestamps=true deployment/aws-k8s-sqs-demo -n aws-k8s-demo
```

#### Monitor SQS Queues
```bash
# Check queue attributes
awslocal sqs get-queue-attributes \
  --queue-url http://localhost:4566/000000000000/cluster-requests \
  --attribute-names All

# List all messages in queue (for debugging)
awslocal sqs receive-message \
  --queue-url http://localhost:4566/000000000000/cluster-requests
```

### Cleanup and Deletion

#### Delete Specific Clusters

**For Windows (PowerShell):**
```powershell
# Delete via REST API
Invoke-RestMethod -Uri "http://localhost:8080/api/clusters/my-elasticsearch-cluster?namespace=default" -Method DELETE

# Delete via SQS message
$deletionMessage = '{"clusterName": "my-elasticsearch-cluster", "namespace": "default"}'
awslocal sqs send-message --queue-url http://localhost:4566/000000000000/cluster-deletions --message-body $deletionMessage
```

**For macOS/Linux (Bash):**
```bash
# Delete via REST API
curl -X DELETE http://localhost:8080/api/clusters/my-elasticsearch-cluster?namespace=default

# Delete via SQS message
awslocal sqs send-message \
  --queue-url http://localhost:4566/000000000000/cluster-deletions \
  --message-body '{"clusterName": "my-elasticsearch-cluster", "namespace": "default"}'
```

#### Clean Up Environment

**For Windows (PowerShell):**
```powershell
# Stop port forwarding jobs
Get-Job | Stop-Job
Get-Job | Remove-Job

# Delete Kubernetes resources
kubectl delete -f k8s/deployment.yaml
kubectl delete -f k8s/rbac.yaml
kubectl delete -f k8s/namespace.yaml

# Stop Docker Compose services
docker-compose down -v

# Delete kind cluster
kind delete cluster --name aws-k8s-demo

# Clean up Docker images
docker rmi aws-k8s-sqs-demo:latest

# Optional: Clean up Docker system
docker system prune -f
```

**For macOS/Linux (Bash):**
```bash
# Stop port forwarding
pkill -f "kubectl port-forward"

# Delete Kubernetes resources
kubectl delete -f k8s/deployment.yaml
kubectl delete -f k8s/rbac.yaml
kubectl delete -f k8s/namespace.yaml

# Stop Docker Compose services
docker-compose down -v

# Delete kind cluster
kind delete cluster --name aws-k8s-demo

# Clean up Docker images
docker rmi aws-k8s-sqs-demo:latest
```

### Troubleshooting

#### Common Issues and Solutions

1. **LocalStack not starting**
   ```bash
   # Check Docker daemon is running
   docker info
   
   # Restart LocalStack
   docker-compose restart localstack
   
   # Check LocalStack logs
   docker-compose logs localstack
   ```

2. **Kubernetes connection issues**
   ```bash
   # Verify kubectl context
   kubectl config current-context
   
   # Switch to correct context
   kubectl config use-context kind-aws-k8s-demo
   
   # Test cluster connectivity
   kubectl cluster-info
   ```

3. **Application not receiving SQS messages**
   ```bash
   # Check SQS configuration
   kubectl describe configmap aws-k8s-demo-config -n aws-k8s-demo
   
   # Verify queue exists
   awslocal sqs list-queues
   
   # Check application logs for SQS errors
   kubectl logs deployment/aws-k8s-sqs-demo -n aws-k8s-demo | grep -i sqs
   ```

4. **Elasticsearch cluster not starting**
   ```bash
   # Check pod status
   kubectl describe pod <elasticsearch-pod-name> -n default
   
   # Check resource constraints
   kubectl top nodes
   kubectl top pods -n default
   
   # View pod logs
   kubectl logs <elasticsearch-pod-name> -n default
   ```

5. **Lombok compilation errors (JDK 24 compatibility)**
   
   **Issue**: Getting "cannot find symbol: variable log" errors despite having Lombok plugin installed.
   
   **Solution**: This is a known compatibility issue between JDK 24 and Lombok. Follow these steps:
   
   **For IntelliJ IDEA:**
   ```
   1. Go to File → Invalidate Caches and Restart
   2. Select "Invalidate and Restart"
   3. Wait for IntelliJ to restart and re-index the project
   4. Rebuild the project (Build → Rebuild Project)
   ```
   
   **Alternative solutions if the above doesn't work:**
   - Update Lombok to the latest version in `pom.xml`:
     ```xml
     <dependency>
         <groupId>org.projectlombok</groupId>
         <artifactId>lombok</artifactId>
         <version>1.18.30</version>
         <scope>provided</scope>
     </dependency>
     ```
   - Verify annotation processing is enabled:
     - Go to File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
     - Ensure "Enable annotation processing" is checked
   - Consider downgrading to JDK 17 or 21 for better Lombok compatibility

### Advanced Usage

#### Custom Configuration
```bash
# Create custom application.yml
kubectl create configmap aws-k8s-demo-config \
  --from-file=application.yml=custom-application.yml \
  -n aws-k8s-demo

# Update deployment to use custom config
kubectl rollout restart deployment/aws-k8s-sqs-demo -n aws-k8s-demo
```

#### Scaling the Application
```bash
# Scale application replicas
kubectl scale deployment aws-k8s-sqs-demo --replicas=3 -n aws-k8s-demo

# Enable horizontal pod autoscaling
kubectl autoscale deployment aws-k8s-sqs-demo \
  --cpu-percent=70 \
  --min=2 \
  --max=10 \
  -n aws-k8s-demo
```

#### Production Deployment

**For Windows (PowerShell):**
```powershell
# Deploy with production profile
kubectl set env deployment/aws-k8s-sqs-demo SPRING_PROFILES_ACTIVE=prod -n aws-k8s-demo

# Use real AWS credentials (in production)
kubectl create secret generic aws-credentials --from-literal=access-key=<your-access-key> --from-literal=secret-key=<your-secret-key> -n aws-k8s-demo
```

**For macOS/Linux (Bash):**
```bash
# Deploy with production profile
kubectl set env deployment/aws-k8s-sqs-demo \
  SPRING_PROFILES_ACTIVE=prod \
  -n aws-k8s-demo

# Use real AWS credentials (in production)
kubectl create secret generic aws-credentials \
  --from-literal=access-key=<your-access-key> \
  --from-literal=secret-key=<your-secret-key> \
  -n aws-k8s-demo
```

## Windows-Specific Notes

### PowerShell Execution Policy
If you encounter execution policy errors, run:
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Docker Desktop Configuration
- Ensure WSL 2 backend is enabled
- Allocate sufficient resources (4GB RAM minimum)
- Enable Kubernetes in Docker Desktop settings

### Path Configuration
Add these directories to your Windows PATH:
- Java installation directory (e.g., `C:\Program Files\Eclipse Adoptium\jdk-24.0.1.7-hotspot\bin`)
- Maven bin directory
- kubectl.exe location
- kind.exe location

### Alternative: Using WSL
For a more Linux-like experience, you can use Windows Subsystem for Linux:
```powershell
# Install WSL
wsl --install

# Then use the Linux commands in WSL environment
```

## Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   SQS Queue     │    │  Spring Boot    │    │   Kubernetes    │
│  (LocalStack)   │───▶│   Application   │───▶│    Cluster      │
│                 │    │                 │    │                 │
│ cluster-requests│    │ SqsController   │    │ Elasticsearch   │
│ cluster-deletions│   │ K8sService      │    │ Deployments     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Key Learning Points

1. **SQS Integration**: Using Spring Cloud AWS for seamless SQS message processing
2. **Kubernetes Java Client**: Programmatic cluster resource management
3. **LocalStack**: Local AWS service emulation for development
4. **Async Processing**: Non-blocking message handling with CompletableFuture
5. **RBAC**: Proper Kubernetes permissions for service accounts
6. **Containerization**: Multi-stage Docker builds with security considerations
7. **Monitoring**: Health checks and observability with Actuator

## Production Considerations

- **Security**: Use IAM roles instead of access keys
- **Monitoring**: Integrate with AWS CloudWatch and Kubernetes monitoring
- **Scaling**: Configure HPA for the application deployment
- **Storage**: Use persistent volumes for stateful workloads
- **Networking**: Implement proper ingress and network policies
- **Secrets**: Use Kubernetes secrets for sensitive configuration 