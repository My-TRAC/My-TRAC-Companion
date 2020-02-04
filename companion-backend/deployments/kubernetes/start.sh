#!/bin/bash

echo "Deploying Sparsity module..."


kubectl create secret generic deploy-conf --from-file mytrac
kubectl apply -f mongo-companion-volume.yaml
kubectl apply -f mongo-companion-backend-deployment.yaml
sleep 10
kubectl apply -f tracking-container-deployment.yaml
kubectl apply -f mytrac-ws-login.yaml
kubectl apply -f mytrac-ws-media.yaml
kubectl apply -f ws-mytrac-register.yaml
kubectl apply -f ws-mytrac-trip.yaml
kubectl apply -f ws-mytrac-activity.yaml
kubectl apply -f firebase-container.yaml
kubectl apply -f polls-container.yaml
kubectl apply -f companion-backend.yaml
kubectl apply -f mytrac-webapp.yaml
kubectl apply -f ws-mytrac-rewards.yaml

#kubectl apply -f mytrac-ingress.yaml

#kubectl apply -f otp-be-nl.yaml
#kubectl apply -f otp-pt.yaml
#kubectl apply -f otp-gr.yaml
#kubectl apply -f otp-ca.yaml

echo "Deploying Sparsity DONE"


