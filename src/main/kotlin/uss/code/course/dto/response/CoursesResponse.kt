package uss.code.course.dto.response

data class CoursesResponse(
    val courseResponses: List<CourseResponse>,
) {
    companion object {
        fun of(courseResponses: List<CourseResponse>): CoursesResponse {
            return CoursesResponse(courseResponses = courseResponses)
        }
    }
}
