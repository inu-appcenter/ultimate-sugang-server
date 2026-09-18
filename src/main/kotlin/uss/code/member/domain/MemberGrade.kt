package uss.code.member.domain

import uss.code.global.exception.domain.ExceptionCode.INVALID_ENUM_TYPE
import uss.code.global.exception.domain.RestApiException

enum class MemberGrade(
    val displayName: String,
    val year: Int,
) {
    FRESHMAN("1학년", 1),
    SOPHOMORE("2학년", 2),
    JUNIOR("3학년", 3),
    SENIOR("4학년", 4);

    companion object {
        fun from(value: String): MemberGrade {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw RestApiException(INVALID_ENUM_TYPE)
        }
    }
}
