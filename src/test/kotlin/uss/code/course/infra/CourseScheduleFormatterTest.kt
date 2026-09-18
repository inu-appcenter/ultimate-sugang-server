package uss.code.course.infra

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uss.code.course.domain.CourseDay
import uss.code.course.fixture.CourseFixture
import uss.code.course.fixture.CourseScheduleFixture
import uss.code.global.infra.IntegrationTest
import java.time.LocalTime

@IntegrationTest
class CourseScheduleFormatterTest {
    @Nested
    inner class 시간표_문자열_조립_테스트 {
        @Test
        fun 시간표가_없으면_빈_문자열을_반환한다() {
            //given
            val course = CourseFixture.createCourse()

            //when
            val schedule = CourseScheduleFormatter.format(course.schedules)

            //then
            assertThat(schedule).isEmpty()
        }

        @Test
        fun 요일이_다르면_묶음을_공백으로_잇는다() {
            //given
            val course = CourseFixture.createCourse()
            course.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    course, CourseDay.TUESDAY, "1-2A", "07-407",
                    LocalTime.of(9, 0), LocalTime.of(10, 15),
                )
            )
            course.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    course, CourseDay.THURSDAY, "2B-3", "07-407",
                    LocalTime.of(10, 30), LocalTime.of(11, 45),
                )
            )

            //when
            val schedule = CourseScheduleFormatter.format(course.schedules)

            //then
            assertThat(schedule).isEqualTo("화 1-2A (07-407) 목 2B-3 (07-407)")
        }

        @Test
        fun 강의실이_여러_개면_강의실별로_묶는다() {
            //given
            val course = CourseFixture.createCourse()
            course.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    course, CourseDay.MONDAY, "5B-6", "05-506",
                    LocalTime.of(13, 30), LocalTime.of(14, 45),
                )
            )
            course.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    course, CourseDay.WEDNESDAY, "5B-6", "05-507",
                    LocalTime.of(13, 30), LocalTime.of(14, 45),
                )
            )

            //when
            val schedule = CourseScheduleFormatter.format(course.schedules)

            //then
            assertThat(schedule).isEqualTo("월 5B-6 (05-506) 수 5B-6 (05-507)")
        }

        @Test
        fun 강의실이_섞여도_요일_순서대로_이어_붙인다() {
            //given
            val course = CourseFixture.createCourse()
            course.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    course, CourseDay.MONDAY, "1-2A", "15-113",
                    LocalTime.of(9, 0), LocalTime.of(10, 15),
                )
            )
            course.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    course, CourseDay.TUESDAY, "4-5A", "가상건물-200",
                    LocalTime.of(12, 0), LocalTime.of(13, 15),
                )
            )
            course.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    course, CourseDay.WEDNESDAY, "7-8A", "15-113",
                    LocalTime.of(15, 0), LocalTime.of(16, 15),
                )
            )

            //when
            val schedule = CourseScheduleFormatter.format(course.schedules)

            //then
            assertThat(schedule).isEqualTo("월 1-2A (15-113) 화 4-5A (가상건물-200) 수 7-8A (15-113)")
        }

        @Test
        fun 등록_순서와_무관하게_요일_순으로_정렬한다() {
            //given
            val course = CourseFixture.createCourse()
            course.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    course, CourseDay.FRIDAY, "3", "07-407",
                    LocalTime.of(11, 0), LocalTime.of(11, 50),
                )
            )
            course.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    course, CourseDay.MONDAY, "1", "07-407",
                    LocalTime.of(9, 0), LocalTime.of(9, 50),
                )
            )
            course.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    course, CourseDay.WEDNESDAY, "2", "07-407",
                    LocalTime.of(10, 0), LocalTime.of(10, 50),
                )
            )

            //when
            val schedule = CourseScheduleFormatter.format(course.schedules)

            //then
            assertThat(schedule).isEqualTo("월 1 (07-407) 수 2 (07-407) 금 3 (07-407)")
        }

        @Test
        fun 같은_요일_같은_강의실이면_교시를_한_묶음으로_잇는다() {
            //given
            val course = CourseFixture.createCourse()
            course.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    course, CourseDay.MONDAY, "6", "08-201",
                    LocalTime.of(14, 0), LocalTime.of(14, 50),
                )
            )
            course.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    course, CourseDay.MONDAY, "5", "08-201",
                    LocalTime.of(13, 0), LocalTime.of(13, 50),
                )
            )

            //when
            val schedule = CourseScheduleFormatter.format(course.schedules)

            //then
            assertThat(schedule).isEqualTo("월 5 6 (08-201)")
        }

        @Test
        fun 야간_교시_표기도_그대로_담는다() {
            //given
            val course = CourseFixture.createCourse()
            course.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    course, CourseDay.MONDAY, "야1-2A", "07-407",
                    LocalTime.of(18, 0), LocalTime.of(19, 15),
                )
            )

            //when
            val schedule = CourseScheduleFormatter.format(course.schedules)

            //then
            assertThat(schedule).isEqualTo("월 야1-2A (07-407)")
        }
    }

    @Nested
    inner class 입력_보존_테스트 {
        @Test
        fun 조립해도_원본_시간표_목록의_순서는_그대로다() {
            //given
            val course = CourseFixture.createCourse()
            course.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    course, CourseDay.FRIDAY, "3", "07-407",
                    LocalTime.of(11, 0), LocalTime.of(11, 50),
                )
            )
            course.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    course, CourseDay.MONDAY, "1", "07-407",
                    LocalTime.of(9, 0), LocalTime.of(9, 50),
                )
            )
            val schedules = course.schedules

            //when
            CourseScheduleFormatter.format(schedules)

            //then
            assertThat(schedules)
                .extracting<CourseDay> { it.dayOfWeek }
                .containsExactly(CourseDay.FRIDAY, CourseDay.MONDAY)
        }
    }
}
