package uss.code.auth.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@JvmRecord
data class AuthTokenResponse(
    @field:Schema(
        description = "액세스 토큰",
        example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzAwMDAwMDAwfQ.signature"
    )
    val accessToken: String,
) {
    companion object {
        @JvmStatic
        fun of(accessToken: String): AuthTokenResponse =
            AuthTokenResponse(accessToken = accessToken)
    }
}
