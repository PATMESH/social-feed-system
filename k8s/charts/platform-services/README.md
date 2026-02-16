# Platform Services Helm Chart

This chart deploys the core infrastructure services required for the social-feed-system.

## Services Included

### 1. **PostgreSQL** (Relational Database)
- Multiple databases for different microservices:
  - `social_auth` - Authentication service data
  - `social_posts` - Posts and content data
  - `social_notifications` - Notification data
- Default credentials: `socialfeed/changeme123` (⚠️ Change in production!)
- Persistent storage: 10Gi

### 2. **Redis** (Cache & Session Store)
- Architecture: Standalone (configure `replication` for HA)
- Persistent storage: 5Gi
- Used for: API caching, session management, rate limiting

### 3. **Kafka** (Event Streaming)
- Message broker for async communication between services
- Includes Zookeeper for cluster coordination
- Auto-creates topics on first use
- Persistent storage: 10Gi (Kafka) + 5Gi (Zookeeper)

### 4. **Cassandra** (NoSQL Database)
- Backend storage for JanusGraph
- Persistent storage: 20Gi
- Default credentials: `cassandra/cassandra123` (⚠️ Change in production!)

### 5. **JanusGraph** (Graph Database)
- Graph database for social relationships (followers, friends, etc.)
- Uses Cassandra as storage backend
- Gremlin query interface on port 8182
- Memory: 512Mi-2Gi, CPU: 250m-1000m

## Installation

### Prerequisites
```bash
# Add Bitnami repository
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
```

### Deploy Platform Services
```bash
# From the k8s/charts directory
cd /Users/patmesh/Projects/github/social-feed-system/k8s/charts

# Update dependencies
helm dependency update platform-services

# Install to platform-services namespace
helm install platform-services ./platform-services \
  --namespace platform-services \
  --create-namespace
```

### Verify Deployment
```bash
# Check all pods are running
kubectl get pods -n platform-services

# Check services
kubectl get svc -n platform-services
```

## Service DNS Names

Once deployed, services are accessible at:

- **PostgreSQL**: `platform-services-postgresql.platform-services.svc.cluster.local:5432`
- **Redis**: `platform-services-redis-master.platform-services.svc.cluster.local:6379`
- **Kafka**: `platform-services-kafka.platform-services.svc.cluster.local:9092`
- **Cassandra**: `platform-services-cassandra.platform-services.svc.cluster.local:9042`
- **JanusGraph**: `janusgraph.platform-services.svc.cluster.local:8182`

## Configuration

### Production Recommendations

1. **Enable RBAC and Security**
   - Set Redis auth: `redis.auth.enabled=true`
   - Use Kubernetes Secrets for passwords
   - Enable TLS for Kafka

2. **High Availability**
   - Increase Kafka replicas: `kafka.replicaCount=3`
   - Use Redis replication: `redis.architecture=replication`
   - Increase Cassandra nodes: `cassandra.cluster.replicaCount=3`

3. **Resource Tuning**
   - Adjust resource requests/limits based on workload
   - Configure persistent volume storage class for your cloud provider

4. **Monitoring**
   - Enable Prometheus metrics exporters
   - Set up ServiceMonitors for Prometheus Operator

### Custom Values Example

Create `platform-services-values.yaml`:

```yaml
postgresql:
  auth:
    password: <strong-password>
    postgresPassword: <postgres-admin-password>
  primary:
    persistence:
      storageClass: gp3  # AWS EBS GP3
      size: 50Gi

kafka:
  replicaCount: 3
  persistence:
    storageClass: gp3
    size: 50Gi

cassandra:
  cluster:
    replicaCount: 3
  persistence:
    storageClass: gp3
    size: 100Gi
```

Then install with:
```bash
helm install platform-services ./platform-services \
  -f platform-services-values.yaml \
  --namespace platform-services \
  --create-namespace
```

## Troubleshooting

### Pods Not Starting
```bash
# Check pod events
kubectl describe pod <pod-name> -n platform-services

# Check logs
kubectl logs <pod-name> -n platform-services
```

### Persistent Volume Issues
- Ensure your cluster has a default StorageClass
- Or specify `global.storageClass` in values.yaml

### JanusGraph Connection Issues
- Ensure Cassandra is fully running before JanusGraph starts
- Check Cassandra logs: `kubectl logs platform-services-cassandra-0 -n platform-services`

## Uninstall
```bash
helm uninstall platform-services --namespace platform-services

# Clean up PVCs if needed
kubectl delete pvc -n platform-services --all
```
