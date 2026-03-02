#!/bin/bash
# Script to run tests inside Docker container

set -e

echo "Running tests in Docker container..."

# Build a test image
docker build -t cart-service-test --target builder .

# Run tests in container
docker run --rm \
  --name cart-service-test-runner \
  cart-service-test \
  mvn test

echo "Tests completed successfully!"

