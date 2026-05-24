package com.wandeTech.rateLimiter.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class RateLimitingService {
    private static final  int REQUEST_PER_MINUTES = 10;
    private  final ProxyManager<String> proxyManager;


    ///key represent the IP address/token/Userid
    /// this method will be called in every incoming request
    public Bucket resolveBucket(String key){
        Supplier<BucketConfiguration> configSupplier=this::getConfig;

        return proxyManager
                .builder()
                .build(key, configSupplier);
    }

    /**
     * Act as factory that produces BucketConfiguration object
     * for newly created bucket
     */
    private BucketConfiguration getConfig() {

        //how many token a bucket can hold
        // How many quickly token are refilled

        var limit = Bandwidth.builder()
                .capacity(REQUEST_PER_MINUTES)
                .refillIntervally(REQUEST_PER_MINUTES, Duration.ofMinutes(1))
                .build();

        return BucketConfiguration.builder()
                .addLimit(limit)
                .build();
    }
}
