#
# Kubernetes Dashboard Setup Script (PowerShell)
#
# Purpose: Install and configure Kubernetes Dashboard for cluster visualization
# When to run: After setting up the kind cluster with setup-demo.ps1
# Prerequisites: kubectl, kind cluster running
#
# What it does:
# 1. Deploys Kubernetes Dashboard
# 2. Creates admin service account
# 3. Provides access instructions
#
# Usage: .\scripts\setup-dashboard.ps1
#

Write-Host "Setting up Kubernetes Dashboard..." -ForegroundColor Green

# Check if kubectl is available
if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) {
    Write-Host "❌ kubectl not found" -ForegroundColor Red
    exit 1
}

# Check if cluster is accessible
Write-Host "Checking cluster access..." -ForegroundColor Blue
try {
    $clusterInfo = kubectl cluster-info 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Cannot access Kubernetes cluster" -ForegroundColor Red
        Write-Host "   Make sure your kind cluster is running" -ForegroundColor Yellow
        exit 1
    }
    Write-Host "✅ Cluster is accessible" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to access cluster" -ForegroundColor Red
    exit 1
}

# Check if dashboard already exists
$existingDashboard = kubectl get deployment kubernetes-dashboard -n kubernetes-dashboard 2>$null
if ($existingDashboard) {
    Write-Host "✅ Dashboard already deployed" -ForegroundColor Green
} else {
    # Deploy Kubernetes Dashboard
    Write-Host "Deploying Kubernetes Dashboard..." -ForegroundColor Blue
    try {
        kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml
        if ($LASTEXITCODE -ne 0) {
            throw "Failed to deploy dashboard"
        }
        Write-Host "✅ Dashboard deployed" -ForegroundColor Green
    } catch {
        Write-Host "❌ Failed to deploy dashboard: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
}

# Wait for dashboard to be ready
Write-Host "Waiting for dashboard to be ready..." -ForegroundColor Blue
try {
    kubectl wait --for=condition=available --timeout=120s deployment/kubernetes-dashboard -n kubernetes-dashboard
    Write-Host "✅ Dashboard is ready" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Dashboard may still be starting, continuing..." -ForegroundColor Yellow
}

# Create admin service account
Write-Host "Creating admin service account..." -ForegroundColor Blue

# Check if service account exists
$existingSA = kubectl get serviceaccount dashboard-admin-sa -n kubernetes-dashboard 2>$null
if ($existingSA) {
    Write-Host "✅ Admin service account already exists" -ForegroundColor Green
} else {
    try {
        # Create service account
        kubectl create serviceaccount dashboard-admin-sa -n kubernetes-dashboard
        Write-Host "✅ Admin service account created" -ForegroundColor Green
    } catch {
        Write-Host "⚠️  Failed to create service account, continuing..." -ForegroundColor Yellow
    }
}

# Check if cluster role binding exists
$existingBinding = kubectl get clusterrolebinding dashboard-admin-sa 2>$null
if ($existingBinding) {
    Write-Host "✅ Cluster role binding already exists" -ForegroundColor Green
} else {
    try {
        # Create cluster role binding
        kubectl create clusterrolebinding dashboard-admin-sa --clusterrole=cluster-admin --serviceaccount=kubernetes-dashboard:dashboard-admin-sa
        Write-Host "✅ Cluster role binding created" -ForegroundColor Green
    } catch {
        Write-Host "⚠️  Failed to create cluster role binding, continuing..." -ForegroundColor Yellow
    }
}

# Generate access token
Write-Host "Generating access token..." -ForegroundColor Blue
try {
    $token = kubectl create token dashboard-admin-sa -n kubernetes-dashboard
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Access token generated" -ForegroundColor Green
    } else {
        throw "Failed to generate token"
    }
} catch {
    Write-Host "❌ Failed to generate access token" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "🎉 Kubernetes Dashboard setup complete!" -ForegroundColor Green
Write-Host ""
Write-Host "📝 Access Instructions:" -ForegroundColor Blue
Write-Host ""
Write-Host "1. Start the proxy (run in a separate terminal):" -ForegroundColor White
Write-Host "   kubectl proxy" -ForegroundColor Cyan
Write-Host ""
Write-Host "2. Open the dashboard in your browser:" -ForegroundColor White
Write-Host "   http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/" -ForegroundColor Cyan
Write-Host ""
Write-Host "3. Login with this token (copy and paste):" -ForegroundColor White
Write-Host ""
Write-Host $token -ForegroundColor Yellow
Write-Host ""
Write-Host "💡 Tips:" -ForegroundColor Blue
Write-Host "   • Keep the proxy running while using the dashboard" -ForegroundColor White
Write-Host "   • Use the token to authenticate when prompted" -ForegroundColor White
Write-Host "   • You can regenerate the token anytime with:" -ForegroundColor White
Write-Host "     kubectl create token dashboard-admin-sa -n kubernetes-dashboard" -ForegroundColor Cyan
Write-Host ""

# Save token to file for later use
$tokenFile = "dashboard-token.txt"
$token | Out-File -FilePath $tokenFile -Encoding UTF8
Write-Host "🔑 Token saved to: $tokenFile" -ForegroundColor Green
Write-Host "" 