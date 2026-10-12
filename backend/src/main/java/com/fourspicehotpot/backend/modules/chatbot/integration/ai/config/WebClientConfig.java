package com.fourspicehotpot.backend.modules.chatbot.integration.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://api.groq.com")
                .build();
    }

}