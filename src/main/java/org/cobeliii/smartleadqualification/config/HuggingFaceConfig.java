package org.cobeliii.smartleadqualification.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@EnableConfigurationProperties(HuggingFaceProperties.class)
public class HuggingFaceConfig {

    @Bean
    RestClient huggingFaceRestClient(HuggingFaceProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.apiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    HuggingFaceService huggingFaceService(RestClient huggingFaceRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(huggingFaceRestClient))
                .build();

        return factory.createClient(HuggingFaceService.class);
    }
}