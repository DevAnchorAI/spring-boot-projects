package com.example.configuration;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    RedisCacheConfiguration cacheConfiguration() {

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(2));

    }

    //By default, Java serialization is inefficient and hard to inspect. JSON is a common choice.
//    @Bean
//    public RedisCacheConfiguration cacheConfig() {
//
//        return RedisCacheConfiguration.defaultCacheConfig()
//                .serializeValuesWith(
//                        RedisSerializationContext.SerializationPair
//                                .fromSerializer(new GenericJackson2JsonRedisSerializer())
//                );
//    }

    //Multiple Cache Configurations
    @Bean
    RedisCacheManagerBuilderCustomizer builderCustomizer() {

        return builder -> builder
                .withCacheConfiguration(
                        "employees",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(2)))
                .withCacheConfiguration(
                        "departments",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofHours(1)));
    }
}