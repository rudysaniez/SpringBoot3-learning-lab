#!/bin/sh

set -e

#
# http://localhost:9999/oauth2/authorize?response_type=code&client_id=attribute-client&redirect_uri=https://my.redirect.uri&scope=attribute:write&state=35725
#

cd $(dirname $0)

curl -k http:///attribute-client:attribute_secret@localhost:9999/oauth2/token -d grant_type=authorization_code \
-d client_id=attribute-client \
-d redirect_uri=https://my.redirect.uri \
-d code=$1 -s | jq
