#!/bin/bash

echo "Cluster Status:"
k3d cluster list

echo ""
echo "Nodes:"
kubectl get nodes

echo ""
echo "Pods:"
kubectl get pods -o wide

echo ""
echo "Services:"
kubectl get services

echo ""
echo "Resource Usage:"
kubectl top pods 2>/dev/null || echo "Metrics not available (requires metrics-server)"

echo ""
echo "Recent Events:"
kubectl get events --sort-by='.lastTimestamp' | tail -10
