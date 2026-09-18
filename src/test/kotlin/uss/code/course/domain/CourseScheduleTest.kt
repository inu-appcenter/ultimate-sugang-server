package uss.code.course.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uss.code.course.fixture.CourseFixture
import uss.code.course.fixture.CourseScheduleFixture

class CourseScheduleTest {
    @Nested
    inner class 수업_길이_판정_테스트 {
        @Test
        fun 교시_코드가_B로_시작하면_75분_수업이다() {
            //given
            val course = CourseFixture.createCourse()

            //when
            val morning = CourseScheduleFixture.createCourseScheduleWithPeriodCode(course, "B01")
            val night = CourseScheduleFixture.createCourseScheduleWithPeriodCode(course, "B10")

            //then
            assertThat(morning.is75MinLesson()).isTrue()
            assertThat(night.is75MinLesson()).isTrue()
        }

        @Test
        fun 교시_코드가_A나_C로_시작하면_75분_수업이_아니다() {
            //given
            val course = CourseFixture.createCourse()

            //when
            val day = CourseScheduleFixture.createCourseScheduleWithPeriodCode(course, "A03")
            val night = CourseScheduleFixture.createCourseScheduleWithPeriodCode(course, "C01")

            //then
            assertThat(day.is75MinLesson()).isFalse()
            assertThat(night.is75MinLesson()).isFalse()
        }
    }
}
