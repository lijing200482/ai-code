package com.zijing.aicode.config;

import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class RoutingChatModelConfig {

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String baseUrl;
    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;
    @Value("${langchain4j.open-ai.chat-model.model-name}")
    private String modelName;
    @Value("${langchain4j.open-ai.chat-model.max-tokens}")
    private Integer maxTokens;
    @Value("${langchain4j.open-ai.chat-model.log-requests}")
    private Boolean logRequests;
    @Value("${langchain4j.open-ai.chat-model.log-responses}")
    private Boolean logResponses;

    /**
     * 智能路游模型
     */
    @Bean
    @Scope("prototype")
    public ChatModel routingChatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .httpClientBuilder(new SpringRestClientBuilderFactory().create())
                .build();
    }

}
