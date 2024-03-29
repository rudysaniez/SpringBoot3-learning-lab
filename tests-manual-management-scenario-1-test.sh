#!/bin/sh

set -e

cd $(dirname $0)

. manual-tests/banner-func-manual-tests
. manual-tests/core-func-manual-tests

PORT="8080"

waitForService localhost $PORT

begin_banner

testNumber="GET HTTP status [endpoint=/management/info] without user"
result=$(eval getHttpStatus localhost $PORT management/info)
if [[ $result == "200" ]]
then echo " > $testNumber : Http_status=$result."
else
  echo " > INCORRECT expected 200, we have $result"
fi

testNumber="GET HTTP status [endpoint=/management/health] without user"
result=$(eval getHttpStatus localhost $PORT management/info)
if [[ $result == "200" ]]
then echo " > $testNumber : Http_status=$result."
else
  echo " > INCORRECT expected 200, we have $result"
fi