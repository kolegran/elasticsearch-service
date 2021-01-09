package com.github.kolegran.elasticsearch.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProductsService {

    private static final String INDEX_NAME = "products";
    private static final String PRODUCTS_JSON = "products.json";
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

    public int migrateProducts() {
        for (Map.Entry<Integer, String> entry : parseProductsJson().entrySet()) {
            final IndexRequest request = new IndexRequest(INDEX_NAME);
            request.id(entry.getKey().toString());
            request.source(entry.getValue(), XContentType.JSON);
            try {
                restHighLevelClient.index(request, RequestOptions.DEFAULT);
            } catch (IOException e) {
                logger.error("Products migration was failed");
                return RestStatus.INTERNAL_SERVER_ERROR.getStatus();
            }
        }
        logger.info("Products migration was successful");
        return RestStatus.OK.getStatus();
    }

    private Settings prepareIndexSettings() {
        return Settings.builder()
            .put("index.number_of_shards", shards)
            .put("index.number_of_replicas", replicas)
            .build();
    }

    private Map<Integer, String> parseProductsJson() {
        final Map<Integer, String> productsByCode = new HashMap<>();
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(PRODUCTS_JSON)) {
            final JsonNode jsonNode = new ObjectMapper().readTree(is);
            for (JsonNode node : jsonNode) {
                productsByCode.put(node.get("productCode").asInt(), node.toPrettyString());
            }
        } catch (Exception e) {
            throw new ProductsJsonParseException("Cannot parse products JSON");
        }
        return productsByCode;
    }

    private static final class CreateIndexException extends RuntimeException {

        public CreateIndexException(String message) {
            super(message);
        }
    }

    private static final class ProductsJsonParseException extends RuntimeException {

        public ProductsJsonParseException(String message) {
            super(message);
        }
    }
}
