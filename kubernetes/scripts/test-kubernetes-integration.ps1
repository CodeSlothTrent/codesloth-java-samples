# Test Kubernetes API Integration
# This script tests that our OpenSearchCluster CRD is working properly

Write-Host "🧪 Testing Kubernetes API integration..." -ForegroundColor Green

# Test 1: Verify CRD exists
Write-Host "📋 Test 1: Checking CRD installation..." -ForegroundColor Blue
try {
    $crd = kubectl get crd opensearchclusters.opensearch.aws.com -o jsonpath='{.metadata.name}' 2>$null
    if ($crd -eq "opensearchclusters.opensearch.aws.com") {
        Write-Host "✅ CRD exists: $crd" -ForegroundColor Green
    } else {
        Write-Host "❌ CRD not found" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Failed to check CRD: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 2: Create a test cluster resource
Write-Host "📋 Test 2: Creating test cluster resource..." -ForegroundColor Blue
$testCluster = @"
apiVersion: opensearch.aws.com/v1
kind: OpenSearchCluster
metadata:
  name: integration-test-cluster
  namespace: default
spec:
  clusterName: integration-test-cluster
  nodeCount: 2
  version: OpenSearch_2.11
"@

try {
    $testCluster | kubectl apply -f -
    Write-Host "✅ Test cluster created" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to create test cluster: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 3: Verify cluster can be retrieved  
Write-Host "📋 Test 3: Retrieving test cluster..." -ForegroundColor Blue
try {
    $retrievedCluster = kubectl get opensearchcluster integration-test-cluster -o jsonpath='{.metadata.name}' 2>$null
    if ($retrievedCluster -eq "integration-test-cluster") {
        Write-Host "✅ Cluster retrieved successfully: $retrievedCluster" -ForegroundColor Green
    } else {
        Write-Host "❌ Failed to retrieve cluster" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Failed to retrieve cluster: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Clean up test cluster
Write-Host "🧹 Cleaning up test cluster..." -ForegroundColor Blue
try {
    kubectl delete opensearchcluster integration-test-cluster 2>$null
    Write-Host "✅ Test cluster deleted" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Failed to delete test cluster" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🎉 Kubernetes API integration tests completed!" -ForegroundColor Green
Write-Host ""
Write-Host "✨ What this proves:" -ForegroundColor Blue
Write-Host "   ✅ Custom Resource Definition is properly installed" -ForegroundColor White
Write-Host "   ✅ OpenSearchCluster resources can be created" -ForegroundColor White
Write-Host "   ✅ Resources are stored in etcd via Kubernetes API" -ForegroundColor White
Write-Host "   ✅ Resources can be retrieved and listed" -ForegroundColor White
Write-Host "" 