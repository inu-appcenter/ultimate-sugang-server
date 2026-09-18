package uss.code.admin.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uss.code.course.domain.CourseGrade.SOPHOMORE
import uss.code.course.domain.CourseTerm.SECOND
import uss.code.course.fixture.CourseFixture
import uss.code.course.fixture.CourseScheduleFixture
import uss.code.course.repository.CourseRepository
import uss.code.global.infra.IntegrationTest

@IntegrationTest
class AdminCourseServiceTest(
    private val adminCourseService: AdminCourseService,

    private val courseRepository: CourseRepository,
) {
    @Nested
    inner class 적재_현황_조회_테스트 {
        @Test
        fun 강의가_없으면_적재_학기가_비어있다() {
            //when
            val response = adminCourseService.getSummary()

            //then
            assertThat(response.semester).isNull()
            assertThat(response.courseCount).isZero()
            assertThat(response.scheduleCount).isZero()
        }

        @Test
        fun 적재된_학기와_건수를_반환한다() {
            //given
            val course = CourseFixture.createCourse()
            course.addCourseSchedule(CourseScheduleFixture.createCourseSchedule(course))
            courseRepository.save(course)

            //when
            val response = adminCourseService.getSummary()

            //then
            assertThat(response.semester?.academicYear).isEqualTo(TEST_ACADEMIC_YEAR)
            assertThat(response.semester?.term).isEqualTo(SECOND)
            assertThat(response.courseCount).isEqualTo(1L)
            assertThat(response.scheduleCount).isEqualTo(1L)
        }

        @Test
        fun 강의_수는_폐강을_포함한다() {
            //given
            val active = CourseFixture.createCourseWithDetails(
                "데이터구조", "Data Structure", "CSE2010", "CSE2010001", SOPHOMORE,
            )
            val closed = CourseFixture.createCourseWithDetails(
                "폐강과목", "Closed Course", "CSE2020", "CSE2020001", SOPHOMORE,
            )
            closed.close()
            courseRepository.saveAll(listOf(active, closed))

            //when
            val response = adminCourseService.getSummary()

            //then
            assertThat(response.courseCount).isEqualTo(2L)
        }
    }

    companion object {
        private const val TEST_ACADEMIC_YEAR = 2026
    }
}
