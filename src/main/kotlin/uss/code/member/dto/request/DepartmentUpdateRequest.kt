package uss.code.member.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class DepartmentUpdateRequest(
    @field:Schema(
        description = "변경할 학과",
        example = "COMPUTER_ENGINEERING"
    )
    @field:NotBlank(message = "학과가 비어있습니다.")
    val department: String?,
)
