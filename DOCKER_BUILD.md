# Docker Build Guide

This guide explains how to build Docker images for all microservices.

## Quick Start

```bash
# Build all services at once
./build-all.sh
```

## What Gets Built

The script builds Docker images for all 5 microservices:

| Service | Image | Port |
|---------|-------|------|
| API Gateway | `patmeshs/api-gateway:latest` | 8990 |
| Auth Service | `patmeshs/auth-service:latest` | 8991 |
| Post Service | `patmeshs/post-service:latest` | 8993 |
| Notification Service | `patmeshs/notification-service:latest` | 8994 |
| WebSocket Notification | `patmeshs/ws-notification:latest` | 8995 |

## Building Individual Services

Each service has its own `build.sh` script:

```bash
# Build only auth service
cd user-auth-service
./build.sh

# Build only api-gateway
cd api-gateway-service
./build.sh
```

## Dockerfile Features

All Dockerfiles use **multi-stage builds** for optimized image sizes:

### Build Stage (maven:3.9-eclipse-temurin-21-alpine)
- Downloads Maven dependencies (cached layer)
- Compiles Java source code
- Creates JAR file
- Skips tests for faster builds

### Runtime Stage (eclipse-temurin:21-jre-alpine)
- Minimal JRE-only image (~200MB vs 500MB+ with JDK)
- Non-root user for security
- Only includes compiled JAR

### Benefits
- ✅ **Smaller images**: ~250-300MB instead of 500MB+
- ✅ **Faster deployments**: Less data to transfer
- ✅ **Cached layers**: Maven dependencies cached separately
- ✅ **Security**: Non-root user, minimal attack surface

## Workflow

### First Time Setup
```bash
# 1. Build all Docker images
./build-all.sh

# 2. Deploy to k3s
cd k3s
./setup.sh

# 3. Verify deployment
kubectl get pods
```

### After Code Changes
```bash
# Option 1: Rebuild specific service
cd user-auth-service
./build.sh
kubectl rollout restart deployment/auth-service

# Option 2: Rebuild all services
./build-all.sh
cd k3s
kubectl delete -f app-services/
kubectl apply -f app-services/
```

## Troubleshooting

### Docker not running
```
Error: Docker is not running
```
**Fix**: Start Docker Desktop

### Maven build fails
```
[ERROR] Failed to execute goal
```
**Fix**: Check Java version and dependencies
```bash
java -version  # Should be Java 21
cd <service-directory>
mvn clean install
```

### Out of disk space
```bash
# Clean up old images
docker system prune -a

# Or remove specific images
docker rmi patmeshs/auth-service:latest
```

### Build is slow
- First build takes 5-10 minutes (downloads dependencies)
- Subsequent builds are faster due to layer caching
- Use individual `build.sh` for faster iteration

## Advanced: Customize Builds

### Change image repository
Edit each service's `build.sh`:
```bash
# Instead of patmeshs/service-name
docker build -t YOUR_DOCKERHUB/service-name:latest .
```

### Push to Docker Hub
```bash
# Login once
docker login

# Push images
docker push patmeshs/api-gateway:latest
docker push patmeshs/auth-service:latest
# ... etc
```

### Use specific version tags
```bash
# In build.sh, change:
docker build -t patmeshs/auth-service:v1.0.0 .

# Update k3s YAML to use version:
image: patmeshs/auth-service:v1.0.0
```

## File Structure

```
social-feed-system/
├── build-all.sh                  # Global build script
├── api-gateway-service/
│   ├── Dockerfile
│   ├── build.sh
│   ├── pom.xml
│   └── src/
├── user-auth-service/
│   ├── Dockerfile
│   ├── build.sh
│   ├── pom.xml
│   └── src/
├── user-post-service/
│   ├── Dockerfile
│   ├── build.sh
│   ├── pom.xml
│   └── src/
├── notification-service/
│   ├── Dockerfile
│   ├── build.sh
│   ├── pom.xml
│   └── src/
└── ws-notification-service/
    ├── Dockerfile
    ├── build.sh
    ├── pom.xml
    └── src/
```

## Next Steps

After building images:
1. ✅ Images are available in local Docker
2. ✅ Ready to deploy to k3s with `cd k3s && ./setup.sh`
3. ✅ Check running containers: `kubectl get pods`
