# K3s Quick Reference

## 🚀 Getting Started
```bash
cd k3s
./setup.sh          # One-command deployment
```

## 📊 Check Status
```bash
./status.sh                    # View everything
kubectl get pods              # List all pods
kubectl get services          # List all services
```

## 🔍 View Logs
```bash
kubectl logs -f deployment/api-gateway
kubectl logs -f deployment/auth-service
kubectl logs -f deployment/postgres
```

## 🌐 Access Services
| Service | URL |
|---------|-----|
| API Gateway | http://localhost:8080 |
| Graph Service | `kubectl port-forward svc/graph-service 8992:8992` |
| JanusGraph | `kubectl port-forward svc/janusgraph 8182:8182` |
| Other services | `kubectl port-forward svc/<name> <port>:<port>` |

## 🔧 Common Commands
```bash
# Restart a service
kubectl rollout restart deployment/auth-service

# Update after code change
kubectl apply -f app-services/graph-service.yaml

# Shell into a pod
kubectl exec -it deployment/janusgraph -- sh

# Database access
kubectl exec -it deployment/postgres -- psql -U socialfeed -d social_auth
```

## 🗑️ Cleanup
```bash
./cleanup.sh                  # Delete cluster (with confirmation)
k3d cluster delete social-feed  # Direct delete
```

## 💾 Database Credentials
- **User:** socialfeed
- **Password:** dev123
- **Databases:** social_auth, social_posts, social_notifications

## 📦 Resource Summary
- **Total RAM:** ~3.3GB Request / 5.8GB Limit
- **Storage:** ~2GB (Postgres) + Ephemeral (Cassandra)
- **Services:** 11 total (5 platform + 6 app)
