package uss.code.auth.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequest(
    @field:Schema(
        description = "학번",
        example = "202012345"
    )
    @field:NotBlank(message = "학번이 비어있습니다.")
    val studentId: String?,

    @field:Schema(
        description = "비밀번호",
        example = "password1234"
    )
    @field:NotBlank(message = "비밀번호가 비어있습니다.")
    @field:Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
    val password: String?,
)
