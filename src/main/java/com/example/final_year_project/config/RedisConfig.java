package com.example.final_year_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * 专门给计数场景用的 RedisTemplate<String, Long>，
     * 默认的 RedisTemplate 序列化方式不太适合做 increment 操作
     */
    @Bean
    public RedisTemplate<String, Long> redisTemplateLong(RedisConnectionFactory factory) {
        RedisTemplate<String, Long> tpl = new RedisTemplate<>();
        tpl.setConnectionFactory(factory);
        tpl.setKeySerializer(new StringRedisSerializer());
        tpl.setValueSerializer(new GenericToStringSerializer<>(Long.class));
        tpl.setHashKeySerializer(new StringRedisSerializer());
        tpl.setHashValueSerializer(new GenericToStringSerializer<>(Long.class));
        tpl.afterPropertiesSet();
        return tpl;
    }
}
