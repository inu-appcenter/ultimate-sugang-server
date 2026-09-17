package uss.code.auth.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@JvmRecord
data class StudentIdAvailabilityResponse(
    @field:Schema(
        description = "학번 사용 가능 여부",
        example = "true"
    )
    val available: Boolean,
) {
    companion object {
        @JvmStatic
        fun of(available: Boolean): StudentIdAvailabilityResponse {
            return StudentIdAvailabilityResponse(available = available)
        }
    }
}
