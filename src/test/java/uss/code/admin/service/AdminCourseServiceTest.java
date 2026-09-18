package uss.code.admin.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uss.code.admin.dto.response.CourseSummaryResponse;
import uss.code.course.domain.Course;
import uss.code.course.fixture.CourseFixture;
import uss.code.course.fixture.CourseScheduleFixture;
import uss.code.course.repository.CourseRepository;
import uss.code.global.infra.IntegrationTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uss.code.course.domain.CourseGrade.SOPHOMORE;
import static uss.code.course.domain.CourseTerm.SECOND;

@IntegrationTest
class AdminCourseServiceTest {

    private static final int TEST_ACADEMIC_YEAR = 2026;

    @Autowired
    private AdminCourseService adminCourseService;

    @Autowired
    private CourseRepository courseRepository;

    @Nested
    class 적재_현황_조회_테스트 {

        @Test
        void 강의가_없으면_적재_학기가_비어있다() {
            //when
            final CourseSummaryResponse response = adminCourseService.getSummary();

            //then
            assertThat(response.semester()).isNull();
            assertThat(response.courseCount()).isZero();
            assertThat(response.scheduleCount()).isZero();
        }

        @Test
        void 적재된_학기와_건수를_반환한다() {
            //given
            final Course course = CourseFixture.createCourse();
            course.addCourseSchedule(CourseScheduleFixture.createCourseSchedule(course));
            courseRepository.save(course);

            //when
            final CourseSummaryResponse response = adminCourseService.getSummary();

            //then
            assertThat(response.semester().academicYear()).isEqualTo(TEST_ACADEMIC_YEAR);
            assertThat(response.semester().term()).isEqualTo(SECOND);
            assertThat(response.courseCount()).isEqualTo(1);
            assertThat(response.scheduleCount()).isEqualTo(1);
        }

        @Test
        void 강의_수는_폐강을_포함한다() {
            //given
            final Course active = CourseFixture.createCourseWithDetails(
                    "데이터구조", "Data Structure", "CSE2010", "CSE2010001", SOPHOMORE
            );
            final Course closed = CourseFixture.createCourseWithDetails(
                    "폐강과목", "Closed Course", "CSE2020", "CSE2020001", SOPHOMORE
            );
            closed.close();
            courseRepository.saveAll(List.of(active, closed));

            //when
            final CourseSummaryResponse response = adminCourseService.getSummary();

            //then
            assertThat(response.courseCount()).isEqualTo(2);
        }
    }
}
