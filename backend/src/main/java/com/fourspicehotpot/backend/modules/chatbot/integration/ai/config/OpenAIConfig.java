package com.fourspicehotpot.backend.modules.chatbot.integration.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai")
public class OpenAIConfig {
    private String apiKey;
    private String model;
    private String baseUrl;
}