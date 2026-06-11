package com.zijing.aicode.config;

import cn.hutool.core.util.StrUtil;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.community.store.memory.chat.redis.StoreType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 持久化对话记忆
 */
@Configuration
public class RedisChatMemoryStoreConfig {

    @Bean
    @ConfigurationProperties(prefix = "langchain4j.community.redis")
    public RedisChatMemoryProperties redisChatMemoryProperties() {
        return new RedisChatMemoryProperties();
    }

    @Bean
    public RedisChatMemoryStore redisChatMemoryStore(RedisChatMemoryProperties properties) {
        RedisChatMemoryStore.Builder builder = RedisChatMemoryStore.builder()
                .host(properties.getHost())
                .port(properties.getPort())
                .password(properties.getPassword())
                .storeType(StoreType.STRING)
                .ttl(properties.getTtlSeconds());
        if (StrUtil.isNotBlank(properties.getUser())) {
            builder.user(properties.getUser());
        }
        return builder.build();
    }

    @Data
    public static class RedisChatMemoryProperties {
        private String host;
        private int port;
        private String password;
        private String user;
        /** 对话记忆过期时间（秒），默认7天 */
        private Long ttlSeconds;
    }
}



