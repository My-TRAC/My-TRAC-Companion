#!/bin/bash


# example to push but not used
docker build --no-cache -t mytrac/companion-backend:v2.0 -f Dockerfile ..
docker push sparsitytechnologies/companion-backend:latest

