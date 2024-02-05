#!/bin/sh

set -e

cd $(dirname $0)

./mvnw clean package

docker build -f src/main/docker/Dockerfile -t attributes-os-rx-api .

docker images | grep -i attributes-os-rx-api