#!/bin/sh

set -e

cd $(dirname $0)

./mvnw -DskipTests=True clean package

docker build -f attribute-dictionary-api/src/main/docker/Dockerfile -t attribute-dictionary-api .
docker build -f attribute-dictionary-sync/src/main/docker/Dockerfile -t attribute-dictionary-sync .
docker build -f dictionary-backend-api/src/main/docker/Dockerfile -t dictionary-backend-api .
docker build -f eureka-server/src/main/docker/Dockerfile -t eureka-server .

docker images | grep -i attribute-dictionary-api
docker images | grep -i attribute-dictionary-sync
docker images | grep -i dictionary-backend-api
docker images | grep -i eureka-server