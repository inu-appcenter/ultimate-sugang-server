package uss.code.course.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uss.code.global.exception.domain.ExceptionCode.INVALID_ENUM_TYPE
import uss.code.global.exception.domain.RestApiException
import uss.code.global.infra.IntegrationTest

@IntegrationTest
class CourseCollegeTest {
    @Nested
    inner class 코드값_변환_테스트 {
        @Test
        fun 문자로_시작하는_코드를_상수로_변환한다() {
            //given

            //when & then
            assertThat(CourseCollege.fromCode("A000")).isEqualTo(CourseCollege.HUMANITIES)
            assertThat(CourseCollege.fromCode("E000")).isEqualTo(CourseCollege.ENGINEERING)
            assertThat(CourseCollege.fromCode("I000")).isEqualTo(CourseCollege.INFORMATION_TECHNOLOGY)
        }

        @Test
        fun 숫자로_시작하는_코드도_상수로_변환한다() {
            //given
            // 연계 API의 단과대 코드는 'A000' 형태와 '0000033' 형태가 섞여 있다
            //when & then
            assertThat(CourseCollege.fromCode("0000033")).isEqualTo(CourseCollege.URBAN_SCIENCE)
            assertThat(CourseCollege.fromCode("0000689")).isEqualTo(CourseCollege.COMMERCE_PUBLIC_AFFAIRS)
        }

        @Test
        fun 단과대구분없음과_법학은_다른_상수다() {
            //given

            //when & then
            assertThat(CourseCollege.fromCode("0000465")).isEqualTo(CourseCollege.NONE)
            assertThat(CourseCollege.fromCode("0000706")).isEqualTo(CourseCollege.LAW)
        }

        @Test
        fun 정의되지_않은_코드면_예외가_발생한다() {
            //given
            val unknownCode = "Q000"

            //when & then
            assertThatThrownBy { CourseCollege.fromCode(unknownCode) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_ENUM_TYPE)
        }
    }
}
