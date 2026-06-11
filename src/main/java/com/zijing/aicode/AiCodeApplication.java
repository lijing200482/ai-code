package com.zijing.aicode;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.zijing.aicode.mapper")
public class AiCodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCodeApplication.class, args);
    }

}
