package uss.code.admin.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import uss.code.admin.dto.internal.SemesterRefDto

@JvmRecord
data class CourseSummaryResponse(
    @field:Schema(description = "적재된 학기. 강의가 하나도 없으면 null")
    val semester: SemesterRefDto?,

    @field:Schema(
        description = "적재된 강의 수. 폐강을 포함한다",
        example = "1203"
    )
    val courseCount: Long,

    @field:Schema(
        description = "적재된 시간표 수",
        example = "2847"
    )
    val scheduleCount: Long,
) {
    companion object {
        @JvmStatic
        fun of(
            semester: SemesterRefDto?,
            courseCount: Long,
            scheduleCount: Long,
        ): CourseSummaryResponse {
            return CourseSummaryResponse(
                semester = semester,
                courseCount = courseCount,
                scheduleCount = scheduleCount,
            )
        }
    }
}
