package uss.code.course.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uss.code.course.domain.Course;
import uss.code.course.domain.CourseArea;
import uss.code.course.domain.CourseClassification;
import uss.code.course.domain.CourseCollege;
import uss.code.course.domain.CourseDay;
import uss.code.course.domain.CourseDepartment;
import uss.code.course.domain.CourseGrade;
import uss.code.course.domain.CourseSchedule;
import uss.code.course.domain.CourseType;
import uss.code.course.dto.response.CourseAreaResponse;
import uss.code.course.dto.response.CourseCategoriesResponse;
import uss.code.course.dto.response.CourseCategoryResponse;
import uss.code.course.dto.response.CourseTermResponse;
import uss.code.course.dto.response.CourseTermsResponse;
import uss.code.course.dto.response.DepartmentResponse;
import uss.code.course.dto.response.DepartmentsResponse;
import uss.code.course.dto.response.InterdisciplinaryMajorResponse;
import uss.code.course.dto.response.InterdisciplinaryMajorsResponse;
import uss.code.course.fixture.CourseFixture;
import uss.code.course.fixture.CourseScheduleFixture;
import uss.code.course.repository.CourseRepository;
import uss.code.course.dto.response.CourseResponse;
import uss.code.course.dto.response.CoursesResponse;
import uss.code.global.exception.domain.RestApiException;
import uss.code.global.infra.IntegrationTest;
import uss.code.member.domain.AcademicStatus;
import uss.code.member.domain.Member;
import uss.code.member.domain.MemberCollege;
import uss.code.member.domain.MemberDepartment;
import uss.code.member.domain.MemberGrade;
import uss.code.member.fixture.MemberFixture;
import uss.code.member.repository.MemberRepository;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static uss.code.global.exception.domain.ExceptionCode.*;

@IntegrationTest
class CourseServiceTest {

    @Autowired
    private CourseService courseService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CourseRepository courseRepository;

    @Nested
    class 전공_과목_조회_테스트 {

        private static final String TEST_STUDENT_ID = "202012345";
        private static final String TEST_NAME = "홍길동";
        private static final MemberCollege TEST_COLLEGE = MemberCollege.INFORMATION_TECHNOLOGY;
        private static final MemberDepartment TEST_DEPARTMENT = MemberDepartment.COMPUTER_ENGINEERING;
        private static final MemberGrade TEST_GRADE = MemberGrade.JUNIOR;
        private static final AcademicStatus TEST_ACADEMIC_STATUS = AcademicStatus.ENROLLED;
        private static final double TEST_GPA = 3.5;

        private long validMemberId;
        private final long invalidMemberId = 999L;

        @BeforeEach
        void setUp() {
            // 회원 생성 (컴퓨터공학부)
            final Member member = MemberFixture.createMember(
                    TEST_STUDENT_ID,
                    TEST_NAME,
                    TEST_COLLEGE,
                    TEST_DEPARTMENT,
                    TEST_GRADE,
                    TEST_ACADEMIC_STATUS,
                    TEST_GPA
            );
            memberRepository.save(member);
            validMemberId = member.getId();

            // 전학년 과목 2개
            Course allGrade1 = CourseFixture.createCourseWithDetails(
                    "자료구조", "Data Structure", "COM001", "COM001001",
                    CourseGrade.ALL
            );
            Course allGrade2 = CourseFixture.createCourseWithDetails(
                    "알고리즘", "Algorithm", "COM002", "COM002001",
                    CourseGrade.ALL
            );

            // 1학년 과목 2개
            Course freshman1 = CourseFixture.createCourseWithDetails(
                    "프로그래밍기초", "Programming Basics", "COM101", "COM101001",
                    CourseGrade.FRESHMAN
            );
            Course freshman2 = CourseFixture.createCourseWithDetails(
                    "컴퓨터개론", "Introduction to Computer", "COM102", "COM102001",
                    CourseGrade.FRESHMAN
            );

            // 2학년 과목 2개
            Course sophomore1 = CourseFixture.createCourseWithDetails(
                    "객체지향프로그래밍", "OOP", "COM201", "COM201001",
                    CourseGrade.SOPHOMORE
            );
            Course sophomore2 = CourseFixture.createCourseWithDetails(
                    "데이터베이스", "Database", "COM202", "COM202001",
                    CourseGrade.SOPHOMORE
            );

            // 컴공이 아닌 다른 학과 과목
            Course otherDept = CourseFixture.createCourseWithDepartmentAndDetails(
                    "미적분학", "Calculus", "MATH101", "MATH101001",
                    CourseDepartment.MATHEMATICS,
                    CourseGrade.FRESHMAN
            );

            // 스케줄 추가 (저장 전에 추가)
            CourseSchedule schedule1 = CourseScheduleFixture.createCourseSchedule(
                    allGrade1, CourseDay.MONDAY, LocalTime.of(13, 0), LocalTime.of(15, 0)
            );
            CourseSchedule schedule2 = CourseScheduleFixture.createCourseSchedule(
                    allGrade1, CourseDay.WEDNESDAY, LocalTime.of(13, 0), LocalTime.of(15, 0)
            );
            CourseSchedule schedule3 = CourseScheduleFixture.createCourseSchedule(
                    freshman1, CourseDay.TUESDAY, LocalTime.of(9, 0), LocalTime.of(11, 0)
            );

            allGrade1.addCourseSchedule(schedule1);
            allGrade1.addCourseSchedule(schedule2);
            freshman1.addCourseSchedule(schedule3);

            courseRepository.saveAll(List.of(
                    allGrade1, allGrade2,
                    freshman1, freshman2,
                    sophomore1, sophomore2,
                    otherDept
            ));
        }

        @Test
        void 컴퓨터공학부_학생이_전공과목을_조회하면_성공한다() {
            //given

            //when
            final CoursesResponse response = courseService.getMajorCourses(validMemberId);

            //then
            assertThat(response.courseResponses()).hasSize(6);
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::department)
                    .containsOnly("컴퓨터공학부");
        }

        @Test
        void 전학년_1학년_2학년_순서로_정렬되어_조회된다() {
            //given

            //when
            final CoursesResponse response = courseService.getMajorCourses(validMemberId);
            final List<String> grades = response.courseResponses().stream()
                    .map(CourseResponse::grade)
                    .toList();

            //then
            assertThat(grades).containsExactly(
                    "전학년", "전학년",  // COM001, COM002
                    "1학년", "1학년",    // COM101, COM102
                    "2학년", "2학년"     // COM201, COM202
            );
        }

        @Test
        void 스케줄이_있는_과목은_요일순으로_정렬되어_반환된다() {
            //given

            //when
            final CoursesResponse response = courseService.getMajorCourses(validMemberId);

            //then
            // COM001: [07-401:월(1-2A),수(1-2A)]
            final CourseResponse allGrade1 = response.courseResponses().stream()
                    .filter(c -> c.courseCode().equals("COM001"))
                    .findFirst()
                    .orElseThrow();
            assertThat(allGrade1.schedule()).isEqualTo("월 1-2A (07-401) 수 1-2A (07-401)");

            // COM101: [07-401:화(1-2A)]
            final CourseResponse freshman1 = response.courseResponses().stream()
                    .filter(c -> c.courseCode().equals("COM101"))
                    .findFirst()
                    .orElseThrow();
            assertThat(freshman1.schedule()).isEqualTo("화 1-2A (07-401)");
        }

        @Test
        void 다른_학과_과목은_조회되지_않는다() {
            //given

            //when
            final CoursesResponse response = courseService.getMajorCourses(validMemberId);

            //then
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::courseCode)
                    .doesNotContain("MATH101");
        }

        @Test
        void 존재하지_않는_회원_아이디로_조회하면_예외가_발생한다() {
            //given

            //when & then
            assertThatThrownBy(() -> courseService.getMajorCourses(invalidMemberId))
                    .isInstanceOf(RestApiException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", MEMBER_NOT_FOUND);
        }
    }

    @Nested
    class 학부_소속_전공_과목_조회_테스트 {

        private long electronicsMemberId;
        private long liberalArtsMemberId;

        @BeforeEach
        void setUp() {
            final Member electronicsMember = MemberFixture.createMember(
                    "202112345",
                    "김전자",
                    MemberCollege.ENGINEERING,
                    MemberDepartment.ELECTRONICS_ENGINEERING_SCHOOL,
                    MemberGrade.SOPHOMORE,
                    AcademicStatus.ENROLLED,
                    3.5
            );
            memberRepository.save(electronicsMember);
            electronicsMemberId = electronicsMember.getId();

            final Member liberalArtsMember = MemberFixture.createMember(
                    "202212345",
                    "박자유",
                    MemberCollege.LIBERAL_ARTS_COLLEGE,
                    MemberDepartment.INTERNATIONAL_LIBERAL_ARTS,
                    MemberGrade.FRESHMAN,
                    AcademicStatus.ENROLLED,
                    3.0
            );
            memberRepository.save(liberalArtsMember);
            liberalArtsMemberId = liberalArtsMember.getId();

            // 학부로 개설된 과목
            Course schoolCourse = CourseFixture.createCourseWithDepartmentAndDetails(
                    "전자회로", "Electronic Circuits", "ELE101", "ELE101001",
                    CourseDepartment.ELECTRONICS_ENGINEERING_SCHOOL,
                    CourseGrade.FRESHMAN
            );

            // 하위 전공으로 개설된 과목
            Course majorCourse = CourseFixture.createCourseWithDepartmentAndDetails(
                    "디지털신호처리", "Digital Signal Processing", "ELE201", "ELE201001",
                    CourseDepartment.ELECTRONICS_ENGINEERING_MAJOR,
                    CourseGrade.SOPHOMORE
            );
            Course semiconductorCourse = CourseFixture.createCourseWithDepartmentAndDetails(
                    "반도체소자", "Semiconductor Devices", "ELE202", "ELE202001",
                    CourseDepartment.SEMICONDUCTOR_CONVERGENCE_MAJOR,
                    CourseGrade.SOPHOMORE
            );

            // 폐지된 학과로 개설된 구 학번 대상 과목
            Course abolishedCourse = CourseFixture.createCourseWithDepartmentAndDetails(
                    "전자공학세미나", "Electronics Seminar", "ELE401", "ELE401001",
                    CourseDepartment.ELECTRONICS_ENGINEERING,
                    CourseGrade.SENIOR
            );

            // 다른 학부 과목
            Course otherSchoolCourse = CourseFixture.createCourseWithDepartmentAndDetails(
                    "생명과학개론", "Introduction to Life Science", "LIF101", "LIF101001",
                    CourseDepartment.LIFE_SCIENCE_SCHOOL,
                    CourseGrade.FRESHMAN
            );

            courseRepository.saveAll(List.of(
                    schoolCourse, majorCourse, semiconductorCourse, abolishedCourse, otherSchoolCourse
            ));
        }

        @Test
        void 학부_소속_학생은_하위_전공_과목까지_함께_조회한다() {
            //given

            //when
            final CoursesResponse response = courseService.getMajorCourses(electronicsMemberId);

            //then
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::courseCode)
                    .containsExactlyInAnyOrder("ELE101", "ELE201", "ELE202", "ELE401");
        }

        @Test
        void 폐지된_학과의_과목도_후신_학부_학생에게_조회된다() {
            //given

            //when
            final CoursesResponse response = courseService.getMajorCourses(electronicsMemberId);

            //then
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::department)
                    .contains("전자공학과");
        }

        @Test
        void 다른_학부의_과목은_조회되지_않는다() {
            //given

            //when
            final CoursesResponse response = courseService.getMajorCourses(electronicsMemberId);

            //then
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::courseCode)
                    .doesNotContain("LIF101");
        }

        @Test
        void 대응_강의_학과가_없는_소속의_회원은_예외없이_빈_목록을_받는다() {
            //given

            //when
            final CoursesResponse response = courseService.getMajorCourses(liberalArtsMemberId);

            //then
            assertThat(response.courseResponses()).isEmpty();
        }

        @Test
        void 타학과로_학부를_조회해도_하위_전공_과목까지_함께_조회된다() {
            //given
            final String department = "ELECTRONICS_ENGINEERING_SCHOOL";

            //when
            final CoursesResponse response = courseService.getOtherDepartmentCourses(department);

            //then
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::courseCode)
                    .containsExactlyInAnyOrder("ELE101", "ELE201", "ELE202", "ELE401");
        }

        @Test
        void 학부_소속_조회_결과도_학년_순으로_정렬된다() {
            //given

            //when
            final CoursesResponse response = courseService.getMajorCourses(electronicsMemberId);
            final List<String> grades = response.courseResponses().stream()
                    .map(CourseResponse::grade)
                    .toList();

            //then
            assertThat(grades).containsExactly("1학년", "2학년", "2학년", "4학년");
        }
    }

    @Nested
    class 교양_과목_조회_테스트 {

        @BeforeEach
        void setUp() {
            // 핵심 인문 과목 2개
            Course coreHumanities1 = CourseFixture.createCourse(
                    "글쓰기", "Writing", "GEN101", "GEN101001",
                    CourseCollege.GENERAL_EDUCATION, CourseDepartment.GENERAL_EDUCATION,
                    CourseClassification.CORE_LIBERAL_ARTS, CourseArea.CORE_HUMANITIES,
                    CourseType.LECTURE,
                    CourseGrade.ALL,
                    3, false, 50, 30
            );
            Course coreHumanities2 = CourseFixture.createCourse(
                    "철학의이해", "Understanding Philosophy", "GEN102", "GEN102001",
                    CourseCollege.GENERAL_EDUCATION, CourseDepartment.GENERAL_EDUCATION,
                    CourseClassification.CORE_LIBERAL_ARTS, CourseArea.CORE_HUMANITIES,
                    CourseType.LECTURE,
                    CourseGrade.ALL,
                    2, false, 40, 20
            );

            // 핵심 외국어 과목 2개
            Course coreForeignLanguage1 = CourseFixture.createCourse(
                    "영어회화", "English Conversation", "GEN201", "GEN201001",
                    CourseCollege.GENERAL_EDUCATION, CourseDepartment.GENERAL_EDUCATION,
                    CourseClassification.CORE_LIBERAL_ARTS, CourseArea.CORE_FOREIGN_LANGUAGE,
                    CourseType.LECTURE,
                    CourseGrade.ALL,
                    3, true, 45, 25
            );
            Course coreForeignLanguage2 = CourseFixture.createCourse(
                    "중국어회화", "Chinese Conversation", "GEN202", "GEN202001",
                    CourseCollege.GENERAL_EDUCATION, CourseDepartment.GENERAL_EDUCATION,
                    CourseClassification.CORE_LIBERAL_ARTS, CourseArea.CORE_FOREIGN_LANGUAGE,
                    CourseType.LECTURE,
                    CourseGrade.ALL,
                    3, false, 40, 15
            );

            // 일반 사회 과목 1개
            Course social = CourseFixture.createCourse(
                    "현대사회와윤리", "Modern Society and Ethics", "GEN301", "GEN301001",
                    CourseCollege.GENERAL_EDUCATION, CourseDepartment.GENERAL_EDUCATION,
                    CourseClassification.ADVANCED_LIBERAL_ARTS, CourseArea.SOCIAL,
                    CourseType.LECTURE,
                    CourseGrade.ALL,
                    3, false, 35, 20
            );

            // 전공 과목 (교양이 아님)
            Course majorCourse = CourseFixture.createCourse(
                    "데이터구조", "Data Structure", "COM101", "COM101001",
                    CourseCollege.INFORMATION_TECHNOLOGY, CourseDepartment.COMPUTER_ENGINEERING,
                    CourseClassification.MAJOR_CORE, CourseArea.MAJOR_CORE,
                    CourseType.LECTURE,
                    CourseGrade.SOPHOMORE,
                    3, false, 50, 40
            );

            // 스케줄 추가
            CourseSchedule schedule1 = CourseScheduleFixture.createCourseSchedule(
                    coreHumanities1, CourseDay.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0)
            );
            CourseSchedule schedule2 = CourseScheduleFixture.createCourseSchedule(
                    coreHumanities1, CourseDay.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(11, 0)
            );
            CourseSchedule schedule3 = CourseScheduleFixture.createCourseSchedule(
                    coreForeignLanguage1, CourseDay.TUESDAY, LocalTime.of(13, 0), LocalTime.of(15, 0)
            );

            coreHumanities1.addCourseSchedule(schedule1);
            coreHumanities1.addCourseSchedule(schedule2);
            coreForeignLanguage1.addCourseSchedule(schedule3);

            // 교직 과목 1개
            Course teaching = CourseFixture.createCourse(
                    "교육학개론", "Introduction to Education", "TEA101", "TEA101001",
                    CourseCollege.TEACHING, CourseDepartment.TEACHING,
                    CourseClassification.TEACHING, CourseArea.TEACHING,
                    CourseType.LECTURE,
                    CourseGrade.ALL,
                    2, false, 30, 10
            );

            // 일반선택 과목 1개
            Course generalElective = CourseFixture.createCourse(
                    "자기설계세미나", "Self Designed Seminar", "GEL101", "GEL101001",
                    CourseCollege.GENERAL_ELECTIVE, CourseDepartment.GENERAL_ELECTIVE,
                    CourseClassification.GENERAL_ELECTIVE, CourseArea.GENERAL_ELECTIVE,
                    CourseType.LECTURE,
                    CourseGrade.ALL,
                    1, false, 30, 10
            );

            courseRepository.saveAll(List.of(
                    coreHumanities1, coreHumanities2,
                    coreForeignLanguage1, coreForeignLanguage2,
                    social,
                    teaching, generalElective,
                    majorCourse
            ));
        }

        @Test
        void 이수영역까지_넘기면_해당_영역의_과목만_반환된다() {
            //given
            final String classificationCode = CourseClassification.CORE_LIBERAL_ARTS.getCode();
            final String areaCode = CourseArea.CORE_HUMANITIES.getCode();

            //when
            final CoursesResponse response = courseService.getGeneralEducationCourses(classificationCode, areaCode);

            //then
            assertThat(response.courseResponses()).hasSize(2);
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::courseArea)
                    .containsOnly("(핵심)인문");
        }

        @Test
        void 이수구분만_넘기면_하위_이수영역_전체가_반환된다() {
            //given
            final String classificationCode = CourseClassification.CORE_LIBERAL_ARTS.getCode();

            //when
            final CoursesResponse response = courseService.getGeneralEducationCourses(classificationCode, null);

            //then
            assertThat(response.courseResponses()).hasSize(4);
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::courseArea)
                    .containsOnly("(핵심)인문", "(핵심)외국어");
        }

        @Test
        void 심화교양_사회_영역으로_조회하면_해당_영역의_과목만_반환된다() {
            //given
            final String classificationCode = CourseClassification.ADVANCED_LIBERAL_ARTS.getCode();
            final String areaCode = CourseArea.SOCIAL.getCode();

            //when
            final CoursesResponse response = courseService.getGeneralEducationCourses(classificationCode, areaCode);

            //then
            assertThat(response.courseResponses()).hasSize(1);
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::courseArea)
                    .containsOnly("사회");
        }

        @Test
        void 교직으로_조회하면_교직_과목이_반환된다() {
            //given
            final String classificationCode = CourseClassification.TEACHING.getCode();

            //when
            final CoursesResponse response = courseService.getGeneralEducationCourses(classificationCode, null);

            //then
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::courseCode)
                    .containsExactly("TEA101");
        }

        @Test
        void 일반선택으로_조회하면_일반선택_과목이_반환된다() {
            //given
            final String classificationCode = CourseClassification.GENERAL_ELECTIVE.getCode();

            //when
            final CoursesResponse response = courseService.getGeneralEducationCourses(classificationCode, null);

            //then
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::courseCode)
                    .containsExactly("GEL101");
        }

        @Test
        void 교양이_아닌_과목의_이수영역은_빈_문자열로_반환된다() {
            //given
            final String classificationCode = CourseClassification.TEACHING.getCode();

            //when
            final CoursesResponse response = courseService.getGeneralEducationCourses(classificationCode, null);

            //then
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::courseArea)
                    .containsOnly("");
        }

        @Test
        void 스케줄이_있는_교양_과목은_요일순으로_정렬되어_반환된다() {
            //given
            final String classificationCode = CourseClassification.CORE_LIBERAL_ARTS.getCode();
            final String areaCode = CourseArea.CORE_HUMANITIES.getCode();

            //when
            final CoursesResponse response = courseService.getGeneralEducationCourses(classificationCode, areaCode);

            //then
            final CourseResponse coreHumanities1 = response.courseResponses().stream()
                    .filter(c -> c.courseCode().equals("GEN101"))
                    .findFirst()
                    .orElseThrow();
            assertThat(coreHumanities1.schedule()).isEqualTo("월 1-2A (07-401) 수 1-2A (07-401)");
        }

        @Test
        void 스케줄이_없는_교양_과목은_빈_문자열로_반환된다() {
            //given
            final String classificationCode = CourseClassification.CORE_LIBERAL_ARTS.getCode();
            final String areaCode = CourseArea.CORE_HUMANITIES.getCode();

            //when
            final CoursesResponse response = courseService.getGeneralEducationCourses(classificationCode, areaCode);

            //then
            final CourseResponse coreHumanities2 = response.courseResponses().stream()
                    .filter(c -> c.courseCode().equals("GEN102"))
                    .findFirst()
                    .orElseThrow();
            assertThat(coreHumanities2.schedule()).isEmpty();
        }

        @Test
        void 전공_과목은_교양_조회시_포함되지_않는다() {
            //given
            final String classificationCode = CourseClassification.CORE_LIBERAL_ARTS.getCode();

            //when
            final CoursesResponse response = courseService.getGeneralEducationCourses(classificationCode, null);

            //then
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::courseCode)
                    .doesNotContain("COM101");
        }

        @Test
        void 전공_이수구분으로_조회하면_예외가_발생한다() {
            //given
            final String majorClassificationCode = CourseClassification.MAJOR_CORE.getCode();

            //when & then
            assertThatThrownBy(() -> courseService.getGeneralEducationCourses(majorClassificationCode, null))
                    .isInstanceOf(RestApiException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", INVALID_GENERAL_EDUCATION_CLASSIFICATION);
        }

        @Test
        void 존재하지_않는_이수구분_코드로_조회하면_예외가_발생한다() {
            //given
            final String invalidClassificationCode = "99";

            //when & then
            assertThatThrownBy(() -> courseService.getGeneralEducationCourses(invalidClassificationCode, null))
                    .isInstanceOf(RestApiException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", INVALID_ENUM_TYPE);
        }

        @Test
        void 이수구분에_속하지_않는_이수영역으로_조회하면_예외가_발생한다() {
            //given
            final String classificationCode = CourseClassification.CORE_LIBERAL_ARTS.getCode();
            final String otherAreaCode = CourseArea.SOCIAL.getCode();

            //when & then
            assertThatThrownBy(() -> courseService.getGeneralEducationCourses(classificationCode, otherAreaCode))
                    .isInstanceOf(RestApiException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", INVALID_GENERAL_EDUCATION_AREA);
        }

        @Test
        void 존재하지_않는_이수영역_코드로_조회하면_예외가_발생한다() {
            //given
            final String classificationCode = CourseClassification.CORE_LIBERAL_ARTS.getCode();
            final String invalidAreaCode = "999";

            //when & then
            assertThatThrownBy(() -> courseService.getGeneralEducationCourses(classificationCode, invalidAreaCode))
                    .isInstanceOf(RestApiException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", INVALID_GENERAL_EDUCATION_AREA);
        }
    }

    @Nested
    class 타학과_전공과목_조회_테스트 {

        @BeforeEach
        void setUp() {
            // 수학과 과목들
            // 전학년 과목 2개
            Course mathAllGrade1 = CourseFixture.createCourseWithDepartmentAndDetails(
                    "미적분학", "Calculus", "MATH101", "MATH101001",
                    CourseDepartment.MATHEMATICS,
                    CourseGrade.ALL
            );
            Course mathAllGrade2 = CourseFixture.createCourseWithDepartmentAndDetails(
                    "선형대수", "Linear Algebra", "MATH102", "MATH102001",
                    CourseDepartment.MATHEMATICS,
                    CourseGrade.ALL
            );

            // 1학년 과목 2개
            Course mathFreshman1 = CourseFixture.createCourseWithDepartmentAndDetails(
                    "수학의이해", "Understanding Mathematics", "MATH201", "MATH201001",
                    CourseDepartment.MATHEMATICS,
                    CourseGrade.FRESHMAN
            );
            Course mathFreshman2 = CourseFixture.createCourseWithDepartmentAndDetails(
                    "기초수학", "Basic Mathematics", "MATH202", "MATH202001",
                    CourseDepartment.MATHEMATICS,
                    CourseGrade.FRESHMAN
            );

            // 2학년 과목 2개
            Course mathSophomore1 = CourseFixture.createCourseWithDepartmentAndDetails(
                    "해석학", "Analysis", "MATH301", "MATH301001",
                    CourseDepartment.MATHEMATICS,
                    CourseGrade.SOPHOMORE
            );
            Course mathSophomore2 = CourseFixture.createCourseWithDepartmentAndDetails(
                    "정수론", "Number Theory", "MATH302", "MATH302001",
                    CourseDepartment.MATHEMATICS,
                    CourseGrade.SOPHOMORE
            );

            // 컴퓨터공학부 과목 (다른 학과)
            Course cseCourse = CourseFixture.createCourseWithDetails(
                    "자료구조", "Data Structure", "CSE101", "CSE101001",
                    CourseGrade.SOPHOMORE
            );

            // 스케줄 추가
            CourseSchedule schedule1 = CourseScheduleFixture.createCourseSchedule(
                    mathAllGrade1, CourseDay.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0)
            );
            CourseSchedule schedule2 = CourseScheduleFixture.createCourseSchedule(
                    mathAllGrade1, CourseDay.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(11, 0)
            );
            CourseSchedule schedule3 = CourseScheduleFixture.createCourseSchedule(
                    mathFreshman1, CourseDay.TUESDAY, LocalTime.of(13, 0), LocalTime.of(15, 0)
            );

            mathAllGrade1.addCourseSchedule(schedule1);
            mathAllGrade1.addCourseSchedule(schedule2);
            mathFreshman1.addCourseSchedule(schedule3);

            courseRepository.saveAll(List.of(
                    mathAllGrade1, mathAllGrade2,
                    mathFreshman1, mathFreshman2,
                    mathSophomore1, mathSophomore2,
                    cseCourse
            ));
        }

        @Test
        void 수학과_학과코드로_조회하면_수학과_과목만_반환된다() {
            //given
            final String department = "MATHEMATICS";

            //when
            final CoursesResponse response = courseService.getOtherDepartmentCourses(department);

            //then
            assertThat(response.courseResponses()).hasSize(6);
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::department)
                    .containsOnly("수학과");
        }

        @Test
        void 전학년_1학년_2학년_순서로_정렬되어_조회된다() {
            //given
            final String department = "MATHEMATICS";

            //when
            final CoursesResponse response = courseService.getOtherDepartmentCourses(department);
            final List<String> grades = response.courseResponses().stream()
                    .map(CourseResponse::grade)
                    .toList();

            //then
            assertThat(grades).containsExactly(
                    "전학년", "전학년",  // MATH101, MATH102
                    "1학년", "1학년",    // MATH201, MATH202
                    "2학년", "2학년"     // MATH301, MATH302
            );
        }

        @Test
        void 스케줄이_있는_과목은_요일순으로_정렬되어_반환된다() {
            //given
            final String department = "MATHEMATICS";

            //when
            final CoursesResponse response = courseService.getOtherDepartmentCourses(department);

            //then
            // MATH101: [07-401:월(1-2A),수(1-2A)]
            final CourseResponse mathAllGrade1 = response.courseResponses().stream()
                    .filter(c -> c.courseCode().equals("MATH101"))
                    .findFirst()
                    .orElseThrow();
            assertThat(mathAllGrade1.schedule()).isEqualTo("월 1-2A (07-401) 수 1-2A (07-401)");

            // MATH201: [07-401:화(1-2A)]
            final CourseResponse mathFreshman1 = response.courseResponses().stream()
                    .filter(c -> c.courseCode().equals("MATH201"))
                    .findFirst()
                    .orElseThrow();
            assertThat(mathFreshman1.schedule()).isEqualTo("화 1-2A (07-401)");
        }

        @Test
        void 다른_학과_과목은_조회되지_않는다() {
            //given
            final String department = "MATHEMATICS";

            //when
            final CoursesResponse response = courseService.getOtherDepartmentCourses(department);

            //then
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::courseCode)
                    .doesNotContain("CSE101");
        }

        @Test
        void 잘못된_학과_코드로_조회하면_예외가_발생한다() {
            //given
            final String invalidDepartment = "INVALID_DEPARTMENT";

            //when & then
            assertThatThrownBy(() -> courseService.getOtherDepartmentCourses(invalidDepartment))
                    .isInstanceOf(RestApiException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", INVALID_ENUM_TYPE);
        }

        @Test
        void 학과가_아닌_값으로_조회하면_예외가_발생한다() {
            //given
            final String generalEducation = "GENERAL_EDUCATION";

            //when & then
            assertThatThrownBy(() -> courseService.getOtherDepartmentCourses(generalEducation))
                    .isInstanceOf(RestApiException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", INVALID_ENUM_TYPE);
        }

    }

    @Nested
    class 학제간융합전공_과목_조회_테스트 {

        @BeforeEach
        void setUp() {
            // 소셜데이터사이언스 연계전공 과목들
            // 전학년 과목 2개
            Course socialDataAllGrade1 = CourseFixture.createCourseWithDepartmentAndDetails(
                    "빅데이터분석", "Big Data Analysis", "SDS101", "SDS101001",
                    CourseDepartment.SOCIAL_DATA_SCIENCE,
                    CourseGrade.ALL
            );
            Course socialDataAllGrade2 = CourseFixture.createCourseWithDepartmentAndDetails(
                    "데이터사이언스개론", "Intro to Data Science", "SDS102", "SDS102001",
                    CourseDepartment.SOCIAL_DATA_SCIENCE,
                    CourseGrade.ALL
            );

            // 1학년 과목 2개
            Course socialDataFreshman1 = CourseFixture.createCourseWithDepartmentAndDetails(
                    "통계학기초", "Basic Statistics", "SDS201", "SDS201001",
                    CourseDepartment.SOCIAL_DATA_SCIENCE,
                    CourseGrade.FRESHMAN
            );
            Course socialDataFreshman2 = CourseFixture.createCourseWithDepartmentAndDetails(
                    "프로그래밍입문", "Programming Intro", "SDS202", "SDS202001",
                    CourseDepartment.SOCIAL_DATA_SCIENCE,
                    CourseGrade.FRESHMAN
            );

            // 2학년 과목 2개
            Course socialDataSophomore1 = CourseFixture.createCourseWithDepartmentAndDetails(
                    "머신러닝", "Machine Learning", "SDS301", "SDS301001",
                    CourseDepartment.SOCIAL_DATA_SCIENCE,
                    CourseGrade.SOPHOMORE
            );
            Course socialDataSophomore2 = CourseFixture.createCourseWithDepartmentAndDetails(
                    "데이터시각화", "Data Visualization", "SDS302", "SDS302001",
                    CourseDepartment.SOCIAL_DATA_SCIENCE,
                    CourseGrade.SOPHOMORE
            );

            // 미래자동차 연계전공 과목 (다른 연계전공)
            Course futureAutoCourse = CourseFixture.createCourseWithDepartmentAndDetails(
                    "자율주행개론", "Intro to Autonomous Driving", "FA101", "FA101001",
                    CourseDepartment.FUTURE_AUTOMOBILE,
                    CourseGrade.SOPHOMORE
            );

            // 일반 학과 과목 (컴퓨터공학부)
            Course cseCourse = CourseFixture.createCourseWithDetails(
                    "자료구조", "Data Structure", "CSE101", "CSE101001",
                    CourseGrade.SOPHOMORE
            );

            // 스케줄 추가
            CourseSchedule schedule1 = CourseScheduleFixture.createCourseSchedule(
                    socialDataAllGrade1, CourseDay.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0)
            );
            CourseSchedule schedule2 = CourseScheduleFixture.createCourseSchedule(
                    socialDataAllGrade1, CourseDay.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(11, 0)
            );
            CourseSchedule schedule3 = CourseScheduleFixture.createCourseSchedule(
                    socialDataFreshman1, CourseDay.TUESDAY, LocalTime.of(13, 0), LocalTime.of(15, 0)
            );

            socialDataAllGrade1.addCourseSchedule(schedule1);
            socialDataAllGrade1.addCourseSchedule(schedule2);
            socialDataFreshman1.addCourseSchedule(schedule3);

            courseRepository.saveAll(List.of(
                    socialDataAllGrade1, socialDataAllGrade2,
                    socialDataFreshman1, socialDataFreshman2,
                    socialDataSophomore1, socialDataSophomore2,
                    futureAutoCourse,
                    cseCourse
            ));
        }

        @Test
        void 소셜데이터사이언스_학과코드로_조회하면_해당_연계전공_과목만_반환된다() {
            //given
            final String department = "SOCIAL_DATA_SCIENCE";

            //when
            final CoursesResponse response = courseService.getInterdisciplinaryMajorCourses(department);

            //then
            assertThat(response.courseResponses()).hasSize(6);
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::courseCode)
                    .containsExactlyInAnyOrder("SDS101", "SDS102", "SDS201", "SDS202", "SDS301", "SDS302");
        }

        @Test
        void 전학년_1학년_2학년_순서로_정렬되어_조회된다() {
            //given
            final String department = "SOCIAL_DATA_SCIENCE";

            //when
            final CoursesResponse response = courseService.getInterdisciplinaryMajorCourses(department);
            final List<String> grades = response.courseResponses().stream()
                    .map(CourseResponse::grade)
                    .toList();

            //then
            assertThat(grades).containsExactly(
                    "전학년", "전학년",  // SDS101, SDS102
                    "1학년", "1학년",    // SDS201, SDS202
                    "2학년", "2학년"     // SDS301, SDS302
            );
        }

        @Test
        void 스케줄이_있는_과목은_요일순으로_정렬되어_반환된다() {
            //given
            final String department = "SOCIAL_DATA_SCIENCE";

            //when
            final CoursesResponse response = courseService.getInterdisciplinaryMajorCourses(department);

            //then
            // SDS101: [07-401:월(1-2A),수(1-2A)]
            final CourseResponse allGrade1 = response.courseResponses().stream()
                    .filter(c -> c.courseCode().equals("SDS101"))
                    .findFirst()
                    .orElseThrow();
            assertThat(allGrade1.schedule()).isEqualTo("월 1-2A (07-401) 수 1-2A (07-401)");

            // SDS201: [07-401:화(1-2A)]
            final CourseResponse freshman1 = response.courseResponses().stream()
                    .filter(c -> c.courseCode().equals("SDS201"))
                    .findFirst()
                    .orElseThrow();
            assertThat(freshman1.schedule()).isEqualTo("화 1-2A (07-401)");
        }

        @Test
        void 스케줄이_없는_과목은_빈_문자열로_반환된다() {
            //given
            final String department = "SOCIAL_DATA_SCIENCE";

            //when
            final CoursesResponse response = courseService.getInterdisciplinaryMajorCourses(department);

            //then
            final CourseResponse allGrade2 = response.courseResponses().stream()
                    .filter(c -> c.courseCode().equals("SDS102"))
                    .findFirst()
                    .orElseThrow();
            assertThat(allGrade2.schedule()).isEmpty();
        }

        @Test
        void 다른_연계전공_과목은_조회되지_않는다() {
            //given
            final String department = "SOCIAL_DATA_SCIENCE";

            //when
            final CoursesResponse response = courseService.getInterdisciplinaryMajorCourses(department);

            //then
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::courseCode)
                    .doesNotContain("FA101");
        }

        @Test
        void 일반_학과_과목은_조회되지_않는다() {
            //given
            final String department = "SOCIAL_DATA_SCIENCE";

            //when
            final CoursesResponse response = courseService.getInterdisciplinaryMajorCourses(department);

            //then
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::courseCode)
                    .doesNotContain("CSE101");
        }

        @Test
        void 잘못된_학과_코드로_조회하면_예외가_발생한다() {
            //given
            final String invalidDepartment = "INVALID_DEPARTMENT";

            //when & then
            assertThatThrownBy(() -> courseService.getInterdisciplinaryMajorCourses(invalidDepartment))
                    .isInstanceOf(RestApiException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", INVALID_ENUM_TYPE);
        }

        @Test
        void 일반_학과_코드로_조회하면_예외가_발생한다() {
            //given
            final String normalDepartment = "COMPUTER_ENGINEERING";

            //when & then
            assertThatThrownBy(() -> courseService.getInterdisciplinaryMajorCourses(normalDepartment))
                    .isInstanceOf(RestApiException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", INVALID_INTERDISCIPLINARY_DEPARTMENT);
        }
    }

//    @Nested
//    class 키워드_검색_테스트 {
//
//        @BeforeEach
//        void setUp() {
//            // 컴퓨터공학부 과목들
//            Course cse1 = CourseFixture.createCourseWithDetails(
//                    "자료구조", "Data Structure", "CSE101",
//                    CourseGrade.SOPHOMORE, "김교수", "공학관101"
//            );
//            Course cse2 = CourseFixture.createCourseWithDetails(
//                    "알고리즘", "Algorithm", "CSE201",
//                    CourseGrade.SOPHOMORE, "이교수", "공학관201"
//            );
//            Course cse3 = CourseFixture.createCourseWithDetails(
//                    "알고리즘설계", "Algorithm Design", "CSE202",
//                    CourseGrade.JUNIOR, null, null
//            );
//
//            // 수학과 과목들
//            Course math1 = CourseFixture.createCourseWithDepartmentAndDetails(
//                    "선형대수", "Linear Algebra", "MATH101",
//                    CourseDepartment.MATHEMATICS,
//                    CourseGrade.FRESHMAN, "박교수", "자연관101"
//            );
//            Course math2 = CourseFixture.createCourseWithDepartmentAndDetails(
//                    "데이터분석", "Data Analysis", "MATH201",
//                    CourseDepartment.MATHEMATICS,
//                    CourseGrade.SOPHOMORE, "최교수", "자연관201"
//            );
//
//            // 소셜데이터사이언스 연계전공 과목
//            Course sds = CourseFixture.createCourseWithDepartmentAndDetails(
//                    "빅데이터분석", "Big Data Analysis", "SDS101",
//                    CourseDepartment.SOCIAL_DATA_SCIENCE,
//                    CourseGrade.ALL, "정교수", null
//            );
//
//            // 스케줄 추가
//            CourseSchedule schedule1 = CourseScheduleFixture.createCourseSchedule(
//                    cse1, "월3,4", CourseDay.MONDAY, LocalTime.of(13, 0), LocalTime.of(15, 0)
//            );
//            CourseSchedule schedule2 = CourseScheduleFixture.createCourseSchedule(
//                    cse1, "수3,4", CourseDay.WEDNESDAY, LocalTime.of(13, 0), LocalTime.of(15, 0)
//            );
//            CourseSchedule schedule3 = CourseScheduleFixture.createCourseSchedule(
//                    cse2, "화1,2", CourseDay.TUESDAY, LocalTime.of(9, 0), LocalTime.of(11, 0)
//            );
//
//            cse1.addCourseSchedule(schedule1);
//            cse1.addCourseSchedule(schedule2);
//            cse2.addCourseSchedule(schedule3);
//
//            courseRepository.saveAll(List.of(cse1, cse2, cse3, math1, math2, sds));
//        }
//
//        @Test
//        void 교과목명_국문으로_검색하면_해당_키워드가_포함된_과목이_조회된다() {
//            //given
//            final String keyword = "알고리즘";
//
//            //when
//            final CoursesResponse response = courseService.searchCourses(keyword);
//
//            //then
//            assertThat(response.courseResponses()).hasSize(2);
//            assertThat(response.courseResponses())
//                    .extracting(CourseResponse::name)
//                    .containsExactlyInAnyOrder("알고리즘", "알고리즘설계");
//        }
//
//        @Test
//        void 교과목명_영문으로_검색하면_해당_키워드가_포함된_과목이_조회된다() {
//            //given
//            final String keyword = "Data";
//
//            //when
//            final CoursesResponse response = courseService.searchCourses(keyword);
//
//            //then
//            assertThat(response.courseResponses()).hasSize(3);
//            assertThat(response.courseResponses())
//                    .extracting(CourseResponse::nameEn)
//                    .containsExactlyInAnyOrder("Data Structure", "Data Analysis", "Big Data Analysis");
//        }
//
//        @Test
//        void 과목코드로_검색하면_해당_키워드가_포함된_과목이_조회된다() {
//            //given
//            final String keyword = "CSE";
//
//            //when
//            final CoursesResponse response = courseService.searchCourses(keyword);
//
//            //then
//            assertThat(response.courseResponses()).hasSize(3);
//            assertThat(response.courseResponses())
//                    .extracting(CourseResponse::courseCode)
//                    .containsExactlyInAnyOrder("CSE101", "CSE201", "CSE202");
//        }
//
//        @Test
//        void 부분_문자열_검색이_정상_동작한다() {
//            //given
//            final String keyword = "101";
//
//            //when
//            final CoursesResponse response = courseService.searchCourses(keyword);
//
//            //then
//            assertThat(response.courseResponses()).hasSize(3);
//            assertThat(response.courseResponses())
//                    .extracting(CourseResponse::courseCode)
//                    .containsExactlyInAnyOrder("CSE101", "MATH101", "SDS101");
//        }
//
//        @Test
//        void 여러_필드에서_동시에_매칭되어도_중복_없이_조회된다() {
//            //given
//            final String keyword = "Data";
//
//            //when
//            final CoursesResponse response = courseService.searchCourses(keyword);
//
//            //then
//            // "빅데이터분석" 과목은 국문 과목명과 영문 과목명 모두에 "Data"를 포함하지만 한 번만 조회
//            assertThat(response.courseResponses()).hasSize(3);
//            final long sdsCount = response.courseResponses().stream()
//                    .filter(c -> c.courseCode().equals("SDS101"))
//                    .count();
//            assertThat(sdsCount).isEqualTo(1);
//        }
//
//        @Test
//        void 검색_결과가_없으면_빈_리스트가_반환된다() {
//            //given
//            final String keyword = "존재하지않는과목";
//
//            //when
//            final CoursesResponse response = courseService.searchCourses(keyword);
//
//            //then
//            assertThat(response.courseResponses()).isEmpty();
//        }
//
//        @Test
//        void 스케줄이_있는_과목은_요일순으로_정렬되어_반환된다() {
//            //given
//            final String keyword = "CSE";
//
//            //when
//            final CoursesResponse response = courseService.searchCourses(keyword);
//
//            //then
//            // CSE101: [07-401:월(1-2A),수(1-2A)]
//            final CourseResponse cse101 = response.courseResponses().stream()
//                    .filter(c -> c.courseCode().equals("CSE101"))
//                    .findFirst()
//                    .orElseThrow();
//            assertThat(cse101.schedule()).isEqualTo("월 1-2A (07-401) 수 1-2A (07-401)");
//
//            // CSE201: [07-401:화(1-2A)]
//            final CourseResponse cse201 = response.courseResponses().stream()
//                    .filter(c -> c.courseCode().equals("CSE201"))
//                    .findFirst()
//                    .orElseThrow();
//            assertThat(cse201.schedule()).isEqualTo("화 1-2A (07-401)");
//        }
//
//        @Test
//        void 스케줄이_없는_과목은_빈_문자열로_반환된다() {
//            //given
//            final String keyword = "알고리즘설계";
//
//            //when
//            final CoursesResponse response = courseService.searchCourses(keyword);
//
//            //then
//            final CourseResponse cse202 = response.courseResponses().stream()
//                    .filter(c -> c.courseCode().equals("CSE202"))
//                    .findFirst()
//                    .orElseThrow();
//            assertThat(cse202.schedule()).isEmpty();
//        }
//
//        @Test
//        void null값인_교수명과_강의실은_하이픈으로_반환된다() {
//            //given
//            final String keyword = "알고리즘설계";
//
//            //when
//            final CoursesResponse response = courseService.searchCourses(keyword);
//
//            //then
//            final CourseResponse cse202 = response.courseResponses().stream()
//                    .filter(c -> c.courseCode().equals("CSE202"))
//                    .findFirst()
//                    .orElseThrow();
//            assertThat(cse202.professor()).isEqualTo("-");
//            assertThat(cse202.classroom()).isEqualTo("-");
//        }
//
//        @Test
//        void 다양한_학과의_과목들이_모두_검색된다() {
//            //given
//            final String keyword = "101";
//
//            //when
//            final CoursesResponse response = courseService.searchCourses(keyword);
//
//            //then
//            assertThat(response.courseResponses())
//                    .extracting(CourseResponse::department)
//                    .containsExactlyInAnyOrder("컴퓨터공학부", "수학과", "소셜데이터사이언스연계전공");
//        }
//
//        @Test
//        void 대소문자_구분_없이_검색된다() {
//            //given
//            final String keywordLower = "data";
//
//            //when
//            final CoursesResponse response = courseService.searchCourses(keywordLower);
//
//            //then
//            assertThat(response.courseResponses()).hasSize(3);
//        }
//    }

    @Nested
    class 목록_정렬_테스트 {

        @BeforeEach
        void setUp() {
            // 같은 학년 안에서 이수구분과 학수번호로 갈리도록 일부러 뒤섞어 저장한다
            courseRepository.saveAll(List.of(
                    createComputerEngineeringCourse("SORT004", CourseClassification.MAJOR_CORE, CourseGrade.FRESHMAN),
                    createComputerEngineeringCourse("SORT002", CourseClassification.BASIC_LIBERAL_ARTS, CourseGrade.ALL),
                    createComputerEngineeringCourse("SORT003", CourseClassification.MAJOR_BASIC, CourseGrade.FRESHMAN),
                    createComputerEngineeringCourse("SORT001", CourseClassification.BASIC_LIBERAL_ARTS, CourseGrade.ALL)
            ));
        }

        @Test
        void 학년_이수구분_학수번호_순으로_정렬된다() {
            //given

            //when
            final CoursesResponse response = courseService.getOtherDepartmentCourses(
                    CourseDepartment.COMPUTER_ENGINEERING.name()
            );

            //then
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::code)
                    .containsExactly("SORT001", "SORT002", "SORT003", "SORT004");
        }
    }

    @Nested
    class 원어강의_표기_테스트 {

        @BeforeEach
        void setUp() {
            courseRepository.saveAll(List.of(
                    createEnglishCourse("ENG001001", true),
                    createEnglishCourse("KOR001001", false)
            ));
        }

        @Test
        void 원어강의가_아니면_원어강의명은_빈_문자열로_내려간다() {
            //given

            //when
            final CoursesResponse response = courseService.getOtherDepartmentCourses(
                    CourseDepartment.COMPUTER_ENGINEERING.name()
            );

            //then
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::code, CourseResponse::englishCourseName)
                    .containsExactlyInAnyOrder(
                            tuple("ENG001001", "원어강의(EN)"),
                            tuple("KOR001001", "")
                    );
        }
    }

    @Nested
    class HUSS_과목_조회_테스트 {

        @BeforeEach
        void setUp() {
            courseRepository.saveAll(List.of(
                    // HUSS 교과목은 학과가 여러 개에 걸쳐 있다
                    CourseFixture.createHussCourse(
                            "글로벌리더십", "Global Leadership", "HUSS001", "HUSS001001",
                            CourseDepartment.HUSS_OTHER_UNIVERSITY
                    ),
                    CourseFixture.createHussCourse(
                            "포용사회의이해", "Understanding Inclusive Society", "HUSS002", "HUSS002001",
                            CourseDepartment.GLOBAL_TRADE_SERVICE
                    ),
                    // 같은 학과의 非HUSS 과목. 섞이면 안 된다
                    CourseFixture.createCourseWithDepartmentAndDetails(
                            "무역실무", "Trade Practice", "GTS001", "GTS001001",
                            CourseDepartment.GLOBAL_TRADE_SERVICE, CourseGrade.SOPHOMORE
                    )
            ));
        }

        @Test
        void 학과를_가리지_않고_HUSS_교과목만_반환된다() {
            //given

            //when
            final CoursesResponse response = courseService.getHussCourses();

            //then
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::code)
                    .containsExactlyInAnyOrder("HUSS001001", "HUSS002001");
        }
    }

    @Nested
    class 카테고리_조회_테스트 {

        @BeforeEach
        void setUp() {
            courseRepository.saveAll(List.of(
                    // 전공핵심(31) - 전공핵심(34)
                    createCategoryCourse("CAT001001", CourseClassification.MAJOR_CORE, CourseArea.MAJOR_CORE),
                    // 기초교양(11) - 학문의기초(161), 같은 조합을 두 번 넣어 중복 제거를 확인한다
                    createCategoryCourse("CAT002001", CourseClassification.BASIC_LIBERAL_ARTS, CourseArea.ACADEMIC_FOUNDATION),
                    createCategoryCourse("CAT003001", CourseClassification.BASIC_LIBERAL_ARTS, CourseArea.ACADEMIC_FOUNDATION),
                    // 기초교양(11) - 기초과학·공학(162)
                    createCategoryCourse("CAT004001", CourseClassification.BASIC_LIBERAL_ARTS, CourseArea.BASIC_SCIENCE_ENGINEERING),
                    // 핵심교양(21) - (핵심)인문(172)
                    createCategoryCourse("CAT005001", CourseClassification.CORE_LIBERAL_ARTS, CourseArea.CORE_HUMANITIES)
            ));
        }

        @Test
        void 이수구분별로_이수영역이_묶여서_코드_오름차순으로_반환된다() {
            //given

            //when
            final CourseCategoriesResponse response = courseService.getCategories();

            //then
            assertThat(response.categoryResponses())
                    .extracting(CourseCategoryResponse::code, CourseCategoryResponse::name)
                    .containsExactly(
                            tuple("11", "기초교양"),
                            tuple("21", "핵심교양"),
                            tuple("31", "전공핵심")
                    );

            assertThat(response.categoryResponses().get(0).areaResponses())
                    .extracting(CourseAreaResponse::code, CourseAreaResponse::name)
                    .containsExactly(
                            tuple("161", "학문의기초"),
                            tuple("162", "기초과학·공학")
                    );
        }
    }

    @Nested
    class 년도_학기_조회_테스트 {

        @BeforeEach
        void setUp() {
            courseRepository.saveAll(List.of(
                    CourseFixture.createCourseWithDetails("자료구조", "Data Structure", "TERM001", "TERM001001", CourseGrade.ALL),
                    CourseFixture.createCourseWithDetails("알고리즘", "Algorithm", "TERM002", "TERM002001", CourseGrade.ALL)
            ));
        }

        @Test
        void 적재된_년도와_학기가_중복_없이_반환된다() {
            //given

            //when
            final CourseTermsResponse response = courseService.getTerms();

            //then
            assertThat(response.termResponses())
                    .extracting(CourseTermResponse::academicYear, CourseTermResponse::termCode, CourseTermResponse::termName)
                    .containsExactly(tuple(2026, "20", "2학기"));
        }
    }

    @Nested
    class 연계전공_조회_테스트 {

        @BeforeEach
        void setUp() {
            courseRepository.saveAll(List.of(
                    createInterdisciplinaryCourse("SDS001001", CourseDepartment.SOCIAL_DATA_SCIENCE),
                    createInterdisciplinaryCourse("LOG001001", CourseDepartment.LOGISTICS),
                    // 연계전공이 아닌 일반 학과. 목록에 섞이면 안 된다
                    CourseFixture.createCourseWithDepartmentAndDetails(
                            "미적분학", "Calculus", "MATH101", "MATH101001",
                            CourseDepartment.MATHEMATICS, CourseGrade.FRESHMAN
                    )
            ));
        }

        @Test
        void 과목이_적재된_연계전공만_반환된다() {
            //given

            //when
            final InterdisciplinaryMajorsResponse response = courseService.getInterdisciplinaryMajors();

            //then
            assertThat(response.interdisciplinaryMajorResponses())
                    .extracting(InterdisciplinaryMajorResponse::code, InterdisciplinaryMajorResponse::name)
                    .containsExactly(
                            tuple("LOGISTICS", "물류학전공(연계)"),
                            tuple("SOCIAL_DATA_SCIENCE", "소셜데이터사이언스연계전공")
                    );
        }
    }

    private static Course createComputerEngineeringCourse(
            final String haksuCode,
            final CourseClassification classification,
            final CourseGrade grade
    ) {
        return CourseFixture.createCourse(
                "정렬대상", "Sort Target", haksuCode.substring(0, 7), haksuCode,
                CourseCollege.INFORMATION_TECHNOLOGY,
                CourseDepartment.COMPUTER_ENGINEERING,
                classification,
                CourseArea.MAJOR_CORE,
                CourseType.LECTURE,
                grade,
                3, false, 50, 30
        );
    }

    private static Course createEnglishCourse(
            final String haksuCode,
            final boolean isEnglishCourse
    ) {
        return CourseFixture.createCourse(
                "원어강의대상", "English Target", haksuCode.substring(0, 7), haksuCode,
                CourseCollege.INFORMATION_TECHNOLOGY,
                CourseDepartment.COMPUTER_ENGINEERING,
                CourseClassification.MAJOR_CORE,
                CourseArea.MAJOR_CORE,
                CourseType.LECTURE,
                CourseGrade.SOPHOMORE,
                3, isEnglishCourse, 50, 30
        );
    }

    private static Course createCategoryCourse(
            final String haksuCode,
            final CourseClassification classification,
            final CourseArea area
    ) {
        return CourseFixture.createCourse(
                "카테고리대상", "Category Target", haksuCode.substring(0, 7), haksuCode,
                CourseCollege.GENERAL_EDUCATION,
                CourseDepartment.GENERAL_EDUCATION,
                classification,
                area,
                CourseType.LECTURE,
                CourseGrade.ALL,
                3, false, 50, 30
        );
    }

    private static Course createInterdisciplinaryCourse(
            final String haksuCode,
            final CourseDepartment department
    ) {
        return CourseFixture.createCourse(
                "연계전공대상", "Interdisciplinary Target", haksuCode.substring(0, 7), haksuCode,
                department.getCourseCollege(),
                department,
                CourseClassification.MAJOR_CORE,
                CourseArea.MAJOR_CORE,
                CourseType.LECTURE,
                CourseGrade.SOPHOMORE,
                3, false, 50, 30
        );
    }

    @Nested
    class 폐강_강의_제외_테스트 {

        private static final String TEST_STUDENT_ID = "202099999";
        private static final String TEST_NAME = "폐강테스터";

        private long memberId;

        @BeforeEach
        void setUp() {
            final Member member = MemberFixture.createMember(
                    TEST_STUDENT_ID,
                    TEST_NAME,
                    MemberCollege.INFORMATION_TECHNOLOGY,
                    MemberDepartment.COMPUTER_ENGINEERING,
                    MemberGrade.JUNIOR,
                    AcademicStatus.ENROLLED,
                    3.5
            );
            memberRepository.save(member);
            memberId = member.getId();

            final Course activeCourse = CourseFixture.createCourseWithDepartmentAndDetails(
                    "개설과목", "Active Course", "CSE5010", "CSE5010001",
                    CourseDepartment.COMPUTER_ENGINEERING, CourseGrade.JUNIOR
            );
            final Course closedCourse = CourseFixture.createCourseWithDepartmentAndDetails(
                    "폐강과목", "Closed Course", "CSE5020", "CSE5020001",
                    CourseDepartment.COMPUTER_ENGINEERING, CourseGrade.JUNIOR
            );
            closedCourse.close();

            courseRepository.saveAll(List.of(activeCourse, closedCourse));
        }

        @Test
        void 전공_조회에서_폐강_강의가_빠진다() {
            //when
            final CoursesResponse response = courseService.getMajorCourses(memberId);

            //then
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::name)
                    .containsExactly("개설과목");
        }

        @Test
        void 타학과_조회에서_폐강_강의가_빠진다() {
            //when
            final CoursesResponse response = courseService.getOtherDepartmentCourses(
                    CourseDepartment.COMPUTER_ENGINEERING.name()
            );

            //then
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::name)
                    .containsExactly("개설과목");
        }

        @Test
        void HUSS_조회에서_폐강_강의가_빠진다() {
            //given
            final Course activeHuss = CourseFixture.createHussCourse(
                    "HUSS개설", "Active Huss", "CSE6010", "CSE6010001",
                    CourseDepartment.COMPUTER_ENGINEERING
            );
            final Course closedHuss = CourseFixture.createHussCourse(
                    "HUSS폐강", "Closed Huss", "CSE6020", "CSE6020001",
                    CourseDepartment.COMPUTER_ENGINEERING
            );
            closedHuss.close();
            courseRepository.saveAll(List.of(activeHuss, closedHuss));

            //when
            final CoursesResponse response = courseService.getHussCourses();

            //then
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::name)
                    .containsExactly("HUSS개설");
        }

        @Test
        void 교양_조회에서_폐강_강의가_빠진다() {
            //given
            final Course activeGeneral = CourseFixture.createCourse(
                    "교양개설", "Active General", "GEN1010", "GEN1010001",
                    CourseCollege.GENERAL_EDUCATION,
                    CourseDepartment.COMPUTER_ENGINEERING,
                    CourseClassification.CORE_LIBERAL_ARTS,
                    CourseArea.CORE_HUMANITIES,
                    CourseType.LECTURE,
                    CourseGrade.ALL,
                    3, false, 50, 0
            );
            final Course closedGeneral = CourseFixture.createCourse(
                    "교양폐강", "Closed General", "GEN1020", "GEN1020001",
                    CourseCollege.GENERAL_EDUCATION,
                    CourseDepartment.COMPUTER_ENGINEERING,
                    CourseClassification.CORE_LIBERAL_ARTS,
                    CourseArea.CORE_HUMANITIES,
                    CourseType.LECTURE,
                    CourseGrade.ALL,
                    3, false, 50, 0
            );
            closedGeneral.close();
            courseRepository.saveAll(List.of(activeGeneral, closedGeneral));

            //when
            final CoursesResponse response = courseService.getGeneralEducationCourses(
                    CourseClassification.CORE_LIBERAL_ARTS.getCode(),
                    CourseArea.CORE_HUMANITIES.getCode()
            );

            //then
            assertThat(response.courseResponses())
                    .extracting(CourseResponse::name)
                    .containsExactly("교양개설");
        }

        @Test
        void 폐강_강의도_카테고리_목록에는_남는다() {
            //when
            final CourseCategoriesResponse response = courseService.getCategories();

            //then
            assertThat(response.categoryResponses()).isNotEmpty();
        }
    }

    @Nested
    class 학과_목록_조회_테스트 {

        @BeforeEach
        void setUp() {
            final Course computerCourse = CourseFixture.createCourseWithDepartmentAndDetails(
                    "자료구조", "Data Structure", "COM101", "COM101001",
                    CourseDepartment.COMPUTER_ENGINEERING, CourseGrade.SOPHOMORE
            );
            final Course businessCourse = CourseFixture.createCourseWithDepartmentAndDetails(
                    "경영학원론", "Business", "BUS101", "BUS101001",
                    CourseDepartment.BUSINESS_ADMINISTRATION, CourseGrade.FRESHMAN
            );
            final Course generalEducationCourse = CourseFixture.createCourse(
                    "글쓰기", "Writing", "GEN101", "GEN101001",
                    CourseCollege.GENERAL_EDUCATION, CourseDepartment.GENERAL_EDUCATION,
                    CourseClassification.CORE_LIBERAL_ARTS, CourseArea.CORE_HUMANITIES,
                    CourseType.LECTURE, CourseGrade.ALL,
                    3, false, 50, 0
            );

            courseRepository.saveAll(List.of(computerCourse, businessCourse, generalEducationCourse));
        }

        @Test
        void 과목이_적재된_학과만_반환된다() {
            //given

            //when
            final DepartmentsResponse response = courseService.getDepartments();

            //then
            assertThat(response.departmentResponses())
                    .extracting(DepartmentResponse::code)
                    .containsExactlyInAnyOrder(
                            MemberDepartment.COMPUTER_ENGINEERING.name(),
                            MemberDepartment.BUSINESS_ADMINISTRATION.name()
                    );
        }

        @Test
        void 학과_코드와_이름이_함께_반환된다() {
            //given

            //when
            final DepartmentsResponse response = courseService.getDepartments();

            //then
            assertThat(response.departmentResponses())
                    .extracting(DepartmentResponse::name)
                    .contains("컴퓨터공학부", "경영학부");
        }

        @Test
        void 학생_소속이_아닌_교양은_학과_목록에_없다() {
            //given

            //when
            final DepartmentsResponse response = courseService.getDepartments();

            //then
            assertThat(response.departmentResponses())
                    .extracting(DepartmentResponse::name)
                    .doesNotContain("교양", "일선", "교직", "군사학");
        }
    }
}
