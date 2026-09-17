package uss.code.course.dto.response

@JvmRecord
data class CourseCategoriesResponse(
    val categoryResponses: List<CourseCategoryResponse>,
) {
    companion object {
        @JvmStatic
        fun of(categoryResponses: List<CourseCategoryResponse>): CourseCategoriesResponse {
            return CourseCategoriesResponse(categoryResponses = categoryResponses)
        }
    }
}
