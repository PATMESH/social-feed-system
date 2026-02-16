#!/bin/bash
set -e

echo "Building graph-service Docker image..."

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

docker build -t patmeshs/graph-service:latest .

echo " graph-service image built successfully!"
echo "  Image: patmeshs/graph-service:latest"
