#!/bin/bash
# Delete the k3d cluster and all resources

echo "This will delete the social-feed k3d cluster."
read -p "Are you sure? (y/N) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]
then
    echo "Deleting cluster..."
    k3d cluster delete social-feed
    echo "✓ Cluster deleted"
else
    echo "Cancelled"
fi
