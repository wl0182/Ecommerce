package com.wassimlagnaoui.Ecommerce.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CachingConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(); // Default cache manager: no specific cache names
        cacheManager.setCaffeine(caffeineCacheBuilder()); // Default cache configuration
        return cacheManager;
    }

    @Bean
    public Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(100) // initial capacity of the cache
                .maximumSize(500) // maximum number of entries in the cache
                .expireAfterAccess(10, TimeUnit.MINUTES) // expire entries after 10 minutes of access
                .weakKeys() // use weak references for keys
                .recordStats(); // enable statistics for monitoring
    }

    @Bean("productCacheManager")
    public CacheManager productCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("products", "product");
        cacheManager.setCaffeine(productCacheBuilder());
        return cacheManager;
    }

    @Bean
    public Caffeine<Object, Object> productCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(200)
                .maximumSize(1000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .refreshAfterWrite(15, TimeUnit.MINUTES)
                .recordStats();
    }

    @Bean("categoryCacheManager")
    public CacheManager categoryCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("categories", "category");
        cacheManager.setCaffeine(categoryCacheBuilder());
        return cacheManager;
    }

    @Bean
    public Caffeine<Object, Object> categoryCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(50)
                .maximumSize(200)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .recordStats();
    }

    @Bean("customerCacheManager")
    public CacheManager customerCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("customers", "customer");
        cacheManager.setCaffeine(customerCacheBuilder());
        return cacheManager;
    }

    @Bean
    public Caffeine<Object, Object> customerCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterAccess(20, TimeUnit.MINUTES)
                .recordStats();
    }

    @Bean("orderCacheManager")
    public CacheManager orderCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("orders", "order", "userOrders");
        cacheManager.setCaffeine(orderCacheBuilder());
        return cacheManager;
    }

    @Bean
    public Caffeine<Object, Object> orderCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(150)
                .maximumSize(800)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .recordStats();
    }
}
