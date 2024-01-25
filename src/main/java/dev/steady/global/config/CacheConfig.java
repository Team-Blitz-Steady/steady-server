package dev.steady.global.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final int DEFAULT_CACHE_TIME = 60 * 60 * 24;

    @Getter
    private enum CacheType {
        STEADIES("steadies", DEFAULT_CACHE_TIME, 50),
        ;

        private final String cacheName;
        private final int expireAfterWriteTime;
        private final int maxSize;

        CacheType(String cacheName, int expireAfterWriteTime, int maxSize) {
            this.cacheName = cacheName;
            this.expireAfterWriteTime = expireAfterWriteTime;
            this.maxSize = maxSize;
        }
    }

    @Bean
    public CacheManager cacheManager() {
        List<CaffeineCache> caches = Arrays.stream(CacheType.values())
                .map(cacheType ->
                        new CaffeineCache(
                                cacheType.getCacheName(),
                                Caffeine.newBuilder()
                                        .recordStats()
                                        .expireAfterWrite(cacheType.getExpireAfterWriteTime(), TimeUnit.SECONDS)
                                        .maximumSize(cacheType.maxSize)
                                        .build()
                        ))
                .toList();

        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(caches);
        return cacheManager;
    }

}
