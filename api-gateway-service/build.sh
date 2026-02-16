#!/bin/bash
set -e

echo "Building api-gateway Docker image..."

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

docker build -t patmeshs/api-gateway:latest .

echo "✓ api-gateway image built successfully!"
echo "  Image: patmeshs/api-gateway:latest"
