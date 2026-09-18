package uss.code.auth.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class AuthTokenResponse(
    @field:Schema(
        description = "액세스 토큰",
        example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzAwMDAwMDAwfQ.signature"
    )
    val accessToken: String,
) {
    companion object {
        fun of(accessToken: String): AuthTokenResponse {
            return AuthTokenResponse(accessToken = accessToken)
        }
    }
}
