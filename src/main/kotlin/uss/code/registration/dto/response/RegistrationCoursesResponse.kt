package uss.code.registration.dto.response

data class RegistrationCoursesResponse(
    val registrationCourseResponses: List<RegistrationCourseResponse>,
) {
    companion object {
        fun of(
            registrationCourseResponses: List<RegistrationCourseResponse>,
        ): RegistrationCoursesResponse {
            return RegistrationCoursesResponse(
                registrationCourseResponses = registrationCourseResponses,
            )
        }
    }
}
