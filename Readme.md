## How to use
##### 1. Run `docker-compose up` for running ElasticSearch
##### 2. Run application and call endpoints

#### For checking you can use basic Rest API

Indices info: `curl http://localhost:9200/_cat/indices?v`

Your index info: `curl http://localhost:9200/products?pretty`

Search all: `curl http://localhost:9200/products/_search?pretty`

GET the document by ID: `curl http://localhost:9200/products/_doc/7947?pretty`
DELETE the document by ID: `curl -X DELETE http://localhost:9200/products/_doc/1`

##### For details by Elasticsearch API see: https://www.elastic.co/guide/en/elasticsearch/reference/current/rest-apis.html
