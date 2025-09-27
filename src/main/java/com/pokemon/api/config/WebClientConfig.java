package com.pokemon.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://pokeapi.co/api/v2")
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB buffer
                .build();
    }
}