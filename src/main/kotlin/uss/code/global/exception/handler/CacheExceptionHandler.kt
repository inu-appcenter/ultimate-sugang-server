package uss.code.global.exception.handler

import org.apache.logging.log4j.LogManager
import org.springframework.cache.Cache
import org.springframework.cache.interceptor.CacheErrorHandler

class CacheExceptionHandler : CacheErrorHandler {
    override fun handleCacheGetError(
        exception: RuntimeException,
        cache: Cache,
        key: Any,
    ) {
        log.warn(
            "Cache get failed, falling back to source. cache={}, key={}, message={}",
            cache.name,
            key,
            exception.message,
        )
    }

    override fun handleCachePutError(
        exception: RuntimeException,
        cache: Cache,
        key: Any,
        value: Any?,
    ) {
        log.warn("Cache put failed. cache={}, key={}, message={}", cache.name, key, exception.message)
    }

    override fun handleCacheEvictError(
        exception: RuntimeException,
        cache: Cache,
        key: Any,
    ) {
        log.warn("Cache evict failed. cache={}, key={}, message={}", cache.name, key, exception.message)
    }

    override fun handleCacheClearError(
        exception: RuntimeException,
        cache: Cache,
    ) {
        log.warn("Cache clear failed. cache={}, message={}", cache.name, exception.message)
    }

    companion object {
        private val log = LogManager.getLogger(CacheExceptionHandler::class.java)
    }
}
