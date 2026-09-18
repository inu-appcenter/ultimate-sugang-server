package uss.code.course.dto.response

data class CourseCategoryResponse(
    val code: String,

    val name: String,

    val areaResponses: List<CourseAreaResponse>,
) {
    companion object {
        fun of(
            code: String,
            name: String,
            areaResponses: List<CourseAreaResponse>,
        ): CourseCategoryResponse {
            return CourseCategoryResponse(
                code = code,
                name = name,
                areaResponses = areaResponses,
            )
        }
    }
}
