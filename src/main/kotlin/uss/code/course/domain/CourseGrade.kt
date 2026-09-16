package uss.code.course.domain

import uss.code.global.exception.domain.ExceptionCode.INVALID_ENUM_TYPE
import uss.code.global.exception.domain.RestApiException

enum class CourseGrade(
    val code: String,
    @get:JvmName("getName")
    val displayName: String,
    val year: Int,
) {
    FRESHMAN("1", "1학년", 1),
    SOPHOMORE("2", "2학년", 2),
    JUNIOR("3", "3학년", 3),
    SENIOR("4", "4학년", 4),
    ALL("0", "전학년", -1);

    companion object {
        @JvmStatic
        fun fromCode(code: String): CourseGrade =
            entries.firstOrNull { it.code.isNotBlank() && it.code == code }
                ?: throw RestApiException(INVALID_ENUM_TYPE)
    }
}
