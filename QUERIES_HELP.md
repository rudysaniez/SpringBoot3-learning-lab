<h3 align="center">Opensearch /Elasticsearch queries example</h3>

---

## Links useful

[Queries example from blog named `Coralogix`](#https://coralogix.com/blog/42-elasticsearch-query-examples-hands-on-tutorial/)  
[Opensearch GitHub readme](#https://github.com/opensearch-project/spring-data-opensearch/blob/main/README.md)  
[Opensearch example](#https://github.com/opensearch-project/spring-data-opensearch/tree/main/spring-data-opensearch-examples/spring-boot-gradle)
[Spring reactor and elasticsearch](#https://nurkiewicz.com/2018/01/spring-reactor-and-elasticsearch-from.html)
[RestHighLevelClient example](#https://blog.clairvoyantsoft.com/elasticsearch-java-high-level-rest-client-1c029610348d)

## Table of contents
- [match](#match)
- [match and minimum_should_match](#match-and-minimum_should_match)
- [multi match](#multi-match)
- [match_phrase](#match_phrase)
- [slot parameter](#slot-parameter)
- [match_phrase_prefix](#match_phrase_prefix)
- [term](#term)
- [terms](#terms)
- [exists](#exists)
- [range](#range)
- [ids queries](#ids-queries)
- [prefix queries](#prefix-queries)
- [wildcard queries](#wildcard-queries)
- [regex queries](#regex-queries)

## match

The “match” query is one of the most basic and commonly used queries in Elasticsearch and functions as a full-text query. 
We can use this query to search for text, number, or boolean values.
Let us search for the word *Learn* in the *videoName* field in the documents we ingested earlier.

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

Taking things a bit further, we can set a threshold for a minimum amount of matching words that the document must contain. 
For example, if we set this parameter to 1, the query will check for any documents with a minimum of 1 matching word.
Now, if we set the “minium_should_match” parameter to 3, then all three words must appear in the document to be classified as a match.

In our case, the following query would return only 1 document (with id=2) as that is the only one matching our criteria

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

So far we’ve been dealing with matches on a single field – that is we searched for the keywords inside a single field named “phrase.”
But what if we needed to search keywords across multiple fields in a document? This is where the multi-match query comes into play.
Let’s try an example search for the keyword “research help” in the “position” and “phrase” fields contained in the documents.

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

Match_phrase is another commonly used query which, like its name indicates, matches phrases in a field.
If we need to search for the phrase “roots heuristic coherent” in the “phrase” field in the employee index, 
we can use the “match_phrase” query:

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

GET videos/_search
{
  "query": {
    "match_phrase": {
      "videoName": "Learn Spring boot 3 with Kafka"
    }
  }
}
```

### slot parameter

A useful feature we can make use of in the match_phrase query is the “slop” parameter which allows us to create more flexible searches.
Suppose we searched for “roots coherent” with the match_phrase query. We wouldn’t receive any documents returned from the employee index. 
This is because for match_phrase to match, the terms need to be in the exact order.

```
GET videos/_search
{
  "query": {
    "match_phrase": {
      "videoName": {
        "query": "Learn ",
        "slop": 1
      }
    }
  }
}
```

### match_phrase_prefix

The **match_phrase_prefix** query is similar to the match_phrase query, 
but here the last term of the search keyword is considered as a prefix and is used to match any term starting with that prefix term.

```
GET videos/_search
{
  "query": {
    "match_phrase_prefix": {
      "videoName": {
        "query": "Springboot 3 w"
      }
    }
  }
}

I obtain :

{
  "took": 17,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "skipped": 0,
    "failed": 0
  },
  "hits": {
    "total": {
      "value": 1,
      "relation": "eq"
    },
    "max_score": 1.0986491,
    "hits": [
      {
        "_index": "videos",
        "_id": "jmNWE40BQpatIpF0zsjl",
        "_score": 1.0986491,
        "_source": {
          "_class": "com.springboot.learning.sb3.domain.VideoEntity",
          "videoName": "Learn Springboot 3 with Testing",
          "description": "Nice book on the tests",
          "username": "user"
        }
      }
    ]
  }
}
```

### term

This is the simplest of the term-level queries. This query searches for the exact match 
of the search keyword against the field in the documents.
For example, if we search for the word "testing" using the term query against the field "videoName", 
it will search exactly as the word is, even with the casing.

```
GET videos/_search
{
  "query": {
    "term": {
      "videoName": "testing"
    }
  }
}

I obtain :

{
  "took": 2,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "skipped": 0,
    "failed": 0
  },
  "hits": {
    "total": {
      "value": 1,
      "relation": "eq"
    },
    "max_score": 0.7199211,
    "hits": [
      {
        "_index": "videos",
        "_id": "jmNWE40BQpatIpF0zsjl",
        "_score": 0.7199211,
        "_source": {
          "_class": "com.springboot.learning.sb3.domain.VideoEntity",
          "videoName": "Learn Springboot 3 with Testing",
          "description": "Nice book on the tests",
          "username": "user"
        }
      }
    ]
  }
}
```

### terms

We can also pass multiple terms to be searched on the same field, by using the terms query.

```
GET videos/_search
{
  "query": {
    "terms": {
      "videoName": ["testing", "kafka"]
    }
  }
}

I obtain :

{
  "took": 4,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "skipped": 0,
    "failed": 0
  },
  "hits": {
    "total": {
      "value": 2,
      "relation": "eq"
    },
    "max_score": 1,
    "hits": [
      {
        "_index": "videos",
        "_id": "jmNWE40BQpatIpF0zsjl",
        "_score": 1,
        "_source": {
          "_class": "com.springboot.learning.sb3.domain.VideoEntity",
          "videoName": "Learn Springboot 3 with Testing",
          "description": "Nice book on the tests",
          "username": "user"
        }
      },
      {
        "_index": "videos",
        "_id": "AmPSGI0BQpatIpF0ktRf",
        "_score": 1,
        "_source": {
          "_class": "com.springboot.learning.sb3.domain.VideoEntity",
          "videoName": "Learn Spring boot 3 with Kafka",
          "description": "Nice book!",
          "username": "user"
        }
      }
    ]
  }
}
```

### exists

```
GET videos/_search
{
  "query": {
    "exists": {
      "field": "videoName"
    }
  }
}

I obtain :

{
  "took": 3,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "skipped": 0,
    "failed": 0
  },
  "hits": {
    "total": {
      "value": 2,
      "relation": "eq"
    },
    "max_score": 1,
    "hits": [
      {
        "_index": "videos",
        "_id": "jmNWE40BQpatIpF0zsjl",
        "_score": 1,
        "_source": {
          "_class": "com.springboot.learning.sb3.domain.VideoEntity",
          "videoName": "Learn Springboot 3 with Testing",
          "description": "Nice book on the tests",
          "username": "user"
        }
      },
      {
        "_index": "videos",
        "_id": "AmPSGI0BQpatIpF0ktRf",
        "_score": 1,
        "_source": {
          "_class": "com.springboot.learning.sb3.domain.VideoEntity",
          "videoName": "Learn Spring boot 3 with Kafka",
          "description": "Nice book!",
          "username": "user"
        }
      }
    ]
  }
}
```

But if launch that :

```
GET videos/_search
{
  "query": {
    "exists": {
      "field": "videoDescription"
    }
  }
}

I obtain :

{
  "took": 12,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "skipped": 0,
    "failed": 0
  },
  "hits": {
    "total": {
      "value": 0,
      "relation": "eq"
    },
    "max_score": null,
    "hits": []
  }
}
```

The `videoDescription` field does not exist.

### range

You can find :

- gte
- gt
- lte
- lt

```

```

### ids queries

```
GET attributs_dictionnary_v1/_search
{
  "query": {
    "ids": {
      "values": ["f2OhHY0BQpatIpF0093b", "tGOuHY0BQpatIpF0-90o"]
    }
  }  
}

I obtain two documents.
```

### prefix queries

The prefix query is used to fetch documents that contain the given search string as the prefix in the specified field.

```
GET attributs_dictionnary_v1/_search
{
  "query": {
    "prefix": {
      "code": "cod"
    }
  }
}
```

### wildcard queries

Will fetch the documents that have terms that match the given wildcard pattern.

```
GET attributs_dictionnary_v1/_search
{
  "query": {
    "wildcard": {
      "defaultMetricUnit": {
        "value": "metric*"
      }
    }
  }
}
```

### regex queries

```
GET attributs_dictionnary_v1/_search
{
  "query": {
    "regexp": {
      "code": {
        "value": "code[0-9]+"
      }
    }
  }
}
```

