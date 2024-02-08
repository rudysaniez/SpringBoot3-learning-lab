<h3 align="center">Opensearch : Index creation </h3>

I would like create a new index :  

```
http -a admin:admin PUT https://localhost:9200/attributes_dictionary_v1 < index_creation_v1.json --verify=no
```

I obtain that :  

```
HTTP/1.1 200 OK
content-length: 83
content-type: application/json; charset=UTF-8

{
    "acknowledged": true,
    "index": "attributes_dictionary_v1",
    "shards_acknowledged": true
}
```

I would like display the mapping associated to `attributes_dictionary_v1` index :  

```
http -a admin:admin  https://localhost:9200/attributes_dictionary_v1/_mapping --verify=no
```

I obtain that :  

```
HTTP/1.1 200 OK
content-length: 1580
content-type: application/json; charset=UTF-8
{
    "attributes_dictionary_v1": {
        "mappings": {
            "properties": {
                "autoOptionSorting": {
                    "type": "boolean"
                },
                "code": {
                    "fielddata": true,
                    "fields": {
                        "keyword": {
                            "ignore_above": 256,
                            "type": "keyword"
                        }
                    },
                    "type": "text"
                },
                "dateMax": {
                    "type": "date"
                },
                "dateMin": {
                    "type": "date"
                },
                "decimalsAllowed": {
                    "type": "boolean"
                },
                "defaultMetricUnit": {
                    "fields": {
                        "keyword": {
                            "ignore_above": 256,
                            "type": "keyword"
                        }
                    },
                    "type": "text"
                },
                "defaultValue": {
                    "fields": {
                        "keyword": {
                            "ignore_above": 256,
                            "type": "keyword"
                        }
                    },
                    "type": "text"
                },
                "group": {
                    "fields": {
                        "keyword": {
                            "ignore_above": 256,
                            "type": "keyword"
                        }
                    },
                    "type": "text"
                },
                "isReadOnly": {
                    "type": "boolean"
                },
                "labels": {
                    "properties": {
                        "KEY01": {
                            "fields": {
                                "keyword": {
                                    "ignore_above": 256,
                                    "type": "keyword"
                                }
                            },
                            "type": "text"
                        }
                    }
                },
                "localizable": {
                    "type": "boolean"
                },
                "maxCharacters": {
                    "type": "long"
                },
                "maxFileSize": {
                    "type": "long"
                },
                "message": {
                    "fielddata": true,
                    "type": "text"
                },
                "metricFamily": {
                    "fields": {
                        "keyword": {
                            "ignore_above": 256,
                            "type": "keyword"
                        }
                    },
                    "type": "text"
                },
                "minimumInputLength": {
                    "type": "long"
                },
                "negativeAllowed": {
                    "type": "boolean"
                },
                "numberMax": {
                    "type": "float"
                },
                "numberMin": {
                    "type": "float"
                },
                "referenceDataName": {
                    "fields": {
                        "keyword": {
                            "ignore_above": 256,
                            "type": "keyword"
                        }
                    },
                    "type": "text"
                },
                "scopable": {
                    "type": "boolean"
                },
                "sortOrder": {
                    "type": "long"
                },
                "type": {
                    "fields": {
                        "keyword": {
                            "ignore_above": 256,
                            "type": "keyword"
                        }
                    },
                    "type": "text"
                },
                "unique": {
                    "type": "boolean"
                },
                "useableAsGridFilter": {
                    "type": "boolean"
                },
                "validationRegexp": {
                    "fields": {
                        "keyword": {
                            "ignore_above": 256,
                            "type": "keyword"
                        }
                    },
                    "type": "text"
                },
                "validationRule": {
                    "fields": {
                        "keyword": {
                            "ignore_above": 256,
                            "type": "keyword"
                        }
                    },
                    "type": "text"
                },
                "wysiwygEnabled": {
                    "type": "boolean"
                }
            }
        }
    }
}
```