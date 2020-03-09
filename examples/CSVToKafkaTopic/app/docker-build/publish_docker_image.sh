#!/bin/bash

CURRENT_PATH=`pwd`

cd ..
mvn assembly:assembly

cd $CURRENT_PATH

cp ../target/CSVToKafkaTopic-1.0-SNAPSHOT-jar-with-dependencies.jar .
docker build --no-cache -t sparsitytechnologies/csvtokafkatopic:latest .
docker push sparsitytechnologies/csvtokafkatopic:latest
rm CSVToKafkaTopic-1.0-SNAPSHOT-jar-with-dependencies.jar

