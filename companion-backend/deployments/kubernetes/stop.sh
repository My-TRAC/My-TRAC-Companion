#!/bin/bash

kubectl delete -f mytrac-ingress.yaml
kubectl delete -f companion-backend.yaml
kubectl delete -f mytrac-webapp.yaml
kubectl delete -f polls-container.yaml
kubectl delete -f firebase-container.yaml
kubectl delete -f ws-mytrac-activity.yaml
kubectl delete -f ws-mytrac-trip.yaml
kubectl delete -f ws-mytrac-register.yaml
kubectl delete -f mytrac-ws-login.yaml
kubectl delete -f tracking-container-deployment.yaml
kubectl delete -f mongo-companion-backend-deployment.yaml
kubectl delete -f mysql-companion-backend-deployment.yaml
kubectl delete secret deploy-conf

echo "Undeploying Sparsity DONE"

