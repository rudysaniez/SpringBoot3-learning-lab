#!/bin/sh

set -e

cd $(dirname $0)

. manual-tests/banner-func-manual-tests
. manual-tests/core-func-manual-tests

PORT="8080"

waitForService localhost $PORT

begin_banner

BASE_PATH="v1/attributes"

####################

testNumber="POST HTTP status [endpoint=$BASE_PATH] with [user:user]"
result=$(eval getHttpStatusWithHttpMethod localhost $PORT POST "$BASE_PATH/:bulk" attributes.json user user)
if [[ $result == "200" ]]
then echo " > $testNumber : Http_status=$result."
else
  echo " > INCORRECT expected 200, we have $result"
fi

####################

testNumber="GET HTTP status [endpoint=$BASE_PATH] with [user:user]"
result=$(eval getHttpStatus localhost $PORT $BASE_PATH user user)
if [[ $result == "200" ]]
then echo " > $testNumber : Http_status=$result."
else
  echo " > INCORRECT expected 200, we have $result"
fi

####################

testNumber="GET HTTP status [endpoint=$BASE_PATH/:search?q=code=code01] with [user:user]"
result=$(eval getHttpStatus localhost $PORT "$BASE_PATH/:search?q=code=code01" user user)
if [[ $result == "200" ]]
then echo " > $testNumber : Http_status=$result."
else
  echo " > INCORRECT expected 200, we have $result"
fi

####################

testNumber="GET HTTP status [endpoint=$BASE_PATH/:empty] with [user:user]"
result=$(eval getHttpStatusWithHttpMethod localhost $PORT DELETE "$BASE_PATH/:empty" empty.json user user)
if [[ $result == "200" ]]
then echo " > $testNumber : Http_status=$result."
else
  echo " > INCORRECT expected 200, we have $result"
fi

####################
