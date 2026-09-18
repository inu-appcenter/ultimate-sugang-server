package uss.code.course.dto.response

data class CourseTermsResponse(
    val termResponses: List<CourseTermResponse>,
) {
    companion object {
        fun of(termResponses: List<CourseTermResponse>): CourseTermsResponse {
            return CourseTermsResponse(termResponses = termResponses)
        }
    }
}
