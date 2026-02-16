# K3s Local Development Setup

Simple, one-command deployment of the entire social-feed-system on your local macOS machine using k3d (k3s in Docker).

## 🎯 What This Does

Deploys **all microservices** with minimal resources (optimized for 6GB RAM):

**Platform Services:**
- PostgreSQL (with 3 databases)
- Redis
- Apache Kafka (KRaft mode, official Apache image)
- Cassandra (JanusGraph backend)
- JanusGraph Server

**Application Services:**
- API Gateway (exposed on `localhost:8080`)
- Auth Service
- Post Service
- Graph Service
- Notification Service
- WebSocket Notification Service

## 📋 Prerequisites

- **macOS** (Intel or Apple Silicon)
- **Docker Desktop** installed and running
  - **Configure 6GB+ RAM**: Docker Desktop → Settings → Resources → Memory → Set to **6GB or 8GB** (Recommended)
- Internet connection for downloading images

## 🚀 Quick Start

**First, build the Docker images:**
```bash
# From project root
./build-all.sh
```

**Then deploy to k3s:**
```bash
cd k3s
./setup.sh
```

That's it! The script will:
1. Install kubectl, k3d if not present
2. Check Docker is running
3. Create a k3d cluster
4. Deploy all platform services (including Cassandra/JanusGraph)
5. Deploy all application services

## ⏱️ Deployment Time

- First run: ~5-10 minutes (includes downloads)
- Subsequent runs: ~3-5 minutes

## 🔍 Verify Deployment

```bash
# Check all pods are running
kubectl get pods

# Check services
kubectl get services

# View logs
kubectl logs -f deployment/api-gateway
kubectl logs -f deployment/graph-service
```

## 🌐 Access Services

### API Gateway (Main Entry Point)
```
http://localhost:8080
```

### Direct Service Access (via port-forward)
```bash
# Auth Service
kubectl port-forward svc/auth-service 8991:8991

# Graph Service
kubectl port-forward svc/graph-service 8992:8992
# JanusGraph (Gremlin)
kubectl port-forward svc/janusgraph 8182:8182

# Post Service
kubectl port-forward svc/post-service 8993:8993

# Notification Service
kubectl port-forward svc/notification-service 8994:8994

# PostgreSQL (for debugging)
kubectl port-forward svc/postgres 5432:5432
# Connect: psql -h localhost -U socialfeed -d social_auth

# Redis (for debugging)
kubectl port-forward svc/redis 6379:6379
```

## 📊 Resource Usage

| Service | Memory Request | Memory Limit | CPU Request | CPU Limit |
|---------|----------------|--------------|-------------|-----------|
| PostgreSQL | 256Mi | 512Mi | 100m | 500m |
| Redis | 64Mi | 128Mi | 50m | 200m |
| Kafka | 384Mi | 512Mi | 100m | 500m |
| Cassandra | 768Mi | 1Gi | 250m | 1000m |
| JanusGraph | 512Mi | 1Gi | 250m | 1000m |
| API Gateway | 256Mi | 512Mi | 100m | 500m |
| Auth Service | 256Mi | 512Mi | 100m | 500m |
| Post Service | 256Mi | 512Mi | 100m | 500m |
| Graph Service | 256Mi | 512Mi | 100m | 500m |
| Notification | 196Mi | 384Mi | 50m | 300m |
| WS Notification | 128Mi | 256Mi | 50m | 200m |

**Total Request:** ~3.3 GB
**Total Limit:** ~5.8 GB

> **Note:** With 6GB RAM, the cluster is **at capacity**. Closing other apps is recommended. If unstable, increase Docker RAM to 8GB.

## 🛠️ Troubleshooting

### Pods not starting
```bash
# Check pod status
kubectl describe pod <pod-name>

# Check recent events
kubectl get events --sort-by='.lastTimestamp'
```

### Docker not running
```
Error: Cannot connect to Docker daemon
```
**Solution:** Start Docker Desktop

### Port 8080 already in use
```bash
# Find what's using the port
lsof -i :8080

# Kill the process or change the port in setup.sh
```

### Out of memory
```bash
# Check Docker resources
docker info | grep Memory

# Increase Docker memory in Docker Desktop > Settings > Resources
```

### Kafka fails to start
Kafka takes ~60-90 seconds to become ready. If it fails:
```bash
kubectl logs deployment/kafka
kubectl delete pod -l app=kafka  # Restart it
```

## 🔄 Update Services

To update a service image:
```bash
# Pull new image and restart
kubectl rollout restart deployment/auth-service
kubectl rollout restart deployment/api-gateway
# etc.

# Or redeploy
kubectl apply -f app-services/auth-service.yaml
```

## 🗑️ Clean Up

### Delete everything
```bash
k3d cluster delete social-feed
```

### Keep cluster, delete services
```bash
kubectl delete -f app-services/
kubectl delete -f platform-services/
```

### Restart cluster
```bash
k3d cluster stop social-feed
k3d cluster start social-feed
```

## 📁 Structure

```
k3s/
├── setup.sh                      # One-command setup script
├── README.md                     # This file
├── platform-services/
│   ├── postgres.yaml            # PostgreSQL with 3 databases
│   ├── redis.yaml               # Redis cache
│   └── kafka.yaml               # Kafka message broker
└── app-services/
    ├── api-gateway.yaml         # API Gateway (LoadBalancer)
    ├── auth-service.yaml        # Authentication
    ├── post-service.yaml        # Posts/Content
    ├── notification-service.yaml # Notifications
    └── ws-notification.yaml     # WebSocket server
```

## 🔐 Credentials (Development Only!)

**PostgreSQL:**
- User: `socialfeed`
- Password: `dev123`
- Databases: `social_auth`, `social_posts`, `social_notifications`

**JWT Secret:** `dev-secret-key-change-in-prod`

⚠️ **Never use these in production!**

## 💡 Tips

- **First run:** Be patient, Docker needs to download all images
- **Low on RAM:** Close other applications before running
- **Want to test changes:** Just `kubectl apply -f <file>` after editing
- **Database migrations:** Access via `kubectl exec -it deployment/postgres -- psql -U socialfeed -d social_auth`

## 🎓 Learn More

```bash
# Get cluster info
kubectl cluster-info

# Get all resources
kubectl get all

# Interactive shell in pod
kubectl exec -it deployment/auth-service -- sh

# Monitor resource usage
kubectl top pods
kubectl top nodes
```

---

**Need help?** Check `kubectl describe pod <pod-name>` for detailed status.
