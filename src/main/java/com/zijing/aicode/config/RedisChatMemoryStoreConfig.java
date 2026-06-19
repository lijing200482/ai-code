package com.zijing.aicode.config;

import cn.hutool.core.util.StrUtil;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.community.store.memory.chat.redis.StoreType;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 持久化对话记忆
 */
@Configuration
public class RedisChatMemoryStoreConfig {

    @Bean
    public RedisChatMemoryStore redisChatMemoryStore(RedisProperties redisProperties) {
        RedisChatMemoryStore.Builder builder = RedisChatMemoryStore.builder()
                .host(redisProperties.getHost())
                .port(redisProperties.getPort())
                .password(redisProperties.getPassword())
                .storeType(StoreType.STRING)
                .ttl(1800L);
        if (StrUtil.isNotBlank(redisProperties.getPassword())) {
            builder.user("default");
        }
        return builder.build();
    }
}



