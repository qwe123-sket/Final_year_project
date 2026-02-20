package com.example.final_year_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置：缓存与计数用 Template
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Long> redisTemplateLong(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Long> t = new RedisTemplate<>();
        t.setConnectionFactory(connectionFactory);
        t.setKeySerializer(new StringRedisSerializer());
        t.setValueSerializer(new GenericToStringSerializer<>(Long.class));
        t.setHashKeySerializer(new StringRedisSerializer());
        t.setHashValueSerializer(new GenericToStringSerializer<>(Long.class));
        t.afterPropertiesSet();
        return t;
    }
}
