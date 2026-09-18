package uss.code.global.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uss.code.global.interceptor.ApiPerformanceInterceptor

@Configuration
class InterceptorConfig(
    private val apiPerformanceInterceptor: ApiPerformanceInterceptor,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(apiPerformanceInterceptor)
            .excludePathPatterns(EXCLUDE_PATTERNS)
    }

    companion object {
        private val EXCLUDE_PATTERNS = listOf(
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
        )
    }
}
