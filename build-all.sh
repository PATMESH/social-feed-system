#!/bin/bash
set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Building All Microservices${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Check if Docker is running
if ! docker info >/dev/null 2>&1; then
    echo -e "${RED}Error: Docker is not running. Please start Docker Desktop.${NC}"
    exit 1
fi

# Services to build
SERVICES=(
    "api-gateway-service"
    "user-auth-service"
    "user-post-service"
    "graph-service"
    "notification-service"
    "ws-notification-service"
)

FAILED_BUILDS=()
SUCCESSFUL_BUILDS=()

for service in "${SERVICES[@]}"; do
    echo -e "${YELLOW}Building $service...${NC}"
    
    if [ -f "$SCRIPT_DIR/$service/build.sh" ]; then
        if bash "$SCRIPT_DIR/$service/build.sh"; then
            SUCCESSFUL_BUILDS+=("$service")
            echo -e "${GREEN}✓ $service built successfully${NC}"
        else
            FAILED_BUILDS+=("$service")
            echo -e "${RED}✗ $service build failed${NC}"
        fi
    else
        echo -e "${RED}✗ build.sh not found for $service${NC}"
        FAILED_BUILDS+=("$service")
    fi
    echo ""
done

# Summary
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Build Summary${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

if [ ${#SUCCESSFUL_BUILDS[@]} -gt 0 ]; then
    echo -e "${GREEN}Successful builds (${#SUCCESSFUL_BUILDS[@]}):${NC}"
    for service in "${SUCCESSFUL_BUILDS[@]}"; do
        echo -e "  ${GREEN}✓${NC} $service"
    done
    echo ""
fi

if [ ${#FAILED_BUILDS[@]} -gt 0 ]; then
    echo -e "${RED}Failed builds (${#FAILED_BUILDS[@]}):${NC}"
    for service in "${FAILED_BUILDS[@]}"; do
        echo -e "  ${RED}✗${NC} $service"
    done
    echo ""
    exit 1
fi

echo -e "${GREEN}All services built successfully! 🚀${NC}"
echo ""
echo -e "${BLUE}Docker images created:${NC}"
docker images | grep "patmeshs" | head -5

echo ""
echo -e "${BLUE}Next steps:${NC}"
echo -e "  1. Run the k3s setup: ${YELLOW}cd k3s && ./setup.sh${NC}"
echo -e "  2. Check pod status: ${YELLOW}kubectl get pods${NC}"
