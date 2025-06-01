#
# Environment Setup Script (PowerShell)
#
# Purpose: One-time setup of Windows environment for Docker Desktop and WSL2
# When to run: Run this ONCE before using the demo setup script
# Prerequisites: Run as Administrator
#
# What it does:
# 1. Checks if running as Administrator
# 2. Enables required Windows features (WSL, Virtual Machine Platform, Hyper-V)
# 3. Installs and configures WSL2
# 4. Checks Docker Desktop installation and service
# 5. Validates all tools are available
#
# Usage: Right-click PowerShell, "Run as Administrator", then: .\scripts\setup-environment.ps1
#
# After completion:
# - Restart your computer if prompted
# - WSL2 will be ready for Docker Desktop
# - All required tools will be validated
#

Write-Host "Setting up Windows Environment for OpenSearch SQS Demo..." -ForegroundColor Green

# Check if running as Administrator
Write-Host "Checking if running as Administrator..." -ForegroundColor Blue
$currentPrincipal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
$isAdmin = $currentPrincipal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host "❌ This script must be run as Administrator to enable Windows features" -ForegroundColor Red
    Write-Host "   Please right-click PowerShell and select 'Run as Administrator'" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Running as Administrator" -ForegroundColor Green

# Enable required Windows features
Write-Host "Enabling required Windows features..." -ForegroundColor Blue

$restartRequired = $false

# Enable WSL
Write-Host "  Enabling Windows Subsystem for Linux..." -ForegroundColor Cyan
$wslResult = dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart
if ($LASTEXITCODE -eq 3010) {
    $restartRequired = $true
    Write-Host "    ✅ WSL enabled (restart required)" -ForegroundColor Yellow
} elseif ($LASTEXITCODE -eq 0) {
    Write-Host "    ✅ WSL already enabled" -ForegroundColor Green
} else {
    Write-Host "    ❌ Failed to enable WSL feature" -ForegroundColor Red
    exit 1
}

# Enable Virtual Machine Platform
Write-Host "  Enabling Virtual Machine Platform..." -ForegroundColor Cyan
$vmpResult = dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart
if ($LASTEXITCODE -eq 3010) {
    $restartRequired = $true
    Write-Host "    ✅ Virtual Machine Platform enabled (restart required)" -ForegroundColor Yellow
} elseif ($LASTEXITCODE -eq 0) {
    Write-Host "    ✅ Virtual Machine Platform already enabled" -ForegroundColor Green
} else {
    Write-Host "    ❌ Failed to enable Virtual Machine Platform" -ForegroundColor Red
    exit 1
}

# Enable Hyper-V (optional, may not be available on all systems)
Write-Host "  Enabling Hyper-V (if available)..." -ForegroundColor Cyan
$hypervResult = dism.exe /online /enable-feature /featurename:Microsoft-Hyper-V-All /all /norestart
if ($LASTEXITCODE -eq 3010) {
    $restartRequired = $true
    Write-Host "    ✅ Hyper-V enabled (restart required)" -ForegroundColor Yellow
} elseif ($LASTEXITCODE -eq 0) {
    Write-Host "    ✅ Hyper-V already enabled or not applicable" -ForegroundColor Green
} else {
    Write-Host "    ⚠️  Hyper-V not available (continuing without it)" -ForegroundColor Yellow
}

Write-Host "✅ Windows features configuration complete" -ForegroundColor Green

# Check if restart is required
if ($restartRequired) {
    Write-Host ""
    Write-Host "🔄 RESTART REQUIRED!" -ForegroundColor Red
    Write-Host "   Windows features have been enabled but require a restart to take effect." -ForegroundColor Yellow
    Write-Host "   Please restart your computer and then run this script again to complete setup." -ForegroundColor Yellow
    Write-Host ""
    $restart = Read-Host "Would you like to restart now? (y/n)"
    if ($restart -eq "y" -or $restart -eq "Y") {
        Restart-Computer -Force
    }
    exit 0
}

# If no restart required, continue with WSL2 setup
Write-Host "Configuring WSL2..." -ForegroundColor Blue

# Set WSL 2 as default version
Write-Host "  Setting WSL2 as default version..." -ForegroundColor Cyan
wsl --set-default-version 2
if ($LASTEXITCODE -eq 0) {
    Write-Host "    ✅ WSL2 set as default" -ForegroundColor Green
} else {
    Write-Host "    ⚠️  Could not set WSL2 as default (may need kernel update)" -ForegroundColor Yellow
}

# Check if WSL distributions are installed
Write-Host "  Checking WSL distributions..." -ForegroundColor Cyan
$wslDistros = wsl --list --verbose 2>$null
if ($LASTEXITCODE -ne 0 -or $wslDistros -match "no installed distributions") {
    Write-Host "    Installing Ubuntu distribution..." -ForegroundColor Yellow
    $installResult = wsl --install -d Ubuntu --no-launch 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "    ✅ Ubuntu distribution installed" -ForegroundColor Green
    } else {
        Write-Host "    ⚠️  WSL2 installation may have issues" -ForegroundColor Yellow
        Write-Host "       You may need to download the WSL2 kernel update manually:" -ForegroundColor Yellow
        Write-Host "       https://wslstorestorage.blob.core.windows.net/wslblob/wsl_update_x64.msi" -ForegroundColor Cyan
    }
} else {
    Write-Host "    ✅ WSL distributions already installed" -ForegroundColor Green
}

# Check tool prerequisites
Write-Host "Validating required tools..." -ForegroundColor Blue

$missingTools = @()

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    $missingTools += "Docker Desktop"
}

if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) {
    $missingTools += "kubectl"
}

if (-not (Get-Command kind -ErrorAction SilentlyContinue)) {
    $missingTools += "kind"
}

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    $missingTools += "Maven"
}

if (-not (Get-Command awslocal -ErrorAction SilentlyContinue)) {
    $missingTools += "awslocal (pip install awscli-local)"
}

# Optional but recommended tools
$optionalTools = @()

if (-not (Get-Command k9s -ErrorAction SilentlyContinue)) {
    $optionalTools += "k9s (choco install k9s) - Terminal UI for Kubernetes"
}

if (-not (Get-Command octant -ErrorAction SilentlyContinue)) {
    $optionalTools += "octant (choco install octant) - Web UI for Kubernetes"
}

if ($missingTools.Count -gt 0) {
    Write-Host "⚠️  Missing required tools:" -ForegroundColor Yellow
    $missingTools | ForEach-Object { Write-Host "   - $_" -ForegroundColor Yellow }
    Write-Host ""
    Write-Host "Installation guides:" -ForegroundColor Blue
    Write-Host "  Docker Desktop: https://docs.docker.com/desktop/install/windows/" -ForegroundColor Cyan
    Write-Host "  kubectl: choco install kubernetes-cli" -ForegroundColor Cyan
    Write-Host "  kind: choco install kind" -ForegroundColor Cyan
    Write-Host "  Maven: choco install maven" -ForegroundColor Cyan
    Write-Host "  awslocal: pip install awscli-local" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Please install missing tools and run setup-demo.ps1 when ready." -ForegroundColor Yellow
} else {
    Write-Host "✅ All required tools are installed" -ForegroundColor Green
}

if ($optionalTools.Count -gt 0) {
    Write-Host ""
    Write-Host "💡 Recommended Kubernetes visualization tools:" -ForegroundColor Blue
    $optionalTools | ForEach-Object { Write-Host "   - $_" -ForegroundColor Cyan }
}

# Check Docker service status
Write-Host "Checking Docker Desktop..." -ForegroundColor Blue

$dockerService = Get-Service -Name "com.docker.service" -ErrorAction SilentlyContinue
if ($dockerService) {
    if ($dockerService.Status -ne "Running") {
        Write-Host "  Docker service is not running, attempting to start..." -ForegroundColor Yellow
        try {
            Start-Service -Name "com.docker.service"
            Write-Host "  ✅ Docker service started" -ForegroundColor Green
        } catch {
            Write-Host "  ❌ Failed to start Docker service: $($_.Exception.Message)" -ForegroundColor Red
            Write-Host "     Please start Docker Desktop manually" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  ✅ Docker service is running" -ForegroundColor Green
    }
    
    # Test Docker functionality
    Write-Host "  Testing Docker functionality..." -ForegroundColor Cyan
    $maxWait = 30  # seconds
    $waited = 0
    
    do {
        try {
            $dockerInfo = docker info 2>$null
            if ($LASTEXITCODE -eq 0) {
                Write-Host "  ✅ Docker Desktop is ready" -ForegroundColor Green
                break
            }
        } catch {
            # Continue waiting
        }
        
        Start-Sleep 2
        $waited += 2
        
        if ($waited -ge $maxWait) {
            Write-Host "  ⚠️  Docker Desktop may not be fully ready" -ForegroundColor Yellow
            Write-Host "     Please ensure Docker Desktop is running before starting the demo" -ForegroundColor Yellow
            break
        }
    } while ($true)
} else {
    Write-Host "  ⚠️  Docker Desktop service not found" -ForegroundColor Yellow
    Write-Host "     Please install Docker Desktop from: https://docs.docker.com/desktop/install/windows/" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "🎉 Environment setup complete!" -ForegroundColor Green
Write-Host ""

if ($missingTools.Count -eq 0) {
    Write-Host "✅ Your environment is ready for the demo!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📝 Next steps:" -ForegroundColor Blue
    Write-Host "   1. Ensure Docker Desktop is running" -ForegroundColor White
    Write-Host "   2. Run the demo setup: .\scripts\setup-demo.ps1" -ForegroundColor White
} else {
    Write-Host "⚠️  Please install the missing tools listed above" -ForegroundColor Yellow
    Write-Host "   Then run: .\scripts\setup-demo.ps1" -ForegroundColor White
}

Write-Host "" 