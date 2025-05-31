#!/bin/bash

#
# LocalStack Initialization Script
#
# Purpose: Automatically initializes AWS resources when LocalStack starts
# When it runs: Triggered automatically by Docker Compose when LocalStack container starts
# Location: Mounted to /etc/localstack/init/ready.d/ in LocalStack container
#
# What it creates:
# - SQS queues for cluster management (cluster-requests, cluster-deletions, cluster-requests-dlq)
# - IAM role with OpenSearch and SQS permissions
#
# Note: This script runs inside the LocalStack container, not on your host machine
#

echo "Initializing LocalStack AWS resources..."

# Wait for services to be available
until curl -s http://localhost:4566/_localstack/health | grep -q '"sqs": "available"' && \
      curl -s http://localhost:4566/_localstack/health | grep -q '"opensearch": "available"'; do
  sleep 2
done

# Create SQS queues
awslocal sqs create-queue --queue-name cluster-requests --attributes VisibilityTimeoutSeconds=300,MessageRetentionPeriod=1209600
awslocal sqs create-queue --queue-name cluster-deletions --attributes VisibilityTimeoutSeconds=300,MessageRetentionPeriod=1209600
awslocal sqs create-queue --queue-name cluster-requests-dlq --attributes VisibilityTimeoutSeconds=300,MessageRetentionPeriod=1209600
awslocal sqs create-queue --queue-name cluster-metrics --attributes VisibilityTimeoutSeconds=300,MessageRetentionPeriod=1209600

# Create basic IAM role for the application
awslocal iam create-role --role-name OpenSearchClusterManager --assume-role-policy-document '{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Principal": {"Service": "ec2.amazonaws.com"},
    "Action": "sts:AssumeRole"
  }]
}'

awslocal iam attach-role-policy --role-name OpenSearchClusterManager --policy-arn arn:aws:iam::aws:policy/AmazonSQSFullAccess
awslocal iam attach-role-policy --role-name OpenSearchClusterManager --policy-arn arn:aws:iam::aws:policy/AmazonOpenSearchServiceFullAccess

echo "✅ LocalStack AWS resources initialized" 