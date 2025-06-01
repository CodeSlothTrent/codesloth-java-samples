#
# Demo Cleanup Script (PowerShell)
#
# Purpose: Clean up all resources created by the OpenSearch SQS demo
# When to run: When you want to completely remove all demo resources
# Prerequisites: None
#
# What it does:
# 1. Stops and removes LocalStack containers
# 2. Deletes kind cluster
# 3. Cleans up Docker images and containers
# 4. Removes generated files
#
# Usage: .\scripts\cleanup-demo.ps1
#

Write-Host "Cleaning up OpenSearch SQS Demo resources..." -ForegroundColor Yellow

# Stop LocalStack containers
Write-Host "Stopping LocalStack containers..." -ForegroundColor Blue
try {
    docker-compose down --volumes --remove-orphans 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ LocalStack containers stopped and removed" -ForegroundColor Green
    } else {
        Write-Host "ℹ️  No LocalStack containers found" -ForegroundColor Cyan
    }
} catch {
    Write-Host "ℹ️  No docker-compose resources found" -ForegroundColor Cyan
}

# Delete kind cluster
Write-Host "Deleting kind cluster..." -ForegroundColor Blue
$existingCluster = kind get clusters 2>$null | Where-Object { $_ -eq "opensearch-sqs-demo" }
if ($existingCluster) {
    kind delete cluster --name opensearch-sqs-demo
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Kind cluster deleted" -ForegroundColor Green
    } else {
        Write-Host "❌ Failed to delete kind cluster" -ForegroundColor Red
    }
} else {
    Write-Host "ℹ️  No kind cluster found" -ForegroundColor Cyan
}

# Clean up Docker containers
Write-Host "Cleaning up Docker containers..." -ForegroundColor Blue
$containers = docker ps -a --filter "name=opensearch-sqs" --format "{{.ID}}" 2>$null
if ($containers) {
    docker rm -f $containers 2>$null
    Write-Host "✅ Docker containers cleaned up" -ForegroundColor Green
} else {
    Write-Host "ℹ️  No demo containers found" -ForegroundColor Cyan
}

# Clean up Docker images (optional)
Write-Host "Cleaning up Docker images..." -ForegroundColor Blue
$images = docker images --filter "reference=opensearch-sqs-demo" --format "{{.ID}}" 2>$null
if ($images) {
    $cleanup = Read-Host "Remove demo Docker images? (y/n)"
    if ($cleanup -eq "y" -or $cleanup -eq "Y") {
        docker rmi -f $images 2>$null
        Write-Host "✅ Docker images cleaned up" -ForegroundColor Green
    } else {
        Write-Host "ℹ️  Docker images kept" -ForegroundColor Cyan
    }
} else {
    Write-Host "ℹ️  No demo Docker images found" -ForegroundColor Cyan
}

# Clean up generated files
Write-Host "Cleaning up generated files..." -ForegroundColor Blue
$filesToClean = @(
    "dashboard-token.txt",
    "localstack-init/init-aws.sh"
)

foreach ($file in $filesToClean) {
    if (Test-Path $file) {
        Remove-Item $file -Force
        Write-Host "  ✅ Removed $file" -ForegroundColor Green
    }
}

# Clean up Maven target directory
if (Test-Path "target") {
    $cleanMaven = Read-Host "Clean Maven target directory? (y/n)"
    if ($cleanMaven -eq "y" -or $cleanMaven -eq "Y") {
        Remove-Item "target" -Recurse -Force
        Write-Host "✅ Maven target directory cleaned" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "🎉 Cleanup completed!" -ForegroundColor Green
Write-Host ""
Write-Host "📝 What was cleaned up:" -ForegroundColor Blue
Write-Host "   • LocalStack containers and volumes" -ForegroundColor White
Write-Host "   • Kind Kubernetes cluster" -ForegroundColor White
Write-Host "   • Demo Docker containers" -ForegroundColor White
Write-Host "   • Generated configuration files" -ForegroundColor White
Write-Host ""
Write-Host "💡 To start fresh:" -ForegroundColor Blue
Write-Host "   Run: .\scripts\setup-demo.ps1" -ForegroundColor Cyan
Write-Host "" 