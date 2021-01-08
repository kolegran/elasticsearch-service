package com.github.kolegran.elasticsearch.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class ProductsService {

    private static final String INDEX_NAME = "products";
    private static final String MIGRATION_JSON = "products.json";
    private static final String INDEXED_DOC_ID = "1";
    private static final Logger logger = LoggerFactory.getLogger(ProductsService.class);
    private final RestHighLevelClient restHighLevelClient;
    private final Integer shards;
    private final Integer replicas;

    public ProductsService(@Value("${index.shards}") Integer shards, @Value("${index.replicas}") Integer replicas, RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
        this.shards = shards;
        this.replicas = replicas;
        createIndex();
    }

    private void createIndex() {
        final CreateIndexRequest request = new CreateIndexRequest(INDEX_NAME);
        request.settings(prepareIndexSettings());
        try {
            final boolean isIndexExists = restHighLevelClient.indices().exists(new GetIndexRequest(INDEX_NAME), RequestOptions.DEFAULT);
            if (!isIndexExists) {
                final CreateIndexResponse response = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
                logger.info("Index \"{}\" has been created", response.index());
            }
        } catch (IOException e) {
            throw new CreateIndexException("Something went wrong during index creation: problem sending the request or parsing back the response");
        }
    }

    public void migrateProducts() {
        final IndexRequest request = new IndexRequest(INDEX_NAME).id(INDEXED_DOC_ID);
        request.source(getProductsJsonContent(), XContentType.JSON);
        try {
            final IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
            logger.info("Migration was finished with status: {}", response.status().getStatus());
        } catch (IOException e) {
            throw new ProductsMigrationException("Products migration was failed");
        }
    }

    private Settings prepareIndexSettings() {
        return Settings.builder()
            .put("index.number_of_shards", shards)
            .put("index.number_of_replicas", replicas)
            .build();
    }

    private String getProductsJsonContent() {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(MIGRATION_JSON)) {
            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode jsonNode = mapper.readValue(in, JsonNode.class);
            return mapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            throw new ProductsJsonParseException("Cannot parse json");
        }
    }

    private static final class CreateIndexException extends RuntimeException {

        public CreateIndexException(String message) {
            super(message);
        }
    }

    private static final class ProductsMigrationException extends RuntimeException {

        public ProductsMigrationException(String message) {
            super(message);
        }
    }

    private static final class ProductsJsonParseException extends RuntimeException {

        public ProductsJsonParseException(String message) {
            super(message);
        }
    }
}
