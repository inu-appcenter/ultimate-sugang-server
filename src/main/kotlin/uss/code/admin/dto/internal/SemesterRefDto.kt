package uss.code.admin.dto.internal

import io.swagger.v3.oas.annotations.media.Schema
import uss.code.course.domain.CourseTerm

@JvmRecord
data class SemesterRefDto(
    @field:Schema(
        description = "학년도",
        example = "2026"
    )
    val academicYear: Int,

    @field:Schema(
        description = "학기",
        example = "SECOND"
    )
    val term: CourseTerm,
) {
    fun matches(
        academicYear: Int,
        term: CourseTerm,
    ): Boolean = this.academicYear == academicYear && this.term == term

    companion object {
        @JvmStatic
        fun of(
            academicYear: Int,
            term: CourseTerm,
        ): SemesterRefDto = SemesterRefDto(
            academicYear = academicYear,
            term = term,
        )
    }
}
