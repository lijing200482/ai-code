package com.zijing.aicode.config;

import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;


@Configuration
public class StreamingChatModelConfig {

    @Value("${langchain4j.open-ai.streaming-chat-model.base-url}")
    private String baseUrl;
    @Value("${langchain4j.open-ai.streaming-chat-model.api-key}")
    private String apiKey;
    @Value("${langchain4j.open-ai.streaming-chat-model.model-name}")
    private String modelName;
    @Value("${langchain4j.open-ai.streaming-chat-model.max-tokens}")
    private Integer maxTokens;
    @Value("${langchain4j.open-ai.streaming-chat-model.log-requests}")
    private Boolean logRequests;
    @Value("${langchain4j.open-ai.streaming-chat-model.log-responses}")
    private Boolean logResponses;

    /**
     * 推理流式模型（用于 Vue 项目生成，带工具调用）
     */
    @Bean
    @Scope("prototype")
    public StreamingChatModel StreamingChatModelPrototype() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .httpClientBuilder(new SpringRestClientBuilderFactory().create())
                .build();
    }

}
