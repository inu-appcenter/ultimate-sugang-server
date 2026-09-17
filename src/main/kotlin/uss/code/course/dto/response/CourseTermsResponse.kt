package uss.code.course.dto.response

@JvmRecord
data class CourseTermsResponse(
    val termResponses: List<CourseTermResponse>,
) {
    companion object {
        @JvmStatic
        fun of(termResponses: List<CourseTermResponse>): CourseTermsResponse {
            return CourseTermsResponse(termResponses = termResponses)
        }
    }
}
