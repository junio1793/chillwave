package com.wrapper.infrastructure.config.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CaffeineCacheConfig {

    private static final Logger logger = LoggerFactory.getLogger(CaffeineCacheConfig.class);

    @Value("${spring.cache.caffeine.objectObjectCaffeine.initalCapacibility:10}")
    private String initalCapacibility;

    @Value("${spring.cache.caffeine.objectObjectCaffeine.maximumSize:10000}")
    private String  maximumSize;

    private String[] arrSpringCacheNames = new String[]{"endpointQueryConfig"};

    @Bean
    public Cache objectObjectCaffeine() {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .maximumSize(Integer.parseInt((maximumSize)))
                .initialCapacity(Integer.parseInt(initalCapacibility))
                .evictionListener((Object key, Object value, RemovalCause cause) ->
                        logger.info(String.format(
                        "Key %s was evicted (%s)%n", key, cause)))
                .removalListener((Object key, Object value,
                                  RemovalCause cause) ->
                        logger.info(String.format(
                                "Key %s was removed (%s)%n", key, cause)));

        return caffeine.build();
    }

}
