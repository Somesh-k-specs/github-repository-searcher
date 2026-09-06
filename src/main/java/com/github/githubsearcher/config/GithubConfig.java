package com.github.githubsearcher.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GithubConfig {

    @Bean
    public RestClient githubRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl("https://api.github.com")
                .build();
    }
}