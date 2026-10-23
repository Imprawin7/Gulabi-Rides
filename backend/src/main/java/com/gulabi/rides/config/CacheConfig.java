package com.gulabi.rides.config;
import lombok.extern.slf4j.Slf4j; import org.springframework.cache.Cache; import org.springframework.cache.annotation.*;
import org.springframework.cache.interceptor.*; import org.springframework.context.annotation.Configuration;

/** Caching is an optimisation: if Redis is down, log it and carry on without the cache instead of failing ride quotes. */
@Configuration @EnableCaching @Slf4j
public class CacheConfig implements CachingConfigurer {
  @Override public CacheErrorHandler errorHandler() {
    return new SimpleCacheErrorHandler() {
      @Override public void handleCacheGetError(RuntimeException e, Cache c, Object k) { log.warn("Cache read failed ({}): {}", c.getName(), e.getMessage()); }
      @Override public void handleCachePutError(RuntimeException e, Cache c, Object k, Object v) { log.warn("Cache write failed ({}): {}", c.getName(), e.getMessage()); }
    };
  }
}
