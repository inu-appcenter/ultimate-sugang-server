package uss.code.registration.dto.response

import uss.code.course.domain.Course
import uss.code.course.dto.response.CourseResponse

@JvmRecord
data class RegistrationResponse(
    val courseResponse: CourseResponse,
) {
    companion object {
        @JvmStatic
        fun from(course: Course): RegistrationResponse = RegistrationResponse(
            courseResponse = CourseResponse.from(course),
        )
    }
}
