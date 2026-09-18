package uss.code.course.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uss.code.global.exception.domain.ExceptionCode.INVALID_ENUM_TYPE
import uss.code.global.exception.domain.RestApiException
import uss.code.global.infra.IntegrationTest

@IntegrationTest
class CourseTermTest {
    @Nested
    inner class 코드값_변환_테스트 {
        @Test
        fun 연계_API의_학기_코드를_상수로_변환한다() {
            //given

            //when & then
            assertThat(CourseTerm.fromCode("10")).isEqualTo(CourseTerm.FIRST)
            assertThat(CourseTerm.fromCode("20")).isEqualTo(CourseTerm.SECOND)
            assertThat(CourseTerm.fromCode("30")).isEqualTo(CourseTerm.SUMMER)
            assertThat(CourseTerm.fromCode("40")).isEqualTo(CourseTerm.WINTER)
        }

        @Test
        fun 정의되지_않은_코드면_예외가_발생한다() {
            //given
            val unknownCode = "50"

            //when & then
            assertThatThrownBy { CourseTerm.fromCode(unknownCode) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_ENUM_TYPE)
        }
    }
}
