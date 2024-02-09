# Attribute dictionary RX API and Event-driven with Spring-Cloud-Stream

## Launch with docker

### Build and image creation

```
sh build-mvnw-and-build-docker.sh
```

When this shell is applied, you can see the image like this :

```
docker images | grep attribute-dictionary-api
```

### Launch with docker

You need to have the 8080, 8081, 9200 and 9092 to free.

- 8080 : the attribute-dictionary-api
- 8081 : the kafka schema registry
- 9200 : the opensearch
- 9092 : the kafka broker

```
docker-compose up --detach
```

### logs

```
docker-compose logs attributes-api -f
```

You will see how the `attributes-api` start.

### Add an index in opensearch

```
http PUT http://localhost:9200/attributes_dictionary_v1 < index_attribute_dictionary_v1.json
```

You will obtain :

```
HTTP/1.1 200 OK
content-encoding: gzip
content-length: 91
content-type: application/json; charset=UTF-8

{
    "acknowledged": true,
    "index": "attributes_dictionary_v1",
    "shards_acknowledged": true
}
```

### Swagger-ui

```
http://localhost:8080/openapi/swagger-ui.html
```

If a login appear, use that :

- username : user
- password : user

### Add an attribute

```
http -a user:user POST http://localhost:8080/v1/attributes/:async < src/test/resources/json/attribute01.json
```

You will obtain :

```
HTTP/1.1 202 Accepted
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Expires: 0
Pragma: no-cache
Referrer-Policy: no-referrer
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 0
content-length: 0
```

### Get a page of attributes

```
http -a user:user GET http://localhost:8080/v1/attributes
```

You will obtain :

```
HTTP/1.1 200 OK
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Content-Length: 772
Content-Type: application/json
Expires: 0
Pragma: no-cache
Referrer-Policy: no-referrer
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 0
```

```
{
    "content": [
        {
            "allowedExtensions": [],
            "autoOptionSorting": true,
            "availableLocales": [],
            "code": "CODE01",
            "dateMax": "2023-12-20",
            "dateMin": "2023-01-01",
            "decimalsAllowed": true,
            "defaultMetricUnit": "METRIC_UNIT_01",
            "defaultValue": "DEFAULT",
            "group": "GROUP01",
            "guidelines": [],
            "id": "Ol77jY0BMU_MOZyPa83V",
            "isReadOnly": false,
            "labels": {
                "KEY01": "VAL01"
            },
            "localizable": true,
            "maxCharacters": 10,
            "maxFileSize": 1000,
            "metricFamily": "METRIC01",
            "minimumInputLength": 1,
            "negativeAllowed": true,
            "numberMax": 10.0,
            "numberMin": 1.0,
            "referenceDataName": "REF_NAME_01",
            "scopable": true,
            "sortOrder": 1,
            "type": "TYPE01",
            "unique": true,
            "useableAsGridFilter": true,
            "validationRegexp": "REGEX_01",
            "validationRule": "RULE_01",
            "wysiwygEnabled": true
        }
    ],
    "pageMetadata": {
        "number": 0,
        "size": 5,
        "totalElements": 1,
        "totalPages": 1
    }
}
```

### Bulk insert

```
http -a user:user POST http://localhost:8080/v1/attributes/:bulk-async < src/test/resources/json/attributes.json
```

You will obtain :

```
HTTP/1.1 202 Accepted
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Expires: 0
Pragma: no-cache
Referrer-Policy: no-referrer
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 0
content-length: 0
```

Now you launch that :

```
http -a user:user GET http://localhost:8080/v1/attributes?size=2
```

You will obtain :

```
{
    "content": [
        {
            "allowedExtensions": [],
            "autoOptionSorting": true,
            "availableLocales": [],
            "code": "CODE01",
            "dateMax": "2023-12-20",
            "dateMin": "2023-01-01",
            "decimalsAllowed": true,
            "defaultMetricUnit": "METRIC_UNIT_01",
            "defaultValue": "DEFAULT",
            "group": "GROUP01",
            "guidelines": [],
            "id": "Ol77jY0BMU_MOZyPa83V",
            "isReadOnly": false,
            "labels": {
                "KEY01": "VAL01"
            },
            "localizable": true,
            "maxCharacters": 10,
            "maxFileSize": 1000,
            "metricFamily": "METRIC01",
            "minimumInputLength": 1,
            "negativeAllowed": true,
            "numberMax": 10.0,
            "numberMin": 1.0,
            "referenceDataName": "REF_NAME_01",
            "scopable": true,
            "sortOrder": 1,
            "type": "TYPE01",
            "unique": true,
            "useableAsGridFilter": true,
            "validationRegexp": "REGEX_01",
            "validationRule": "RULE_01",
            "wysiwygEnabled": true
        },
        {
            "allowedExtensions": [],
            "autoOptionSorting": true,
            "availableLocales": [],
            "code": "CODE01",
            "dateMax": "2023-12-20",
            "dateMin": "2023-01-01",
            "decimalsAllowed": true,
            "defaultMetricUnit": "METRIC_UNIT_01",
            "defaultValue": "DEFAULT",
            "group": "GROUP01",
            "guidelines": [],
            "id": "O178jY0BMU_MOZyP1s0H",
            "isReadOnly": false,
            "labels": {
                "KEY01": "VAL01"
            },
            "localizable": true,
            "maxCharacters": 10,
            "maxFileSize": 1000,
            "metricFamily": "METRIC01",
            "minimumInputLength": 1,
            "negativeAllowed": true,
            "numberMax": 10.0,
            "numberMin": 1.0,
            "referenceDataName": "REF_NAME_01",
            "scopable": true,
            "sortOrder": 1,
            "type": "TYPE01",
            "unique": true,
            "useableAsGridFilter": true,
            "validationRegexp": "REGEX_01",
            "validationRule": "RULE_01",
            "wysiwygEnabled": true
        }
    ],
    "pageMetadata": {
        "number": 0,
        "size": 2,
        "totalElements": 6,
        "totalPages": 3
    }
}
```

You are two `code01` in the field named `code`. 
The API does not control this field, and therefore it allows to have duplicates.

### Delete one

Take an id and launch this call

```
http -a user:user DELETE http://localhost:8080/v1/attributes/O178jY0BMU_MOZyP1s0H
```

You will obtain :

```
HTTP/1.1 200 OK
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Expires: 0
Pragma: no-cache
Referrer-Policy: no-referrer
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 0
content-length: 0
```

The duplicate has disappeared.