#!/bin/sh

set -e

cd $(dirname $0)

curl -k http:///attribute-client:attribute_secret@localhost:9999/oauth2/token -d grant_type=client_credentials -d scope="attribute:read" -s | jq

