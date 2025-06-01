Write-Host "Starting AWS OpenSearch SQS Demo Setup..." -ForegroundColor Green

# Quick environment validation
Write-Host "Validating environment..." -ForegroundColor Blue

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
    $missingTools += "awslocal"
}

if ($missingTools.Count -gt 0) {
    Write-Host "Missing required tools:" -ForegroundColor Red
    $missingTools | ForEach-Object { Write-Host "   - $_" -ForegroundColor Red }
    Write-Host "Please run setup-environment.ps1 first." -ForegroundColor Yellow
    exit 1
}

# Quick Docker check
Write-Host "Checking Docker..." -ForegroundColor Blue
try {
    docker info | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Docker is ready" -ForegroundColor Green
    } else {
        Write-Host "Docker is not running" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "Docker is not available" -ForegroundColor Red
    exit 1
}

Write-Host "Environment validation complete" -ForegroundColor Green

# Cleanup existing resources
Write-Host "Cleaning up existing resources..." -ForegroundColor Blue

# Check if kind cluster already exists
$existingCluster = kind get clusters 2>$null | Where-Object { $_ -eq "opensearch-sqs-demo" }
if ($existingCluster) {
    Write-Host "  Deleting existing kind cluster..." -ForegroundColor Yellow
    kind delete cluster --name opensearch-sqs-demo
}

# Stop existing LocalStack containers
Write-Host "  Stopping existing LocalStack containers..." -ForegroundColor Yellow
docker-compose down 2>$null

Write-Host "Cleanup complete" -ForegroundColor Green

# Create kind cluster
Write-Host "Creating kind cluster..." -ForegroundColor Blue

# Create simple kind cluster
kind create cluster --name opensearch-sqs-demo

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to create kind cluster" -ForegroundColor Red
    exit 1
}

# Set kubectl context
Write-Host "Setting kubectl context..." -ForegroundColor Blue
kubectl config use-context kind-opensearch-sqs-demo

# Start LocalStack
Write-Host "Starting LocalStack..." -ForegroundColor Blue
docker-compose up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to start LocalStack" -ForegroundColor Red
    exit 1
}

# Wait for LocalStack initialization
Write-Host "Waiting for LocalStack initialization..." -ForegroundColor Blue
Start-Sleep 15

Write-Host "LocalStack is ready" -ForegroundColor Green

# Build application
Write-Host "Building Spring Boot application..." -ForegroundColor Blue
mvn clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to build Spring Boot application" -ForegroundColor Red
    exit 1
}

# Build Docker image
Write-Host "Building Docker image..." -ForegroundColor Blue
docker build -t opensearch-sqs-demo:latest .

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to build Docker image" -ForegroundColor Red
    exit 1
}

# Load image into kind cluster
Write-Host "Loading image into kind cluster..." -ForegroundColor Blue
kind load docker-image opensearch-sqs-demo:latest --name opensearch-sqs-demo

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to load Docker image into kind cluster" -ForegroundColor Red
    exit 1
}

# Deploy to Kubernetes
Write-Host "Deploying application to Kubernetes..." -ForegroundColor Blue
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/deployment.yaml

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to deploy to Kubernetes" -ForegroundColor Red
    exit 1
}

# Wait for deployment to be ready
Write-Host "Waiting for deployment to be ready..." -ForegroundColor Blue
kubectl wait --for=condition=available --timeout=300s deployment/opensearch-sqs-demo -n opensearch-sqs-demo

if ($LASTEXITCODE -ne 0) {
    Write-Host "Deployment failed to become ready" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Setup completed successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "Application is running in Kubernetes and ready to process SQS messages" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Blue
Write-Host "1. Check application logs: kubectl logs -f deployment/opensearch-sqs-demo -n opensearch-sqs-demo" -ForegroundColor White
Write-Host "2. Access LocalStack: http://localhost:4566" -ForegroundColor White
Write-Host "3. Port forward to app: kubectl port-forward -n opensearch-sqs-demo service/opensearch-sqs-demo-service 8080:80" -ForegroundColor White
