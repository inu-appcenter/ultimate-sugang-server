package uss.code.course.dto.internal

import uss.code.course.domain.CourseTerm

@JvmRecord
data class CourseTermInfoDto(
    val academicYear: Int,
    val term: CourseTerm,
)
