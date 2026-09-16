package uss.code.course.dto.response

@JvmRecord
data class CoursesResponse(
    val courseResponses: List<CourseResponse>,
) {
    companion object {
        @JvmStatic
        fun of(courseResponses: List<CourseResponse>): CoursesResponse =
            CoursesResponse(courseResponses = courseResponses)
    }
}
