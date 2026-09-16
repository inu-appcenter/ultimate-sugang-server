package uss.code.course.dto.response

import uss.code.course.dto.internal.CourseCategoryDto

@JvmRecord
data class CourseAreaResponse(
    val code: String,

    val name: String,
) {
    companion object {
        @JvmStatic
        fun from(courseCategory: CourseCategoryDto): CourseAreaResponse = CourseAreaResponse(
            code = courseCategory.areaCode,
            name = courseCategory.areaName,
        )
    }
}
