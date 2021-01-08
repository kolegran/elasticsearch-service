package com.github.kolegran.elasticsearch.client;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Primary
public class RestHighLevelClientConfiguration {

    private final String host;
    private final Integer port;

    public RestHighLevelClientConfiguration(@Value("${elasticsearch.host}") String host, @Value("${elasticsearch.port}") Integer port) {
        this.host = host;
        this.port = port;
    }

    @Bean(destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(RestClient.builder(new HttpHost(host, port)));
    }
}
