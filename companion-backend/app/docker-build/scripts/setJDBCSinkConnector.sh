#!/bin/bash


#export URL="kafka-connect-avor:28083"
export URL="$KAFKA_CONNECT_HOST:28083"
export MYSQL="$MYSQL_HOST"

echo ""
echo "Adding connector to $URL/connectors"

echo ""
echo ""

#curl -X POST \
#  -H "Content-Type: application/json" \
#  --data "{ \"name\": \"cigo-jdbc-sink\", \"config\": { \"connector.class\": \"io.confluent.connect.jdbc.JdbcSinkConnector\", \"connection.url\": \"jdbc:mysql://mysql:3306/sink_table?user=root&password=confluent\", \"topics\":\"ratings\", \"auto.create\":\"true\", \"task.max\":1} }" \
#  $URL/connectors


CONNECTOR="{ 
\"name\": \"$1\", 
\"config\": {
\"connector.class\":\"io.confluent.connect.jdbc.JdbcSinkConnector\",
\"tasks.max\":\"1\",
\"topics\":\"$2\",
\"connection.url\": \"jdbc:mysql://$MYSQL_HOST:$MYSQL_PORT/$MYSQL_DATABASE?user=$MYSQL_USER&password=$MYSQL_PASSWORD\", 
\"key.converter\":\"io.confluent.connect.avro.AvroConverter\",
\"key.converter.schema.registry.url\":\"http://$SCHEMA_REGISTRY_HOST_NAME:8081\",
\"value.converter\":\"io.confluent.connect.avro.AvroConverter\",
\"value.converter.schema.registry.url\":\"http://$SCHEMA_REGISTRY_HOST_NAME:8081\",
\"insert.mode\":\"upsert\",
\"batch.size\":\"0\",
\"auto.create\":\"false\",
\"pk.mode\":\"record_value\",
\"pk.fields\":\"mytrac_id\" }
}"



 echo ""

echo $CONNECTOR | curl -X POST \
  -H "Content-Type: application/json" \
  --data @- \
  $URL/connectors

  echo ""
  echo ""
