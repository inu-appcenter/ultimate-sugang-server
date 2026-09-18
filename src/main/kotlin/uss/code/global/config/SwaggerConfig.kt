package uss.code.global.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.utils.SpringDocUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uss.code.auth.annotation.Auth

@Configuration
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI {
        val accessTokenSecurityScheme = SecurityScheme()
            .type(SecurityScheme.Type.APIKEY)
            .scheme("bearer")
            .bearerFormat("JWT")
            .`in`(SecurityScheme.In.HEADER)
            .name(ACCESS_TOKEN_KEY)

        val components = Components()
            .addSecuritySchemes(ACCESS_TOKEN_KEY, accessTokenSecurityScheme)

        val requirement = SecurityRequirement()
            .addList(ACCESS_TOKEN_KEY)

        return OpenAPI()
            .components(components)
            .info(apiInfo())
            .addSecurityItem(requirement)
            .servers(servers())
    }

    private fun apiInfo(): Info {
        return Info()
            .title("USS API Docs")
            .description("궁극의 수강신청 시뮬레이터 API 명세서")
            .version("1.0.0")
    }

    private fun servers(): List<Server> {
        return listOf(
            Server().url("http://localhost:8080").description("Dev env"),
            Server().url("https://uss.inuappcenter.kr").description("Production env"),
        )
    }

    companion object {
        private const val ACCESS_TOKEN_KEY = "access-token"

        init {
            SpringDocUtils.getConfig().addAnnotationsToIgnore(Auth::class.java)
        }
    }
}
