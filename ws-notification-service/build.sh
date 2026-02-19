#!/bin/bash
set -e

echo "Building ws-notification-service Docker image..."

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

docker build -t patmeshs/ws-notification:latest .

echo " ws-notification-service image built successfully!"
echo "  Image: patmeshs/ws-notification:latest"
