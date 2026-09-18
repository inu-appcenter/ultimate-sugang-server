package uss.code.course.dto.response

import uss.code.course.dto.internal.CourseCategoryDto

data class CourseAreaResponse(
    val code: String,

    val name: String,
) {
    companion object {
        fun from(courseCategory: CourseCategoryDto): CourseAreaResponse {
            return CourseAreaResponse(
                code = courseCategory.areaCode,
                name = courseCategory.areaName,
            )
        }
    }
}
