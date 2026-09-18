package uss.code.global.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uss.code.auth.resolver.AdminAuthArgumentResolver
import uss.code.auth.resolver.AuthArgumentResolver

@Configuration
class ArgumentResolverConfig(
    private val authArgumentResolver: AuthArgumentResolver,
    private val adminAuthArgumentResolver: AdminAuthArgumentResolver,
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authArgumentResolver)
        resolvers.add(adminAuthArgumentResolver)
    }
}
