package uss.code.course.domain

import uss.code.global.exception.domain.ExceptionCode.INVALID_ENUM_TYPE
import uss.code.global.exception.domain.RestApiException

enum class CourseType(
    val code: String,
    val displayName: String,
) {
    LECTURE("1", "강의(이론)"),
    THEORY_LANGUAGE("20", "이론(어학)"),
    LAB("2", "실험실습"),
    THEORY_LAB("5", "이론실험실습"),
    PHYSICAL_EDUCATION("3", "체육실기"),
    ART_PRACTICE("4", "미술실기"),
    ARTS_PHYSICAL_PRACTICE("23", "예술체육실기"),
    E_LEARNING("8", "e-Learning"),
    E_LEARNING_HUSS("26", "e-Learning(HUSS)"),
    ONLINE_BLENDED("24", "온라인혼합형강좌"),
    ONLINE_BLENDED_HUSS("27", "온라인혼합형강좌(HUSS)"),
    FIELD_TYPE_HUSS("28", "현장형(HUSS)"),
    K_MOOC("25", "K-MOOC"),
    OCU("7", "열린사이버대학(OCU)"),
    SOCIAL_SERVICE_1("11", "사회봉사(1)"),
    SOCIAL_SERVICE_2("12", "사회봉사(2)"),
    SOCIAL_SERVICE_3("13", "사회봉사(3)"),
    RISE_WITHOUT_SCHEDULE("22", "RISE(시간표 없음)"),
    RISE_WITH_SCHEDULE("21", "RISE(시간표 있음)"),
    SELF_DESIGNED_SEMINAR("17", "자기설계세미나");

    companion object {
        private val TAG_TYPES = setOf(
            E_LEARNING,
            E_LEARNING_HUSS,
            ONLINE_BLENDED,
            ONLINE_BLENDED_HUSS,
        )

        fun isTagType(typeCode: String): Boolean {
            return TAG_TYPES.any { it.code == typeCode }
        }

        fun fromCode(code: String): CourseType {
            return entries.firstOrNull { it.code.isNotBlank() && it.code == code }
                ?: throw RestApiException(INVALID_ENUM_TYPE)
        }
    }
}
