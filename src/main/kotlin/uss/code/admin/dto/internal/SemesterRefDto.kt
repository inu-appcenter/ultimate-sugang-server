package uss.code.admin.dto.internal

import io.swagger.v3.oas.annotations.media.Schema
import uss.code.course.domain.CourseTerm

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
    ): Boolean {
        return this.academicYear == academicYear && this.term == term
    }

    companion object {
        fun of(
            academicYear: Int,
            term: CourseTerm,
        ): SemesterRefDto {
            return SemesterRefDto(
                academicYear = academicYear,
                term = term,
            )
        }
    }
}
