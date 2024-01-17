<h3 align="center">Opensearch /Elasticsearch queries example</h3>

---

## Links useful

[Queries example from blog named `Coralogix`](#https://coralogix.com/blog/42-elasticsearch-query-examples-hands-on-tutorial/)  
[Opensearch GitHub readme](#https://github.com/opensearch-project/spring-data-opensearch/blob/main/README.md)  
[Opensearch example](#https://github.com/opensearch-project/spring-data-opensearch/tree/main/spring-data-opensearch-examples/spring-boot-gradle)
[Spring reactor and elasticsearch](#https://nurkiewicz.com/2018/01/spring-reactor-and-elasticsearch-from.html)

## Table of contents
- [match](#match)
- [match and minimum_should_match](#match-and-minimum_should_match)
- [multi match](#multi-match)
- [match_phrase](#match_phrase)

## match

```
GET videos/_search
{
    "query": {
        "match": {
          "videoName": {
            "query" : "Learn"
          }
        }
    }
}
```

### match and minimum_should_match

```
GET videos/_search
{
    "query": {
        "match": {
          "videoName": {
            "query" : "Learn",
            "minimum_should_match": 3
          }
        }
    }
}
```

### multi match

```
GET videos/_search
{
  "query": {
    "multi_match": {
      "fields": ["videoName","description"],
      "query": "Learn"
    }
  }
}
```

### match_phrase

```
GET videos/_search
{
  "query": {
    "match_phrase": {
      "videoName": {
        "query": "Learn Springboot 3 with Testing"
      }
    }
  }
}
```

