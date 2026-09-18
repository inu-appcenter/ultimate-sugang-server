package uss.code.course.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uss.code.global.exception.domain.ExceptionCode.INVALID_ENUM_TYPE
import uss.code.global.exception.domain.ExceptionCode.INVALID_GENERAL_EDUCATION_AREA
import uss.code.global.exception.domain.RestApiException
import uss.code.global.infra.IntegrationTest

@IntegrationTest
class CourseAreaTest {
    @Nested
    inner class 코드값_변환_테스트 {
        @Test
        fun 연계_API의_영역_코드를_상수로_변환한다() {
            //given

            //when & then
            assertThat(CourseArea.fromCode("34")).isEqualTo(CourseArea.MAJOR_CORE)
            assertThat(CourseArea.fromCode("161")).isEqualTo(CourseArea.ACADEMIC_FOUNDATION)
            assertThat(CourseArea.fromCode("186")).isEqualTo(CourseArea.FOREIGN_LANGUAGE)
        }

        @Test
        fun 이름이_달라도_코드로_변환된다() {
            //given
            // API 이름은 '기초과학ㆍ공학'(U+318D), enum 이름은 '기초과학·공학'(U+00B7)으로 문자가 다르다
            val code = "162"

            //when
            val area = CourseArea.fromCode(code)

            //then
            assertThat(area).isEqualTo(CourseArea.BASIC_SCIENCE_ENGINEERING)
            assertThat(area.displayName).isNotEqualTo("기초과학ㆍ공학")
        }

        @Test
        fun 정의되지_않은_코드면_예외가_발생한다() {
            //given
            val unknownCode = "999"

            //when & then
            assertThatThrownBy { CourseArea.fromCode(unknownCode) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_ENUM_TYPE)
        }
    }

    @Nested
    inner class 교양_영역_판정_테스트 {
        @Test
        fun 교양_영역이면_그대로_반환한다() {
            //given
            val courseArea = "CORE_HUMANITIES"

            //when
            val area = CourseArea.fromGeneralEducation(courseArea)

            //then
            assertThat(area).isEqualTo(CourseArea.CORE_HUMANITIES)
        }

        @Test
        fun 교양_영역이_아니면_예외가_발생한다() {
            //given
            val courseArea = "MAJOR_CORE"

            //when & then
            assertThatThrownBy { CourseArea.fromGeneralEducation(courseArea) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_GENERAL_EDUCATION_AREA)
        }
    }
}
