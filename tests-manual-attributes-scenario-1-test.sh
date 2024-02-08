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

testNumber="Bulk several attributes [endpoint=$BASE_PATH/:bulk] with [user:user]"
result=$(eval getHttpStatusWithHttpMethod localhost $PORT POST "$BASE_PATH/:bulk" attributes.json user user)
if [[ $result == "200" ]]
then echo " > $testNumber : Http_status=$result."
else
  echo " > INCORRECT expected 200, we have $result"
fi

####################

testNumber="Bulk async several attributes [endpoint=$BASE_PATH/:bulk-async] with [user:user]"
result=$(eval getHttpStatusWithHttpMethod localhost $PORT POST "$BASE_PATH/:bulk-async" attributes.json user user)
if [[ $result == "202" ]]
then echo " > $testNumber : Http_status=$result."
else
  echo " > INCORRECT expected 202, we have $result"
fi

####################

testNumber="Save one attribute [endpoint=$BASE_PATH] with [user:user]"
result=$(eval getHttpStatusWithHttpMethod localhost $PORT POST "$BASE_PATH" attribute01.json user user)
if [[ $result == "201" ]]
then echo " > $testNumber : Http_status=$result."
else
  echo " > INCORRECT expected 201, we have $result"
fi

####################

testNumber="Save async one attribute [endpoint=$BASE_PATH/:async] with [user:user]"
result=$(eval getHttpStatusWithHttpMethod localhost $PORT POST "$BASE_PATH/:async" attribute02.json user user)
if [[ $result == "202" ]]
then echo " > $testNumber : Http_status=$result."
else
  echo " > INCORRECT expected 202, we have $result"
fi

####################

testNumber="Get a page of attributes [endpoint=$BASE_PATH] with [user:user]"
result=$(eval getHttpStatus localhost $PORT $BASE_PATH user user)
if [[ $result == "200" ]]
then echo " > $testNumber : Http_status=$result."
else
  echo " > INCORRECT expected 200, we have $result"
fi

####################

testNumber="Search attributes [endpoint=$BASE_PATH/:search?q=code=code01] with [user:user]"
result=$(eval getHttpStatus localhost $PORT "$BASE_PATH/:search?q=code=code01" user user)
if [[ $result == "200" ]]
then echo " > $testNumber : Http_status=$result."
else
  echo " > INCORRECT expected 200, we have $result"
fi

####################

testNumber="Delete all attributes [endpoint=$BASE_PATH/:empty] with [user:user]"
result=$(eval getHttpStatusWithHttpMethod localhost $PORT DELETE "$BASE_PATH/:empty" empty.json user user)
if [[ $result == "200" ]]
then echo " > $testNumber : Http_status=$result."
else
  echo " > INCORRECT expected 200, we have $result"
fi

####################
