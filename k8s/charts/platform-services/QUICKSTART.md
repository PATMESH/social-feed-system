# Platform Services - Quick Deployment Guide

## 📦 What's Deployed

| Service | Purpose | Port | DNS Name |
|---------|---------|------|----------|
| **PostgreSQL** | Relational DB (auth, posts, notifications) | 5432 | `platform-services-postgresql.platform-services.svc.cluster.local` |
| **Redis** | Cache & sessions | 6379 | `platform-services-redis-master.platform-services.svc.cluster.local` |
| **Kafka** | Event streaming | 9092 | `platform-services-kafka.platform-services.svc.cluster.local` |
| **Cassandra** | JanusGraph backend | 9042 | `platform-services-cassandra.platform-services.svc.cluster.local` |
| **JanusGraph** | Graph database | 8182 | `janusgraph.platform-services.svc.cluster.local` |

## 🚀 Quick Deploy

```bash
# 1. Add Bitnami repo (one-time)
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# 2. Update chart dependencies
cd /Users/patmesh/Projects/github/social-feed-system/k8s/charts
helm dependency update platform-services

# 3. Install
helm install platform-services ./platform-services \
  --namespace platform-services \
  --create-namespace

# 4. Watch pods start
kubectl get pods -n platform-services -w
```

## ✅ Verify Deployment

```bash
# Check all services
kubectl get all -n platform-services

# Test PostgreSQL
kubectl run -it --rm psql-test --image=postgres:15 --restart=Never -n platform-services -- \
  psql -h platform-services-postgresql -U socialfeed -d social_auth

# Test Redis
kubectl run -it --rm redis-test --image=redis:7 --restart=Never -n platform-services -- \
  redis-cli -h platform-services-redis-master ping

# Test Kafka
kubectl run -it --rm kafka-test --image=bitnami/kafka:3.4 --restart=Never -n platform-services -- \
  kafka-topics.sh --list --bootstrap-server platform-services-kafka:9092
```

## 🔐 Default Credentials (⚠️ CHANGE IN PRODUCTION!)

- **PostgreSQL**: `socialfeed / changeme123`
- **Cassandra**: `cassandra / cassandra123`
- **Redis**: No auth (enable with `redis.auth.enabled=true`)

## 📊 Resource Usage (Minimum)

- **CPU**: ~2.5 cores total
- **Memory**: ~6 GB total  
- **Storage**: ~55 GB persistent volumes

## 🔧 Common Operations

### Scale Kafka for production
```bash
helm upgrade platform-services ./platform-services \
  --set kafka.replicaCount=3 \
  --namespace platform-services
```

### Enable Redis auth
```bash
helm upgrade platform-services ./platform-services \
  --set redis.auth.enabled=true \
  --set redis.auth.password=your-secure-password \
  --namespace platform-services
```

### Uninstall
```bash
helm uninstall platform-services -n platform-services
kubectl delete pvc -n platform-services --all  # Clean up volumes
```
