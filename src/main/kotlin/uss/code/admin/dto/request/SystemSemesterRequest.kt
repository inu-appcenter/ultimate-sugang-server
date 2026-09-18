package uss.code.admin.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import uss.code.course.domain.CourseTerm

data class SystemSemesterRequest(
    @field:Schema(
        description = "표시 학년도. 4자리 정수",
        example = "2026"
    )
    @field:NotNull(message = "학년도가 비어있습니다.")
    @field:Min(value = 2000, message = "학년도는 4자리 정수여야 합니다.")
    @field:Max(value = 2100, message = "학년도는 4자리 정수여야 합니다.")
    val academicYear: Int?,

    @field:Schema(
        description = "표시 학기",
        example = "SECOND"
    )
    @field:NotNull(message = "학기가 비어있습니다.")
    val term: CourseTerm?,
)
