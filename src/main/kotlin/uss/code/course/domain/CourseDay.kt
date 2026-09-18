package uss.code.course.domain

import uss.code.global.exception.domain.ExceptionCode.INVALID_ENUM_TYPE
import uss.code.global.exception.domain.RestApiException

enum class CourseDay(
    val code: String,
    val displayName: String,
) {
    MONDAY("1", "월"),
    TUESDAY("2", "화"),
    WEDNESDAY("3", "수"),
    THURSDAY("4", "목"),
    FRIDAY("5", "금"),
    SATURDAY("6", "토"),
    SUNDAY("", "일");

    companion object {
        fun tryFromCode(code: String): CourseDay? {
            return entries.firstOrNull { it.code.isNotBlank() && it.code == code }
        }

        fun fromCode(code: String): CourseDay {
            return tryFromCode(code) ?: throw RestApiException(INVALID_ENUM_TYPE)
        }
    }
}
