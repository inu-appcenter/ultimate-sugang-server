package uss.code.registration.dto.response

import uss.code.course.domain.Course
import uss.code.course.dto.response.CourseResponse

data class RegistrationResponse(
    val courseResponse: CourseResponse,
) {
    companion object {
        fun from(course: Course): RegistrationResponse {
            return RegistrationResponse(
                courseResponse = CourseResponse.from(course),
            )
        }
    }
}
