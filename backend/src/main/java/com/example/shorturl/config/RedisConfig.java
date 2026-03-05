package com.example.shorturl.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 创建统一的 ObjectMapper，支持 Java 8 日期时间
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        // 注册 JavaTimeModule 支持 LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    /**
     * 配置 RedisTemplate
     * 使用Jackson2JsonRedisSerializer序列化value
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用Jackson2JsonRedisSerializer序列化value
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = createObjectMapper();
        serializer.setObjectMapper(objectMapper);

        // 使用StringRedisSerializer序列化key
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // key采用String的序列化方式
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        // value采用jackson序列化方式
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }

    /**
     * 配置 CacheManager
     * 使用Redis缓存，TTL可配置
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 创建支持 Java8 时间类型的 ObjectMapper
        ObjectMapper objectMapper = createObjectMapper();
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        // 默认配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))  // 默认TTL 30分钟
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        // 针对不同缓存使用不同的TTL配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 短链接缓存 - TTL 10分钟（高频访问）
        cacheConfigurations.put("shortLinkByCode",
                defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // 用户名缓存 - TTL 30分钟（用户信息相对稳定）
        cacheConfigurations.put("userByUsername",
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
