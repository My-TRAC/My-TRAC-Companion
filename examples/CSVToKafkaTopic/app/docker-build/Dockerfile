#FROM java:8-jre
#FROM sdorra/oracle-java-8
#FROM williamyeh/java8
#FROM java8ocr/java8-ocr

#FROM adoptopenjdk/maven-openjdk8
#FROM maven:3.5.2-jdk-8-alpine
FROM maven:3.5.2-jdk-8-alpine AS MAVEN_TOOL_CHAIN
RUN apk add --update \
    python git

RUN git clone https://github.com/My-TRAC/data-model.git  /data-model
WORKDIR /data-model/Resources/MYSQLInitDataModel/app
RUN mvn assembly:assembly
#cp target/MYSQLInitDataModel-1.0-SNAPSHOT-jar-with-dependencies.jar /opt/CSVToKafkaTopic/MYSQLInitDataModel-1.0-SNAPSHOT-jar-with-dependencies.jar



#FROM re6exp/debian-jessie-oracle-jdk-8 as final
#RUN printf "deb http://archive.debian.org/debian/ jessie main\ndeb-src http://archive.debian.org/debian/ jessie main\ndeb http://security.debian.org jessie/updates main\ndeb-src http://security.debian.org jessie/updates main" > /etc/apt/sources.list
#RUN apt-get update
#RUN apt-get install -y mysql-client
#RUN apt-get install -y git
#RUN apt-get install -y curl


FROM openjdk:8 as final

RUN apt-get update && \
    apt-get install -y mysql-client git 

COPY --from=MAVEN_TOOL_CHAIN /data-model/Resources/MYSQLInitDataModel/app/target/MYSQLInitDataModel-1.0-SNAPSHOT-jar-with-dependencies.jar /opt/CSVToKafkaTopic/


COPY  CSVToKafkaTopic-1.0-SNAPSHOT-jar-with-dependencies.jar /opt/CSVToKafkaTopic/CSVToKafkaTopic-1.0-SNAPSHOT-jar-with-dependencies.jar
COPY run.sh /opt/CSVToKafkaTopic/run.sh
RUN chmod +x /opt/CSVToKafkaTopic/run.sh
WORKDIR /opt/CSVToKafkaTopic


EXPOSE 4568

ENTRYPOINT ["/opt/CSVToKafkaTopic/run.sh"]
CMD [""]

