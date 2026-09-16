package uss.code.course.domain

import uss.code.global.exception.domain.ExceptionCode.INVALID_ENUM_TYPE
import uss.code.global.exception.domain.ExceptionCode.INVALID_GENERAL_EDUCATION_CLASSIFICATION
import uss.code.global.exception.domain.RestApiException

enum class CourseClassification(
    val code: String,
    @get:JvmName("getName")
    val displayName: String,
) {
    MAJOR_ADVANCED("41", "전공심화"),
    MAJOR_BASIC("25", "전공기초"),
    MAJOR_CORE("31", "전공핵심"),
    BASIC_LIBERAL_ARTS("11", "기초교양"),
    CORE_LIBERAL_ARTS("21", "핵심교양"),
    ADVANCED_LIBERAL_ARTS("23", "심화교양"),
    TEACHING("50", "교직"),
    GENERAL_ELECTIVE("80", "일반선택"),
    MILITARY("70", "군사학");

    fun isLiberalArtsScreen(): Boolean = this in LIBERAL_ARTS_SCREEN

    fun hasArea(area: CourseArea): Boolean = area in AREAS[this].orEmpty()

    companion object {
        private val LIBERAL_ARTS_SCREEN = setOf(
            BASIC_LIBERAL_ARTS,
            CORE_LIBERAL_ARTS,
            ADVANCED_LIBERAL_ARTS,
            TEACHING,
            GENERAL_ELECTIVE,
            MILITARY,
        )

        private val AREAS = mapOf(
            MAJOR_ADVANCED to setOf(CourseArea.MAJOR_ADVANCED),
            MAJOR_BASIC to setOf(CourseArea.MAJOR_BASIC),
            MAJOR_CORE to setOf(CourseArea.MAJOR_CORE),
            BASIC_LIBERAL_ARTS to setOf(CourseArea.ACADEMIC_FOUNDATION, CourseArea.BASIC_SCIENCE_ENGINEERING),
            CORE_LIBERAL_ARTS to setOf(
                CourseArea.CORE_INU_SEMINAR, CourseArea.CORE_HUMANITIES, CourseArea.CORE_SOCIAL,
                CourseArea.CORE_SCIENCE_TECHNOLOGY, CourseArea.CORE_ARTS_SPORTS, CourseArea.CORE_FOREIGN_LANGUAGE,
            ),
            ADVANCED_LIBERAL_ARTS to setOf(
                CourseArea.HUMANITIES, CourseArea.SOCIAL, CourseArea.SCIENCE_TECHNOLOGY,
                CourseArea.ARTS_SPORTS, CourseArea.FOREIGN_LANGUAGE,
            ),
            TEACHING to setOf(CourseArea.TEACHING),
            GENERAL_ELECTIVE to setOf(CourseArea.GENERAL_ELECTIVE),
            MILITARY to setOf(CourseArea.MILITARY),
        )

        @JvmStatic
        fun fromCode(code: String): CourseClassification =
            entries.firstOrNull { it.code.isNotBlank() && it.code == code }
                ?: throw RestApiException(INVALID_ENUM_TYPE)

        @JvmStatic
        fun fromLiberalArtsScreen(code: String): CourseClassification {
            val classification = fromCode(code)

            if (!classification.isLiberalArtsScreen()) {
                throw RestApiException(INVALID_GENERAL_EDUCATION_CLASSIFICATION)
            }

            return classification
        }
    }
}
