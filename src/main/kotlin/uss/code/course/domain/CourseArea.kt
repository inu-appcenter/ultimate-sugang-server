package uss.code.course.domain

import uss.code.global.exception.domain.ExceptionCode.INVALID_ENUM_TYPE
import uss.code.global.exception.domain.ExceptionCode.INVALID_GENERAL_EDUCATION_AREA
import uss.code.global.exception.domain.RestApiException
import java.util.*

enum class CourseArea(
    val code: String,
    @get:JvmName("getName")
    val displayName: String,
) {
    // 전공 영역
    MAJOR_ADVANCED("35", "전공심화"),
    MAJOR_BASIC("31", "전공기초"),
    MAJOR_CORE("34", "전공핵심"),

    // 기초 교양 영역
    BASIC_SCIENCE_ENGINEERING("162", "기초과학·공학"),
    ACADEMIC_FOUNDATION("161", "학문의기초"),

    // 핵심 교양 영역
    CORE_INU_SEMINAR("171", "(핵심)INU세미나"),
    CORE_HUMANITIES("172", "(핵심)인문"),
    CORE_SOCIAL("173", "(핵심)사회"),
    CORE_SCIENCE_TECHNOLOGY("174", "(핵심)과학기술"),
    CORE_ARTS_SPORTS("175", "(핵심)예술체육"),
    CORE_FOREIGN_LANGUAGE("176", "(핵심)외국어"),

    // 심화 교양 영역
    HUMANITIES("182", "인문"),
    SOCIAL("183", "사회"),
    SCIENCE_TECHNOLOGY("184", "과학기술"),
    ARTS_SPORTS("185", "예술체육"),
    FOREIGN_LANGUAGE("186", "외국어"),

    // 기타
    TEACHING("51", "교직"),
    GENERAL_ELECTIVE("81", "일반선택"),
    MILITARY("71", "군사학");

    fun isGeneralEducationArea(): Boolean {
        return when (this) {
            BASIC_SCIENCE_ENGINEERING,
            ACADEMIC_FOUNDATION,
            CORE_INU_SEMINAR,
            CORE_HUMANITIES,
            CORE_SOCIAL,
            CORE_SCIENCE_TECHNOLOGY,
            CORE_ARTS_SPORTS,
            CORE_FOREIGN_LANGUAGE,
            HUMANITIES,
            SOCIAL,
            SCIENCE_TECHNOLOGY,
            ARTS_SPORTS,
            FOREIGN_LANGUAGE -> true

            else -> false
        }
    }

    companion object {
        @JvmStatic
        fun tryFromCode(code: String?): Optional<CourseArea> {
            return Optional.ofNullable(entries.firstOrNull { it.code.isNotBlank() && it.code == code })
        }

        @JvmStatic
        fun fromCode(code: String): CourseArea {
            return tryFromCode(code).orElseThrow { RestApiException(INVALID_ENUM_TYPE) }
        }

        fun from(courseArea: String): CourseArea {
            return try {
                valueOf(courseArea.uppercase())
            } catch (e: IllegalArgumentException) {
                throw RestApiException(INVALID_ENUM_TYPE)
            }
        }

        @JvmStatic
        fun fromGeneralEducation(courseArea: String): CourseArea {
            val area = from(courseArea)

            if (!area.isGeneralEducationArea()) {
                throw RestApiException(INVALID_GENERAL_EDUCATION_AREA)
            }

            return area
        }
    }
}
