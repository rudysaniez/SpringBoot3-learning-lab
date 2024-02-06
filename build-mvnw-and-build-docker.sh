#!/bin/sh

set -e

cd $(dirname $0)

./mvnw clean package

docker build -f src/main/docker/Dockerfile -t attribute-dictionary-api .

docker images | grep -i attribute-dictionary-api