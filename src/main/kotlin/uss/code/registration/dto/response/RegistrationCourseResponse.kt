package uss.code.registration.dto.response

import uss.code.course.dto.response.CourseResponse
import uss.code.registration.domain.Registration
import java.time.format.DateTimeFormatter

@JvmRecord
data class RegistrationCourseResponse(
    val studentId: String,

    val resolvedType: String,

    val reAttendance: String,

    val createdAt: String,

    val courseResponse: CourseResponse,
) {
    companion object {
        private const val NO_RE_ATTENDANCE = ""
        private val CREATED_AT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

        @JvmStatic
        fun of(
            registration: Registration,
            studentId: String,
            resolvedType: String,
        ): RegistrationCourseResponse {
            return RegistrationCourseResponse(
                studentId = studentId,
                resolvedType = resolvedType,
                reAttendance = NO_RE_ATTENDANCE,
                createdAt = registration.createdAt.format(CREATED_AT_FORMAT),
                courseResponse = CourseResponse.from(registration.course),
            )
        }
    }
}
