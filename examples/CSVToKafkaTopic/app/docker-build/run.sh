#!/bin/bash

git clone https://github.com/My-TRAC/ConfigurationScripts.git

chmod +x ./ConfigurationScripts/*.sh

/opt/CSVToKafkaTopic/ConfigurationScripts/waitForKafkaConenct.sh
/opt/CSVToKafkaTopic/ConfigurationScripts/waitForMySQL.sh


#git clone https://github.com/My-TRAC/data-model.git
#cd data-model/Resources/MYSQLInitDataModel/app
#mvn assembly:assembly
#cp target/MYSQLInitDataModel-1.0-SNAPSHOT-jar-with-dependencies.jar /opt/CSVToKafkaTopic/MYSQLInitDataModel-1.0-SNAPSHOT-jar-with-dependencies.jar


cd /opt/CSVToKafkaTopic

#java -cp /root/MYSQLInitDataModel-1.0-SNAPSHOT-jar-with-dependencies.jar MYSQLInitDataModel --username $MYSQL_USER --password $MYSQL_PASSWORD --database $MYSQL_DATABASE --topic-names activity,mobility_trace,facility,poi,user,user_chooses_route,user_evaluates_activity,user_joins_group,user_uses_app,user_views_activity,user_reward,route_choice_model_output,route_departure_model_output,poll_questionary,poll_response --schema-registry http://$SCHEMA_REGISTRY_HOST_NAME:8081 --mysql $MYSQL_HOST':'$MYSQL_PORT








/opt/CSVToKafkaTopic/ConfigurationScripts/setJDBCSourceConnector.sh -c "cigo-jdbc-source_CSV_TO_KAFKA"\
                                                -k $KAFKA_CONNECT_HOST\
                                                -m $MYSQL_HOST\
                                                -d $MYSQL_DATABASE\
                                                -u $MYSQL_USER\
                                                -p $MYSQL_PASSWORD\
                                                -i "mytrac_id"\
                                                -t "mytrac_last_modified"

#/opt/CSVToKafkaTopic/ConfigurationScripts/setElasticSearchConnector.sh







java -jar /opt/CSVToKafkaTopic/CSVToKafkaTopic-1.0-SNAPSHOT-jar-with-dependencies.jar -db $MYSQL_DATABASE -ip $MYSQL_HOST -sr $SCHEMA_REGISTRY_HOST
