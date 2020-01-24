# StreamingApp

## Prerequistes

Java 8

Maven

ActiveMQ

MySQL

MongoDB

FFmpeg

Shaka Packager

## Instructions

Install Prerequistes.

Clone following git repositorys:
- https://github.com/ljungberg84/video-data.git
- https://github.com/ljungberg84/video-encoder.git
- https://github.com/ljungberg84/video-uploader.git
- https://github.com/Maksym-Gorbunov/register-service.git
- https://github.com/Maksym-Gorbunov/api-gateway.git

Build each project with "mvn clean package".

Start ActiveMQ and create a database in MySQL and MongoDB.

Run programs:
- java -jar -DPORT=<'value'> -DEUREKA_URL=<'value'> -DMYSQL_URL=<'value'> -DDB_NAME=<'value'> -DMESSAGE_BROKER_URL=<'value'> target/video-streaming-api-0.0.1-SNAPSHOT.jar
- java -jar -DPORT=<'value'> -DEUREKA_URL=<'value'> -DMONGODB_URL=<'value'> target/api-gateway-0.0.1-SNAPSHOT.jar
- java -jar -DPORT=<'value'> -DEUREKA_URL=<'value'> -DMESSAGE_BROKER_URL=<'value'> -DFILE_LOCATION=<'value'> target/videouploader-0.0.1-SNAPSHOT.jar
- java -jar -DPORT=<'value'> -DEUREKA_URL=<'value'> -DFILE_LOCATION=<'value_same_as_for_video_uploader'> -DMESSAGE_BROKER_URL=<'value'> target/video-encoder-0.0.1-SNAPSHOT.jar
- java -jar -DPORT=<'value'> target/register-service-0.0.1-SNAPSHOT.jar



