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
    private final String scheme;

    public RestHighLevelClientConfiguration(
        @Value("${elasticsearch.host}") String host,
        @Value("${elasticsearch.port}") Integer port,
        @Value("${scheme}") String scheme
    ) {
        this.host = host;
        this.port = port;
        this.scheme = scheme;
    }

    @Bean(destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, scheme)));
    }

}
