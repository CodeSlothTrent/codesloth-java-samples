#
# AWS OpenSearch SQS Demo Setup Script (PowerShell)
#
# Purpose: Complete environment setup for the OpenSearch SQS demo
# When to run: Execute this manually to set up the entire demo environment
# Prerequisites: Docker Desktop, kubectl, kind, awslocal (pip install awscli-local)
#
# What it does:
# 1. Validates all required tools are installed
# 2. Creates a local Kubernetes cluster using kind
# 3. Starts LocalStack with SQS and OpenSearch services
# 4. Builds the Spring Boot application
# 5. Creates Docker image and deploys to Kubernetes
# 6. Waits for everything to be ready
#
# Usage: .\scripts\setup-demo.ps1
#
# After completion:
# - Spring Boot app runs in Kubernetes pod
# - App listens to SQS messages from LocalStack
# - App creates OpenSearch clusters in LocalStack via AWS SDK
#

Write-Host "Setting up AWS OpenSearch SQS Demo..." -ForegroundColor Green

# Check prerequisites
Write-Host "Checking prerequisites..." -ForegroundColor Blue

$missingTools = @()

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    $missingTools += "Docker"
}

if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) {
    $missingTools += "kubectl"
}

if (-not (Get-Command kind -ErrorAction SilentlyContinue)) {
    $missingTools += "kind"
}

if (-not (Get-Command awslocal -ErrorAction SilentlyContinue)) {
    $missingTools += "awslocal (pip install awscli-local)"
}

if ($missingTools.Count -gt 0) {
    Write-Host "❌ Missing required tools:" -ForegroundColor Red
    $missingTools | ForEach-Object { Write-Host "   - $_" -ForegroundColor Red }
    Write-Host ""
    Write-Host "Please install missing tools using setup-environment.ps1 first" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ All prerequisites are installed" -ForegroundColor Green

# Create kind cluster
Write-Host "Creating kind cluster..." -ForegroundColor Blue
$kindConfig = @"
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 80
    hostPort: 8080
    protocol: TCP
"@

$kindConfig | kind create cluster --name opensearch-sqs-demo --config=-

# Set kubectl context
kubectl config use-context kind-opensearch-sqs-demo

# Start LocalStack
Write-Host "Starting LocalStack..." -ForegroundColor Blue
docker-compose up -d

# Wait for LocalStack initialization to complete
Write-Host "Waiting for LocalStack initialization to complete..." -ForegroundColor Blue
Start-Sleep 10

# Check if LocalStack is ready and initialized
$maxAttempts = 12
$attempt = 0
$ready = $false

while (-not $ready -and $attempt -lt $maxAttempts) {
    $attempt++
    Write-Host "Waiting for LocalStack services and initialization... (attempt $attempt/$maxAttempts)" -ForegroundColor Yellow
    
    try {
        $healthResponse = Invoke-RestMethod -Uri "http://localhost:4566/_localstack/health" -Method Get -ErrorAction SilentlyContinue
        $queuesResponse = awslocal sqs list-queues 2>$null
        
        if ($healthResponse.opensearch -eq "available" -and $queuesResponse -match "cluster-requests") {
            $ready = $true
            break
        }
    } catch {
        # Continue waiting
    }
    
    if (-not $ready) {
        Start-Sleep 5
    }
}

if (-not $ready) {
    Write-Host "❌ LocalStack failed to initialize properly" -ForegroundColor Red
    exit 1
}

Write-Host "✅ LocalStack is ready and initialized" -ForegroundColor Green

# Build application
Write-Host "Building Spring Boot application..." -ForegroundColor Blue
mvn clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Maven build failed" -ForegroundColor Red
    exit 1
}

# Build Docker image
Write-Host "Building Docker image..." -ForegroundColor Blue
docker build -t opensearch-sqs-demo:latest .

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker build failed" -ForegroundColor Red
    exit 1
}

# Load image into kind cluster
Write-Host "Loading image into kind cluster..." -ForegroundColor Blue
kind load docker-image opensearch-sqs-demo:latest --name opensearch-sqs-demo

# Deploy to Kubernetes
Write-Host "Deploying application to Kubernetes..." -ForegroundColor Blue
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/deployment.yaml

# Wait for deployment to be ready
Write-Host "Waiting for deployment to be ready..." -ForegroundColor Blue
kubectl wait --for=condition=available --timeout=300s deployment/opensearch-sqs-demo -n opensearch-sqs-demo

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Deployment failed to become ready" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Setup completed successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "Application is running in Kubernetes and ready to process SQS messages" -ForegroundColor Cyan
Write-Host ""
Write-Host "To test:" -ForegroundColor Blue
Write-Host "1. Send SQS message to create OpenSearch cluster" -ForegroundColor Cyan
Write-Host "2. Check application logs: kubectl logs -f deployment/opensearch-sqs-demo -n opensearch-sqs-demo" -ForegroundColor Cyan
Write-Host "3. Access application: kubectl port-forward -n opensearch-sqs-demo service/opensearch-sqs-demo-service 8080:80" -ForegroundColor Cyan
Write-Host ""
Write-Host "LocalStack services available at: http://localhost:4566" -ForegroundColor Cyan
