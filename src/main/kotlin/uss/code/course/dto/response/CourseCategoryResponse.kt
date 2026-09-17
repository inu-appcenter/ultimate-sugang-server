package uss.code.course.dto.response

@JvmRecord
data class CourseCategoryResponse(
    val code: String,

    val name: String,

    val areaResponses: List<CourseAreaResponse>,
) {
    companion object {
        @JvmStatic
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
