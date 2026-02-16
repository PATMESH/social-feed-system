#!/bin/bash
set -e

echo "Building notification-service Docker image..."

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

docker build -t patmeshs/notification-service:latest .

echo "✓ notification-service image built successfully!"
echo "  Image: patmeshs/notification-service:latest"
