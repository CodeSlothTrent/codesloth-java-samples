#!/bin/bash

set -e

echo "Sending test messages to SQS..."

# Check if awslocal is available
if ! command -v awslocal &> /dev/null; then
    echo "Error: awslocal is not installed. Please install awscli-local:"
    echo "pip install awscli-local"
    exit 1
fi

# Test message for Elasticsearch cluster creation
CLUSTER_REQUEST='{
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
  "configuration": {
    "cluster.name": "test-cluster",
    "discovery.type": "single-node"
  },
  "requestId": "test-request-001",
  "requestedAt": "2024-01-15T10:30:00Z",
  "requestedBy": "test-user"
}'

echo "Sending cluster creation request..."
awslocal sqs send-message \
  --queue-url http://localhost:4566/000000000000/cluster-requests \
  --message-body "$CLUSTER_REQUEST"

echo "Cluster creation request sent successfully!"

# Test message for cluster deletion
DELETION_REQUEST='{
  "clusterName": "test-elasticsearch-cluster",
  "namespace": "default"
}'

echo "Waiting 5 seconds before sending deletion request..."
sleep 5

echo "Sending cluster deletion request..."
awslocal sqs send-message \
  --queue-url http://localhost:4566/000000000000/cluster-deletions \
  --message-body "$DELETION_REQUEST"

echo "Cluster deletion request sent successfully!"

# Show queue status
echo ""
echo "Queue status:"
echo "============="

echo "Cluster requests queue:"
awslocal sqs get-queue-attributes \
  --queue-url http://localhost:4566/000000000000/cluster-requests \
  --attribute-names ApproximateNumberOfMessages

echo "Cluster deletions queue:"
awslocal sqs get-queue-attributes \
  --queue-url http://localhost:4566/000000000000/cluster-deletions \
  --attribute-names ApproximateNumberOfMessages

echo ""
echo "Test messages sent successfully!"
echo "Check your application logs to see the message processing." 