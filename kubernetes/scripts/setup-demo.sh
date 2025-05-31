#!/bin/bash

#
# AWS OpenSearch SQS Demo Setup Script
#
# Purpose: Complete environment setup for the OpenSearch SQS demo
# When to run: Execute this manually to set up the entire demo environment
# Prerequisites: docker, kubectl, kind, awslocal (pip install awscli-local)
#
# What it does:
# 1. Validates all required tools are installed
# 2. Creates a local Kubernetes cluster using kind
# 3. Starts LocalStack with SQS and OpenSearch services
# 4. Builds the Spring Boot application
# 5. Creates Docker image and deploys to Kubernetes
# 6. Waits for everything to be ready
#
# Usage: ./scripts/setup-demo.sh
#
# After completion:
# - Spring Boot app runs in Kubernetes pod
# - App listens to SQS messages from LocalStack
# - App creates OpenSearch clusters in LocalStack via AWS SDK
#

echo "Setting up AWS OpenSearch SQS Demo..."

# Check prerequisites
echo "Checking prerequisites..."

if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed"
    exit 1
fi

if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl is not installed"
    exit 1
fi

if ! command -v kind &> /dev/null; then
    echo "❌ kind is not installed"
    exit 1
fi

if ! command -v awslocal &> /dev/null; then
    echo "❌ awslocal is not installed (pip install awscli-local)"
    exit 1
fi

echo "✅ All prerequisites are installed"

# Create kind cluster
echo "Creating kind cluster..."
kind create cluster --name opensearch-sqs-demo --config - <<EOF
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 80
    hostPort: 8080
    protocol: TCP
EOF

# Set kubectl context
kubectl config use-context kind-opensearch-sqs-demo

# Start LocalStack
echo "Starting LocalStack..."
docker-compose up -d

# Wait for LocalStack initialization to complete
echo "Waiting for LocalStack initialization to complete..."
sleep 10

# Check if LocalStack is ready and initialized
until curl -s http://localhost:4566/_localstack/health | grep -q '"opensearch": "available"' && \
      awslocal sqs list-queues | grep -q "cluster-requests"; do
  echo "Waiting for LocalStack services and initialization..."
  sleep 5
done

echo "✅ LocalStack is ready and initialized"

# Build application
echo "Building Spring Boot application..."
mvn clean package -DskipTests

# Build Docker image
echo "Building Docker image..."
docker build -t opensearch-sqs-demo:latest .

# Load image into kind cluster
echo "Loading image into kind cluster..."
kind load docker-image opensearch-sqs-demo:latest --name opensearch-sqs-demo

# Deploy to Kubernetes
echo "Deploying application to Kubernetes..."
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/deployment.yaml

# Wait for deployment to be ready
echo "Waiting for deployment to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/opensearch-sqs-demo -n opensearch-sqs-demo

echo "✅ Setup completed successfully!"
echo ""
echo "Application is running in Kubernetes and ready to process SQS messages"
echo ""
echo "To test:"
echo "1. Send SQS message to create OpenSearch cluster"
echo "2. Check application logs: kubectl logs -f deployment/opensearch-sqs-demo -n opensearch-sqs-demo"
echo "3. Access application: kubectl port-forward -n opensearch-sqs-demo service/opensearch-sqs-demo-service 8080:80"
echo ""
echo "LocalStack services available at: http://localhost:4566" 