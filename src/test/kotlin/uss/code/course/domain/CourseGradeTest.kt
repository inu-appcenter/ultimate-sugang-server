package uss.code.course.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uss.code.global.exception.domain.ExceptionCode.INVALID_ENUM_TYPE
import uss.code.global.exception.domain.RestApiException
import uss.code.global.infra.IntegrationTest

@IntegrationTest
class CourseGradeTest {
    @Nested
    inner class 코드값_변환_테스트 {
        @Test
        fun 연계_API의_학년_코드를_상수로_변환한다() {
            //given

            //when & then
            assertThat(CourseGrade.fromCode("1")).isEqualTo(CourseGrade.FRESHMAN)
            assertThat(CourseGrade.fromCode("2")).isEqualTo(CourseGrade.SOPHOMORE)
            assertThat(CourseGrade.fromCode("3")).isEqualTo(CourseGrade.JUNIOR)
            assertThat(CourseGrade.fromCode("4")).isEqualTo(CourseGrade.SENIOR)
        }

        @Test
        fun 전학년_코드는_0이다() {
            //given
            // API가 주는 이름은 '전학년'이지만 1~4학년은 '1', '2'처럼 숫자만 와서 이름으로는 매칭할 수 없다
            val code = "0"

            //when
            val grade = CourseGrade.fromCode(code)

            //then
            assertThat(grade).isEqualTo(CourseGrade.ALL)
            assertThat(grade.year).isEqualTo(-1)
        }

        @Test
        fun 정의되지_않은_코드면_예외가_발생한다() {
            //given
            val unknownCode = "5"

            //when & then
            assertThatThrownBy { CourseGrade.fromCode(unknownCode) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_ENUM_TYPE)
        }
    }
}
