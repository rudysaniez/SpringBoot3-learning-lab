#!/bin/sh

set -e

cd $(dirname $0)

./mvnw clean package

docker build -f attribute-dictionary-api/src/main/docker/Dockerfile -t attribute-dictionary-api .
docker build -f attribute-dictionary-sync/src/main/docker/Dockerfile -t attribute-dictionary-sync .

docker images | grep -i attribute-dictionary-api
docker images | grep -i attribute-dictionary-sync