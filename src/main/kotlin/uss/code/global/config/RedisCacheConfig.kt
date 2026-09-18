package uss.code.global.config

import io.lettuce.core.ClientOptions.DisconnectedBehavior
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer
import org.springframework.boot.data.redis.autoconfigure.LettuceClientOptionsBuilderCustomizer
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.interceptor.CacheErrorHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
import uss.code.course.dto.internal.CachedCoursesDto
import uss.code.course.infra.CourseCacheLoader
import uss.code.global.exception.handler.CacheExceptionHandler

@Configuration
@EnableCaching
class RedisCacheConfig : CachingConfigurer {
    override fun errorHandler(): CacheErrorHandler {
        return CacheExceptionHandler()
    }

    @Bean
    fun majorCoursesCacheCustomizer(): RedisCacheManagerBuilderCustomizer {
        return RedisCacheManagerBuilderCustomizer { builder ->
            builder.withCacheConfiguration(
                CourseCacheLoader.MAJOR_COURSES,
                builder.cacheDefaults().serializeValuesWith(
                    SerializationPair.fromSerializer(JacksonJsonRedisSerializer(CachedCoursesDto::class.java)),
                ),
            )
        }
    }

    @Bean
    fun generalEducationCoursesCacheCustomizer(): RedisCacheManagerBuilderCustomizer {
        return RedisCacheManagerBuilderCustomizer { builder ->
            builder.withCacheConfiguration(
                CourseCacheLoader.GENERAL_EDUCATION_COURSES,
                builder.cacheDefaults().serializeValuesWith(
                    SerializationPair.fromSerializer(JacksonJsonRedisSerializer(CachedCoursesDto::class.java)),
                ),
            )
        }
    }

    @Bean
    fun rejectCommandsWhileDisconnected(): LettuceClientOptionsBuilderCustomizer {
        return LettuceClientOptionsBuilderCustomizer { builder ->
            builder.disconnectedBehavior(DisconnectedBehavior.REJECT_COMMANDS)
        }
    }

    @Bean
    fun cacheFlusher(cacheManager: CacheManager): ApplicationRunner {
        return ApplicationRunner {
            cacheManager.cacheNames.forEach { name ->
                checkNotNull(cacheManager.getCache(name)).clear()
            }
        }
    }
}
