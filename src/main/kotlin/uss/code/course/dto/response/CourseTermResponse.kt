package uss.code.course.dto.response

import uss.code.course.dto.internal.CourseTermInfoDto

@JvmRecord
data class CourseTermResponse(
    val academicYear: Int,

    val termCode: String,

    val termName: String,
) {
    companion object {
        @JvmStatic
        fun from(courseTermInfo: CourseTermInfoDto): CourseTermResponse {
            return CourseTermResponse(
                academicYear = courseTermInfo.academicYear,
                termCode = courseTermInfo.term.code,
                termName = courseTermInfo.term.displayName,
            )
        }
    }
}
