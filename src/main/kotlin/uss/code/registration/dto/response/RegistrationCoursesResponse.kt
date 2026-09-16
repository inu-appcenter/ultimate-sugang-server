package uss.code.registration.dto.response

@JvmRecord
data class RegistrationCoursesResponse(
    val registrationCourseResponses: List<RegistrationCourseResponse>,
) {
    companion object {
        @JvmStatic
        fun of(
            registrationCourseResponses: List<RegistrationCourseResponse>,
        ): RegistrationCoursesResponse = RegistrationCoursesResponse(
            registrationCourseResponses = registrationCourseResponses,
        )
    }
}
