package uss.code.course.domain

import uss.code.global.exception.domain.ExceptionCode.INVALID_ENUM_TYPE
import uss.code.global.exception.domain.RestApiException
import java.util.*

enum class CourseDay(
    val code: String,
    @get:JvmName("getName")
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
        @JvmStatic
        fun tryFromCode(code: String?): Optional<CourseDay> =
            Optional.ofNullable(entries.firstOrNull { it.code.isNotBlank() && it.code == code })

        @JvmStatic
        fun fromCode(code: String): CourseDay =
            tryFromCode(code).orElseThrow { RestApiException(INVALID_ENUM_TYPE) }
    }
}
