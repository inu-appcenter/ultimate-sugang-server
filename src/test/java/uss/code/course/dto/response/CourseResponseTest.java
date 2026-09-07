package uss.code.course.dto.response;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import uss.code.course.domain.Course;
import uss.code.course.domain.CourseArea;
import uss.code.course.domain.CourseClassification;
import uss.code.course.domain.CourseCollege;
import uss.code.course.domain.CourseDay;
import uss.code.course.domain.CourseDepartment;
import uss.code.course.domain.CourseGrade;
import uss.code.course.domain.CourseType;
import uss.code.course.fixture.CourseFixture;
import uss.code.course.fixture.CourseScheduleFixture;
import uss.code.course.repository.CourseRepository;
import uss.code.global.infra.IntegrationTest;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class CourseResponseTest {

    @Autowired
    private CourseRepository courseRepository;

    private Course createCourse() {
        return courseRepository.save(CourseFixture.createCourse());
    }

    private Course createCourseWithSchedule() {
        final Course course = CourseFixture.createCourse();
        course.addCourseSchedule(CourseScheduleFixture.createCourseSchedule(
                course, CourseDay.MONDAY, "1-2A", "07-407",
                LocalTime.of(9, 0), LocalTime.of(10, 15)
        ));
        return courseRepository.save(course);
    }

    @Nested
    class 강의_식별자_매핑_테스트 {

        @Test
        void 식별자는_문자열로_내려간다() {
            //given
            final Course course = createCourse();

            //when
            final CourseResponse response = CourseResponse.from(course);

            //then
            assertThat(response.id()).isEqualTo(String.valueOf(course.getId()));
        }

        @Test
        void 학수번호는_code_에_과목코드는_courseCode_에_담긴다() {
            //given
            final Course course = createCourse();

            //when
            final CourseResponse response = CourseResponse.from(course);

            //then
            assertThat(response.code()).isEqualTo("CSE2010001");
            assertThat(response.courseCode()).isEqualTo("CSE2010");
        }

        @Test
        void 국문명과_영문명이_각각_name_과_nameEn_에_담긴다() {
            //given
            final Course course = createCourse();

            //when
            final CourseResponse response = CourseResponse.from(course);

            //then
            assertThat(response.name()).isEqualTo("데이터구조");
            assertThat(response.nameEn()).isEqualTo("Data Structure");
        }
    }

    @Nested
    class 값이_없는_필드_테스트 {

        @Test
        void 교강사는_항상_빈_문자열이다() {
            //given
            final Course course = createCourse();

            //when
            final CourseResponse response = CourseResponse.from(course);

            //then
            assertThat(response.professor()).isEmpty();
        }

        @Test
        void 원어강의가_아니면_원어강의명은_빈_문자열이다() {
            //given
            final Course course = createCourse();

            //when
            final CourseResponse response = CourseResponse.from(course);

            //then
            assertThat(response.isEnglish()).isFalse();
            assertThat(response.englishCourseName()).isEmpty();
        }

        @Test
        void 원어강의면_원어강의명이_담긴다() {
            //given
            final Course course = courseRepository.save(CourseFixture.createCourse(
                    "데이터구조", "Data Structure", "CSE2010", "CSE2010002",
                    CourseCollege.INFORMATION_TECHNOLOGY, CourseDepartment.COMPUTER_ENGINEERING,
                    CourseClassification.MAJOR_CORE, CourseArea.MAJOR_CORE,
                    CourseType.LECTURE, CourseGrade.SOPHOMORE,
                    3, true, 50, 30
            ));

            //when
            final CourseResponse response = CourseResponse.from(course);

            //then
            assertThat(response.isEnglish()).isTrue();
            assertThat(response.englishCourseName()).isEqualTo("원어강의(EN)");
        }

        @Test
        void 시간표가_없으면_빈_문자열이다() {
            //given
            final Course course = createCourse();

            //when
            final CourseResponse response = CourseResponse.from(course);

            //then
            assertThat(response.schedule()).isEmpty();
        }

        @Test
        void 시간표가_있으면_요일_교시_강의실_표기로_담긴다() {
            //given
            final Course course = createCourseWithSchedule();

            //when
            final CourseResponse response = CourseResponse.from(course);

            //then
            assertThat(response.schedule()).isEqualTo("월 1-2A (07-407)");
        }
    }

    @Nested
    class 이수영역_매핑_테스트 {

        @Test
        void 교양_영역이면_이수영역_명칭이_담긴다() {
            //given
            final Course course = courseRepository.save(CourseFixture.createCourse(
                    "글쓰기", "Writing", "GEN101", "GEN101001",
                    CourseCollege.GENERAL_EDUCATION, CourseDepartment.GENERAL_EDUCATION,
                    CourseClassification.CORE_LIBERAL_ARTS, CourseArea.CORE_HUMANITIES,
                    CourseType.LECTURE, CourseGrade.ALL,
                    3, false, 50, 30
            ));

            //when
            final CourseResponse response = CourseResponse.from(course);

            //then
            assertThat(response.courseArea()).isEqualTo("(핵심)인문");
        }

        @Test
        void 교양_영역이_아니면_이수영역은_빈_문자열이다() {
            //given
            final Course course = createCourse();

            //when
            final CourseResponse response = CourseResponse.from(course);

            //then
            assertThat(response.courseType()).isEqualTo("전공핵심");
            assertThat(response.courseArea()).isEmpty();
        }
    }

    @Nested
    class 태그_조립_테스트 {

        @Test
        void 태그_대상이_아니면_빈_목록이다() {
            //given
            final Course course = createCourse();

            //when
            final CourseResponse response = CourseResponse.from(course);

            //then
            assertThat(response.tags()).isEmpty();
        }

        @Test
        void 교시_코드가_B로_시작하면_긴_수업_태그가_붙는다() {
            //given
            final Course course = createCourseWithSchedule();

            //when
            final CourseResponse response = CourseResponse.from(course);

            //then
            assertThat(response.tags()).containsExactly("75분수업");
        }

        @Test
        void 수업유형이_태그_대상이면_유형_명칭이_붙는다() {
            //given
            final Course course = CourseFixture.createCourse(
                    "온라인교양", "Online Course", "GEN901", "GEN901001",
                    CourseCollege.GENERAL_EDUCATION, CourseDepartment.GENERAL_EDUCATION,
                    CourseClassification.CORE_LIBERAL_ARTS, CourseArea.CORE_HUMANITIES,
                    CourseType.E_LEARNING, CourseGrade.ALL,
                    3, false, 50, 30
            );
            courseRepository.save(course);

            //when
            final CourseResponse response = CourseResponse.from(course);

            //then
            assertThat(response.tags()).containsExactly("e-Learning");
        }

        @Test
        void 두_태그가_함께_붙으면_75분수업이_앞에_온다() {
            //given
            final Course course = CourseFixture.createCourse(
                    "온라인교양", "Online Course", "GEN902", "GEN902001",
                    CourseCollege.GENERAL_EDUCATION, CourseDepartment.GENERAL_EDUCATION,
                    CourseClassification.CORE_LIBERAL_ARTS, CourseArea.CORE_HUMANITIES,
                    CourseType.ONLINE_BLENDED, CourseGrade.ALL,
                    3, false, 50, 30
            );
            course.addCourseSchedule(CourseScheduleFixture.createCourseSchedule(
                    course, CourseDay.MONDAY, "1-2A", "07-407",
                    LocalTime.of(9, 0), LocalTime.of(10, 15)
            ));
            courseRepository.save(course);

            //when
            final CourseResponse response = CourseResponse.from(course);

            //then
            assertThat(response.tags()).containsExactly("75분수업", "온라인혼합형강좌");
        }
    }

    @Nested
    class 야간학과_표기_테스트 {

        @Test
        void 학과명이_야로_끝나면_야간학과다() {
            //given
            final Course course = courseRepository.save(CourseFixture.createCourseWithDepartmentAndDetails(
                    "무역학원론", "Trade", "TRD101", "TRD101001",
                    CourseDepartment.TRADE_NIGHT, CourseGrade.FRESHMAN
            ));

            //when
            final CourseResponse response = CourseResponse.from(course);

            //then
            assertThat(response.department()).isEqualTo("무역학부(야)");
            assertThat(response.isNight()).isTrue();
        }

        @Test
        void 주간학과는_야간학과가_아니다() {
            //given
            final Course course = createCourse();

            //when
            final CourseResponse response = CourseResponse.from(course);

            //then
            assertThat(response.isNight()).isFalse();
        }
    }

    @Nested
    class 마감_여부_테스트 {

        @Test
        void 정원이_남아_있으면_마감이_아니다() {
            //given
            final Course course = createCourse();

            //when
            final CourseResponse response = CourseResponse.from(course);

            //then
            assertThat(response.capacity()).isEqualTo(50);
            assertThat(response.enrolled()).isEqualTo(30);
            assertThat(response.isClosed()).isFalse();
        }

        @Test
        void 현재_수강인원이_정원에_도달하면_마감이다() {
            //given
            final Course course = CourseFixture.createCourse();
            ReflectionTestUtils.setField(course, "currentEnrollment", 50);
            courseRepository.save(course);

            //when
            final CourseResponse response = CourseResponse.from(course);

            //then
            assertThat(response.isClosed()).isTrue();
        }

        @Test
        void 폐강된_강의는_정원이_남아도_마감이다() {
            //given
            final Course course = CourseFixture.createCourse();
            course.close();
            courseRepository.save(course);

            //when
            final CourseResponse response = CourseResponse.from(course);

            //then
            assertThat(response.isClosed()).isTrue();
        }
    }
}
