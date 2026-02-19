#!/bin/bash
set -e

echo "Building post-service Docker image..."

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

docker build -t patmeshs/post-service:latest .

echo " post-service image built successfully!"
echo "  Image: patmeshs/post-service:latest"
