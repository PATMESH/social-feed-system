#!/bin/bash
set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Social Feed System - K3s Local Setup${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

if [[ "$OSTYPE" != "darwin"* ]]; then
    echo -e "${RED}Error: This script is only for macOS${NC}"
    exit 1
fi

command_exists() {
    command -v "$1" >/dev/null 2>&1
}

echo -e "${YELLOW}[1/7] Checking Homebrew...${NC}"
if ! command_exists brew; then
    echo -e "${YELLOW}Installing Homebrew...${NC}"
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
else
    echo -e "${GREEN}✓ Homebrew already installed${NC}"
fi

echo -e "${YELLOW}[2/7] Checking kubectl...${NC}"
if ! command_exists kubectl; then
    echo -e "${YELLOW}Installing kubectl...${NC}"
    brew install kubectl
else
    echo -e "${GREEN}✓ kubectl already installed${NC}"
fi

echo -e "${YELLOW}[3/7] Checking k3d...${NC}"
if ! command_exists k3d; then
    echo -e "${YELLOW}Installing k3d...${NC}"
    brew install k3d
else
    echo -e "${GREEN}✓ k3d already installed${NC}"
fi

echo -e "${YELLOW}[4/7] Checking Docker...${NC}"
if ! command_exists docker; then
    echo -e "${YELLOW}Installing Docker via Homebrew...${NC}"
    brew install --cask docker
    echo -e "${YELLOW}Please start Docker Desktop and run this script again${NC}"
    exit 1
fi

if ! docker info >/dev/null 2>&1; then
    echo -e "${RED}Docker is installed but not running. Please start Docker Desktop and try again.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker is running${NC}"

echo -e "${BLUE}Note: Ensure Docker Desktop is configured with at least 6GB RAM${NC}"
echo -e "${BLUE}      (Docker Desktop > Settings > Resources > Memory)${NC}"
echo ""

echo -e "${YELLOW}[5/7] Creating k3d cluster...${NC}"
if k3d cluster list | grep -q "social-feed"; then
    echo -e "${YELLOW}Cluster already exists. Deleting and recreating...${NC}"
    k3d cluster delete social-feed
fi

k3d cluster create social-feed \
    --agents 0 \
    --port "8080:80@loadbalancer" \
    --k3s-arg "--disable=traefik@server:0" \
    --k3s-arg "--disable=metrics-server@server:0"

echo -e "${GREEN}✓ K3d cluster created${NC}"

# Wait for cluster to be ready
echo -e "${YELLOW}Waiting for cluster to be ready...${NC}"
kubectl wait --for=condition=Ready nodes --all --timeout=120s

echo -e "${YELLOW}[6/7] Deploying platform services...${NC}"
kubectl apply -f platform-services/postgres.yaml
kubectl apply -f platform-services/redis.yaml
kubectl apply -f platform-services/kafka.yaml

# Check GRAPH_IMPL env var, default to gremlin
GRAPH_IMPL=${GRAPH_IMPL:-neo4j}
echo -e "${BLUE}Graph implementation selected: ${GRAPH_IMPL}${NC}"

if [ "$GRAPH_IMPL" = "neo4j" ]; then
    echo -e "${YELLOW}Deploying Neo4j...${NC}"
    kubectl apply -f platform-services/neo4j.yaml
else
    echo -e "${YELLOW}Deploying JanusGraph and Cassandra...${NC}"
    kubectl apply -f platform-services/cassandra.yaml
    kubectl apply -f platform-services/janusgraph.yaml
fi

echo -e "${YELLOW}Waiting for platform services to be ready...${NC}"
kubectl wait --for=condition=Ready pod -l app=postgres --timeout=180s
kubectl wait --for=condition=Ready pod -l app=redis --timeout=120s
kubectl wait --for=condition=Ready pod -l app=kafka --timeout=180s

if [ "$GRAPH_IMPL" = "neo4j" ]; then
    kubectl wait --for=condition=Ready pod -l app=neo4j --timeout=300s
else
    kubectl wait --for=condition=Ready pod -l app=cassandra --timeout=300s
    kubectl wait --for=condition=Ready pod -l app=janusgraph --timeout=300s
fi

echo -e "${GREEN}✓ Platform services deployed${NC}"

echo -e "${YELLOW}[7/7] Deploying application services...${NC}"

echo -e "${YELLOW}Importing local Docker images into k3d...${NC}"

k3d image import patmeshs/api-gateway:latest -c social-feed
k3d image import patmeshs/auth-service:latest -c social-feed
k3d image import patmeshs/post-service:latest -c social-feed
k3d image import patmeshs/notification-service:latest -c social-feed
k3d image import patmeshs/ws-notification:latest -c social-feed
k3d image import patmeshs/graph-service:latest -c social-feed

echo -e "${GREEN}✓ Images imported into k3d${NC}"

openssl genrsa -out private.pem 2048
openssl rsa -in private.pem -pubout -out public.pem

cat private.pem | sed -e '1d' -e '$d' | tr -d '\n' > private_base64.txt
cat public.pem | sed -e '1d' -e '$d' | tr -d '\n' > public_base64.txt

kubectl create secret generic jwt-keys \
  --from-file=JWT_RSA_PRIVATE_KEY=private_base64.txt \
  --from-file=JWT_RSA_PUBLIC_KEY=public_base64.txt \
  --dry-run=client -o yaml | kubectl apply -f -

kubectl apply -f app-services/auth-service.yaml
kubectl apply -f app-services/post-service.yaml
kubectl apply -f app-services/notification-service.yaml
kubectl apply -f app-services/ws-notification.yaml
kubectl apply -f app-services/api-gateway.yaml

# Deploy graph-service with correct GRAPH_IMPL
echo -e "${YELLOW}Deploying graph-service with GRAPH_IMPL=${GRAPH_IMPL}...${NC}"
sed "s/value: \"gremlin\"/value: \"$GRAPH_IMPL\"/g" app-services/graph-service.yaml | kubectl apply -f -


echo -e "${YELLOW}Waiting for application services to start...${NC}"
sleep 10

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Deployment Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${BLUE}Cluster Information:${NC}"
echo -e "Cluster Name: ${GREEN}social-feed${NC}"
echo -e "API Gateway: ${GREEN}http://localhost:8080${NC}"
echo ""
echo -e "${BLUE}Check status:${NC}"
echo -e "  ${YELLOW}kubectl get pods${NC}"
echo -e "  ${YELLOW}kubectl get services${NC}"
echo ""
echo -e "${BLUE}View logs:${NC}"
echo -e "  ${YELLOW}kubectl logs -f deployment/api-gateway${NC}"
echo -e "  ${YELLOW}kubectl logs -f deployment/auth-service${NC}"
echo ""
echo -e "${BLUE}Access services:${NC}"
echo -e "  API Gateway:    ${GREEN}http://localhost:8080${NC}"
echo -e "  Auth Service:   ${YELLOW}kubectl port-forward svc/auth-service 8991:8991${NC}"
echo -e "  Post Service:   ${YELLOW}kubectl port-forward svc/post-service 8993:8993${NC}"
echo ""
echo -e "${BLUE}Delete cluster:${NC}"
echo -e "  ${YELLOW}k3d cluster delete social-feed${NC}"
echo ""
echo -e "${GREEN}Happy coding! 🚀${NC}"
