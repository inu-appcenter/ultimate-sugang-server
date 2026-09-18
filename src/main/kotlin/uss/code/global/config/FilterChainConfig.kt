package uss.code.global.config

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import tools.jackson.databind.ObjectMapper
import uss.code.auth.filter.AdminAuthenticationFilter
import uss.code.auth.filter.JwtAuthenticationFilter
import uss.code.auth.filter.JwtExceptionFilter
import uss.code.auth.infra.JwtProvider
import uss.code.global.filter.HttpLoggingFilter

@Configuration
class FilterChainConfig(
    private val objectMapper: ObjectMapper,
    private val jwtProvider: JwtProvider,

    private val corsConfigurationSource: CorsConfigurationSource,
) {
    @Bean
    fun corsFilter(): FilterRegistrationBean<CorsFilter> {
        val bean = FilterRegistrationBean<CorsFilter>()

        bean.setFilter(CorsFilter(corsConfigurationSource))
        bean.order = CORS_FILTER_ORDER

        return bean
    }

    @Bean
    fun httpLoggingFilter(): FilterRegistrationBean<HttpLoggingFilter> {
        val bean = FilterRegistrationBean<HttpLoggingFilter>()

        bean.setFilter(HttpLoggingFilter())
        bean.order = HTTP_LOGGING_FILTER_ORDER

        return bean
    }

    @Bean
    fun jwtExceptionFilter(): FilterRegistrationBean<JwtExceptionFilter> {
        val bean = FilterRegistrationBean<JwtExceptionFilter>()

        bean.setFilter(JwtExceptionFilter(objectMapper))
        bean.order = JWT_EXCEPTION_FILTER_ORDER

        return bean
    }

    @Bean
    fun jwtAuthenticationFilter(): FilterRegistrationBean<JwtAuthenticationFilter> {
        val bean = FilterRegistrationBean<JwtAuthenticationFilter>()

        bean.setFilter(JwtAuthenticationFilter(jwtProvider))
        bean.order = JWT_AUTHENTICATION_FILTER_ORDER

        return bean
    }

    @Bean
    fun adminAuthenticationFilter(): FilterRegistrationBean<AdminAuthenticationFilter> {
        val bean = FilterRegistrationBean<AdminAuthenticationFilter>()

        bean.setFilter(AdminAuthenticationFilter(jwtProvider))
        bean.order = ADMIN_AUTHENTICATION_FILTER_ORDER

        return bean
    }

    companion object {
        private const val CORS_FILTER_ORDER = 0
        private const val HTTP_LOGGING_FILTER_ORDER = 1
        private const val JWT_EXCEPTION_FILTER_ORDER = 2
        private const val JWT_AUTHENTICATION_FILTER_ORDER = 3
        private const val ADMIN_AUTHENTICATION_FILTER_ORDER = 4
    }
}
