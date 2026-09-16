package uss.code.member.domain

import uss.code.global.exception.domain.ExceptionCode.INVALID_ENUM_TYPE
import uss.code.global.exception.domain.RestApiException

enum class AcademicStatus(
    @get:JvmName("getName")
    val displayName: String,
) {
    ENROLLED("재학"),
    LEAVE_OF_ABSENCE("휴학"),
    DEFERMENT("유예");

    companion object {
        @JvmStatic
        fun from(value: String): AcademicStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw RestApiException(INVALID_ENUM_TYPE)
    }
}
