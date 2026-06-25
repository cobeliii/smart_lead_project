package org.cobeliii.smartleadqualification.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@EnableConfigurationProperties(HuggingFaceProperties.class)
public class HuggingFaceConfig {

    @Bean
    RestClient huggingFaceRestClient(HuggingFaceProperties properties) {
        if (!StringUtils.hasText(properties.apiKey())) {
            throw new IllegalStateException("Missing Hugging Face API key. Set the HUGGINGFACE_API_KEY environment variable.");
        }

        if (!StringUtils.hasText(properties.baseUrl())) {
            throw new IllegalStateException("Missing Hugging Face base URL. Set huggingface.base-url in application.properties.");
        }

        if (properties.chat() == null || !StringUtils.hasText(properties.chat().model())) {
            throw new IllegalStateException("Missing Hugging Face chat model. Set huggingface.chat.model in application.properties.");
        }

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