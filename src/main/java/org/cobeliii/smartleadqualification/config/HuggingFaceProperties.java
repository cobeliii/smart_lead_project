package org.cobeliii.smartleadqualification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "huggingface")
public record HuggingFaceProperties(
        String apiKey,
        String baseUrl,
        Chat chat
) {
    public record Chat(
            String model
    ) {
    }
}