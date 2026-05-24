package com.wandeTech.rateLimiter.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean
    public RedisClient getRedisClient() {
        return RedisClient.create(

                RedisURI.builder()
                        .withHost(redisHost)
                        .withPort(redisPort)
                        .build()
        );

    }
    public ProxyManager<String> proxyManager(RedisClient redisClient) {
        var redisConnection=redisClient.connect(
                RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
        );
        // Define the TTL for bucket to avoid the ttl to stay forever
       var expirationStrategy= ExpirationAfterWriteStrategy
                .basedOnTimeForRefillingBucketUpToMax(Duration.ofHours(1));

       var clientConfig= ClientSideConfig.getDefault()
                .withExpirationAfterWriteStrategy(expirationStrategy);

       //build proxy manager
        return LettuceBasedProxyManager.builderFor(redisConnection)
                .withClientSideConfig(clientConfig).build();

    }
}

