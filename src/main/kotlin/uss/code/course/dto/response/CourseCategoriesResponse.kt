package uss.code.course.dto.response

data class CourseCategoriesResponse(
    val categoryResponses: List<CourseCategoryResponse>,
) {
    companion object {
        fun of(categoryResponses: List<CourseCategoryResponse>): CourseCategoriesResponse {
            return CourseCategoriesResponse(categoryResponses = categoryResponses)
        }
    }
}
