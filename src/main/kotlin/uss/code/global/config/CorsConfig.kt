package uss.code.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class CorsConfig {
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        configuration.allowedOrigins = ALLOWED_ORIGINS
        configuration.allowedMethods = ALLOWED_METHODS
        configuration.addAllowedHeader(ALL_HEADER_PATTERN)
        configuration.exposedHeaders = EXPOSED_HEADERS
        configuration.allowCredentials = true
        configuration.maxAge = MAX_AGE

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration(ALL_PATH_PATTERN, configuration)

        return source
    }

    companion object {
        private const val ALL_PATH_PATTERN = "/**"
        private const val ALL_HEADER_PATTERN = "*"
        private const val MAX_AGE = 3600L

        private val ALLOWED_ORIGINS = listOf(
            "http://localhost:3000",
            "http://localhost:5173",
        )

        private val ALLOWED_METHODS = listOf("GET", "POST", "PUT", "DELETE", "PATCH")
        private val EXPOSED_HEADERS = listOf("Date")
    }
}
