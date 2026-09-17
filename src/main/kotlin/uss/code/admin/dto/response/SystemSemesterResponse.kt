package uss.code.admin.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import uss.code.admin.domain.SystemSemester
import uss.code.course.domain.CourseTerm

@JvmRecord
data class SystemSemesterResponse(
    @field:Schema(
        description = "표시 학년도",
        example = "2026"
    )
    val academicYear: Int,

    @field:Schema(
        description = "표시 학기",
        example = "SECOND"
    )
    val term: CourseTerm,
) {
    companion object {
        @JvmStatic
        fun from(systemSemester: SystemSemester): SystemSemesterResponse {
            return SystemSemesterResponse(
                academicYear = systemSemester.academicYear,
                term = systemSemester.term,
            )
        }
    }
}
