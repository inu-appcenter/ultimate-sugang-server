package uss.code.auth.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class EmailAvailabilityResponse(
    @field:Schema(
        description = "이메일 사용 가능 여부",
        example = "true"
    )
    val available: Boolean,
) {
    companion object {
        fun of(available: Boolean): EmailAvailabilityResponse {
            return EmailAvailabilityResponse(available = available)
        }
    }
}
