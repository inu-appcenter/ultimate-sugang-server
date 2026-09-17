package uss.code.course.domain

import uss.code.global.exception.domain.ExceptionCode.INVALID_ENUM_TYPE
import uss.code.global.exception.domain.RestApiException

enum class CourseTerm(
    val code: String,
    @get:JvmName("getName")
    val displayName: String,
) {
    FIRST("10", "1학기"),
    SECOND("20", "2학기"),
    SUMMER("30", "여름계절학기"),
    WINTER("40", "겨울계절학기");

    companion object {
        fun tryFromCode(code: String): CourseTerm? {
            return entries.firstOrNull { it.code.isNotBlank() && it.code == code }
        }

        @JvmStatic
        fun fromCode(code: String): CourseTerm {
            return tryFromCode(code) ?: throw RestApiException(INVALID_ENUM_TYPE)
        }
    }
}
