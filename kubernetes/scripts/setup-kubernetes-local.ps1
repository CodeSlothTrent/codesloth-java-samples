# Setup Local Kubernetes Development Environment
# This script helps set up the Kubernetes API integration for local development

Write-Host "🔧 Setting up Kubernetes API integration for local development..." -ForegroundColor Green

# Check if kubectl is installed
Write-Host "📋 Checking kubectl installation..." -ForegroundColor Blue
try {
    $kubectlVersion = kubectl version --client --short 2>$null
    Write-Host "✅ kubectl found: $kubectlVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ kubectl not found. Please install kubectl first:" -ForegroundColor Red
    Write-Host "   - Install via Chocolatey: choco install kubernetes-cli" -ForegroundColor Yellow
    Write-Host "   - Or download from: https://kubernetes.io/docs/tasks/tools/install-kubectl-windows/" -ForegroundColor Yellow
    exit 1
}

# Check if Docker Desktop or other Kubernetes is running
Write-Host "📋 Checking Kubernetes cluster access..." -ForegroundColor Blue
try {
    $clusterInfo = kubectl cluster-info 2>$null
    Write-Host "✅ Kubernetes cluster accessible" -ForegroundColor Green
} catch {
    Write-Host "❌ Cannot access Kubernetes cluster. Options:" -ForegroundColor Red
    Write-Host "   1. Enable Kubernetes in Docker Desktop" -ForegroundColor Yellow
    Write-Host "   2. Use minikube: minikube start" -ForegroundColor Yellow
    Write-Host "   3. Use kind: kind create cluster" -ForegroundColor Yellow
    exit 1
}

# Apply the Custom Resource Definition
Write-Host "📋 Applying OpenSearchCluster CRD..." -ForegroundColor Blue
$crdPath = Join-Path $PSScriptRoot "..\k8s\elasticsearch-cluster-crd.yaml"

if (Test-Path $crdPath) {
    try {
        kubectl apply -f $crdPath
        Write-Host "✅ OpenSearchCluster CRD applied successfully" -ForegroundColor Green
    } catch {
        Write-Host "❌ Failed to apply CRD. Error: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "❌ CRD file not found at: $crdPath" -ForegroundColor Red
    exit 1
}

# Create namespace if it doesn't exist
Write-Host "📋 Ensuring default namespace exists..." -ForegroundColor Blue
try {
    kubectl create namespace default --dry-run=client -o yaml | kubectl apply -f -
    Write-Host "✅ Namespace ready" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Namespace might already exist, continuing..." -ForegroundColor Yellow
}

# Verify CRD installation
Write-Host "📋 Verifying CRD installation..." -ForegroundColor Blue
try {
    $crdCheck = kubectl get crd opensearchclusters.opensearch.aws.com 2>$null
    if ($crdCheck) {
        Write-Host "✅ OpenSearchCluster CRD is properly installed" -ForegroundColor Green
    } else {
        Write-Host "❌ CRD verification failed" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ CRD verification failed" -ForegroundColor Red
    exit 1
}

# Test creating a sample cluster resource
Write-Host "📋 Testing cluster resource creation..." -ForegroundColor Blue
$testCluster = @"
apiVersion: opensearch.aws.com/v1
kind: OpenSearchCluster
metadata:
  name: test-setup-cluster
  namespace: default
spec:
  clusterName: test-setup-cluster
  nodeCount: 3
  version: OpenSearch_2.11
  instanceType: m6g.large.search
"@

try {
    $testCluster | kubectl apply -f -
    Write-Host "✅ Test cluster created successfully" -ForegroundColor Green
    
    # Clean up test cluster
    Write-Host "🧹 Cleaning up test cluster..." -ForegroundColor Blue
    kubectl delete opensearchcluster test-setup-cluster
    Write-Host "✅ Test cluster cleaned up" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to create/delete test cluster: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Check RBAC permissions (for future when running in cluster)
Write-Host "📋 Checking RBAC permissions..." -ForegroundColor Blue
try {
    $canGet = kubectl auth can-i get opensearchclusters 2>$null
    $canCreate = kubectl auth can-i create opensearchclusters 2>$null
    $canUpdate = kubectl auth can-i update opensearchclusters 2>$null
    
    if ($canGet -and $canCreate -and $canUpdate) {
        Write-Host "✅ RBAC permissions look good" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Some RBAC permissions may be missing (OK for local development)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️  RBAC check failed (OK for local development)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🎉 Kubernetes API integration setup complete!" -ForegroundColor Green
Write-Host ""
Write-Host "📝 Next steps:" -ForegroundColor Blue
Write-Host "   1. Start your Spring Boot application" -ForegroundColor White
Write-Host "   2. Send CloudWatch metrics via SQS" -ForegroundColor White
Write-Host "   3. Watch clusters being created in Kubernetes:" -ForegroundColor White
Write-Host "      kubectl get opensearchclusters -w" -ForegroundColor Cyan
Write-Host ""
Write-Host "🔍 Useful commands:" -ForegroundColor Blue
Write-Host "   kubectl get opensearchclusters                    # List all clusters" -ForegroundColor Cyan
Write-Host "   kubectl describe opensearchcluster my-cluster     # Get cluster details" -ForegroundColor Cyan
Write-Host "   kubectl get opensearchcluster my-cluster -o yaml  # Get full cluster YAML" -ForegroundColor Cyan
Write-Host "   kubectl logs deployment/opensearch-controller     # View controller logs" -ForegroundColor Cyan
Write-Host "" 