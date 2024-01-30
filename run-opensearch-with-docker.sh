#!/bin/sh

set -e 

docker run -d --name opensearch-attr-node -p 9200:9200 -p 9600:9600 -e "discovery.type=single-node" opensearchproject/opensearch:latest
