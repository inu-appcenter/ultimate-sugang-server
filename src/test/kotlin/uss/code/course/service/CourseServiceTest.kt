package uss.code.course.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uss.code.course.domain.Course
import uss.code.course.domain.CourseArea
import uss.code.course.domain.CourseClassification
import uss.code.course.domain.CourseCollege
import uss.code.course.domain.CourseDay
import uss.code.course.domain.CourseDepartment
import uss.code.course.domain.CourseGrade
import uss.code.course.domain.CourseType
import uss.code.course.dto.response.CourseAreaResponse
import uss.code.course.dto.response.CourseCategoryResponse
import uss.code.course.dto.response.CourseResponse
import uss.code.course.dto.response.CourseTermResponse
import uss.code.course.dto.response.DepartmentResponse
import uss.code.course.dto.response.InterdisciplinaryMajorResponse
import uss.code.course.fixture.CourseFixture
import uss.code.course.fixture.CourseScheduleFixture
import uss.code.course.repository.CourseRepository
import uss.code.global.exception.domain.ExceptionCode.INVALID_DEPARTMENT
import uss.code.global.exception.domain.ExceptionCode.INVALID_ENUM_TYPE
import uss.code.global.exception.domain.ExceptionCode.INVALID_GENERAL_EDUCATION_AREA
import uss.code.global.exception.domain.ExceptionCode.INVALID_GENERAL_EDUCATION_CLASSIFICATION
import uss.code.global.exception.domain.ExceptionCode.INVALID_INTERDISCIPLINARY_DEPARTMENT
import uss.code.global.exception.domain.ExceptionCode.MEMBER_NOT_FOUND
import uss.code.global.exception.domain.RestApiException
import uss.code.global.infra.IntegrationTest
import uss.code.member.domain.AcademicStatus
import uss.code.member.domain.MemberDepartment
import uss.code.member.domain.MemberGrade
import uss.code.member.fixture.MemberFixture
import uss.code.member.repository.MemberRepository
import java.time.LocalTime

@IntegrationTest
class CourseServiceTest(
    private val courseService: CourseService,

    private val memberRepository: MemberRepository,
    private val courseRepository: CourseRepository,
) {
    @Nested
    inner class 전공_과목_조회_테스트 {
        private val testStudentId = "202012345"
        private val testName = "홍길동"
        private val testDepartment = MemberDepartment.COMPUTER_ENGINEERING
        private val testGrade = MemberGrade.JUNIOR
        private val testAcademicStatus = AcademicStatus.ENROLLED
        private val testGpa = 3.5

        private var validMemberId = 0L
        private val invalidMemberId = 999L

        @BeforeEach
        fun setUp() {
            // 회원 생성 (컴퓨터공학부)
            val member = MemberFixture.createMember(
                testStudentId,
                testName,
                testDepartment,
                testGrade,
                testAcademicStatus,
                testGpa,
            )
            memberRepository.save(member)
            validMemberId = member.id

            // 전학년 과목 2개
            val allGrade1 = CourseFixture.createCourseWithDetails(
                "자료구조", "Data Structure", "COM001", "COM001001",
                CourseGrade.ALL,
            )
            val allGrade2 = CourseFixture.createCourseWithDetails(
                "알고리즘", "Algorithm", "COM002", "COM002001",
                CourseGrade.ALL,
            )

            // 1학년 과목 2개
            val freshman1 = CourseFixture.createCourseWithDetails(
                "프로그래밍기초", "Programming Basics", "COM101", "COM101001",
                CourseGrade.FRESHMAN,
            )
            val freshman2 = CourseFixture.createCourseWithDetails(
                "컴퓨터개론", "Introduction to Computer", "COM102", "COM102001",
                CourseGrade.FRESHMAN,
            )

            // 2학년 과목 2개
            val sophomore1 = CourseFixture.createCourseWithDetails(
                "객체지향프로그래밍", "OOP", "COM201", "COM201001",
                CourseGrade.SOPHOMORE,
            )
            val sophomore2 = CourseFixture.createCourseWithDetails(
                "데이터베이스", "Database", "COM202", "COM202001",
                CourseGrade.SOPHOMORE,
            )

            // 컴공이 아닌 다른 학과 과목
            val otherDept = CourseFixture.createCourseWithDepartmentAndDetails(
                "미적분학", "Calculus", "MATH101", "MATH101001",
                CourseDepartment.MATHEMATICS,
                CourseGrade.FRESHMAN,
            )

            // 스케줄 추가 (저장 전에 추가)
            val schedule1 = CourseScheduleFixture.createCourseSchedule(
                allGrade1, CourseDay.MONDAY, LocalTime.of(13, 0), LocalTime.of(15, 0),
            )
            val schedule2 = CourseScheduleFixture.createCourseSchedule(
                allGrade1, CourseDay.WEDNESDAY, LocalTime.of(13, 0), LocalTime.of(15, 0),
            )
            val schedule3 = CourseScheduleFixture.createCourseSchedule(
                freshman1, CourseDay.TUESDAY, LocalTime.of(9, 0), LocalTime.of(11, 0),
            )

            allGrade1.addCourseSchedule(schedule1)
            allGrade1.addCourseSchedule(schedule2)
            freshman1.addCourseSchedule(schedule3)

            courseRepository.saveAll(
                listOf(
                    allGrade1, allGrade2,
                    freshman1, freshman2,
                    sophomore1, sophomore2,
                    otherDept,
                )
            )
        }

        @Test
        fun 컴퓨터공학부_학생이_전공과목을_조회하면_성공한다() {
            //given

            //when
            val response = courseService.getMajorCourses(validMemberId)

            //then
            assertThat(response.courseResponses).hasSize(6)
            assertThat(response.courseResponses)
                .extracting<String> { it.department }
                .containsOnly("컴퓨터공학부")
        }

        @Test
        fun 전학년_1학년_2학년_순서로_정렬되어_조회된다() {
            //given

            //when
            val response = courseService.getMajorCourses(validMemberId)
            val grades = response.courseResponses.map { it.grade }

            //then
            assertThat(grades).containsExactly(
                "전학년", "전학년", // COM001, COM002
                "1학년", "1학년", // COM101, COM102
                "2학년", "2학년", // COM201, COM202
            )
        }

        @Test
        fun 스케줄이_있는_과목은_요일순으로_정렬되어_반환된다() {
            //given

            //when
            val response = courseService.getMajorCourses(validMemberId)

            //then
            // COM001: [07-401:월(1-2A),수(1-2A)]
            val allGrade1 = response.courseResponses.first { it.courseCode == "COM001" }
            assertThat(allGrade1.schedule).isEqualTo("월 1-2A (07-401) 수 1-2A (07-401)")

            // COM101: [07-401:화(1-2A)]
            val freshman1 = response.courseResponses.first { it.courseCode == "COM101" }
            assertThat(freshman1.schedule).isEqualTo("화 1-2A (07-401)")
        }

        @Test
        fun 다른_학과_과목은_조회되지_않는다() {
            //given

            //when
            val response = courseService.getMajorCourses(validMemberId)

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.courseCode }
                .isNotEmpty()
                .doesNotContain("MATH101")
        }

        @Test
        fun 존재하지_않는_회원_아이디로_조회하면_예외가_발생한다() {
            //given

            //when & then
            assertThatThrownBy { courseService.getMajorCourses(invalidMemberId) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", MEMBER_NOT_FOUND)
        }
    }

    @Nested
    inner class 학부_소속_전공_과목_조회_테스트 {
        private var electronicsMemberId = 0L
        private var liberalArtsMemberId = 0L

        @BeforeEach
        fun setUp() {
            val electronicsMember = MemberFixture.createMember(
                "202112345",
                "김전자",
                MemberDepartment.ELECTRONICS_ENGINEERING_SCHOOL,
                MemberGrade.SOPHOMORE,
                AcademicStatus.ENROLLED,
                3.5,
            )
            memberRepository.save(electronicsMember)
            electronicsMemberId = electronicsMember.id

            val liberalArtsMember = MemberFixture.createMember(
                "202212345",
                "박자유",
                MemberDepartment.INTERNATIONAL_LIBERAL_ARTS,
                MemberGrade.FRESHMAN,
                AcademicStatus.ENROLLED,
                3.0,
            )
            memberRepository.save(liberalArtsMember)
            liberalArtsMemberId = liberalArtsMember.id

            // 학부로 개설된 과목
            val schoolCourse = CourseFixture.createCourseWithDepartmentAndDetails(
                "전자회로", "Electronic Circuits", "ELE101", "ELE101001",
                CourseDepartment.ELECTRONICS_ENGINEERING_SCHOOL,
                CourseGrade.FRESHMAN,
            )

            // 하위 전공으로 개설된 과목
            val majorCourse = CourseFixture.createCourseWithDepartmentAndDetails(
                "디지털신호처리", "Digital Signal Processing", "ELE201", "ELE201001",
                CourseDepartment.ELECTRONICS_ENGINEERING_MAJOR,
                CourseGrade.SOPHOMORE,
            )
            val semiconductorCourse = CourseFixture.createCourseWithDepartmentAndDetails(
                "반도체소자", "Semiconductor Devices", "ELE202", "ELE202001",
                CourseDepartment.SEMICONDUCTOR_CONVERGENCE_MAJOR,
                CourseGrade.SOPHOMORE,
            )

            // 폐지된 학과로 개설된 구 학번 대상 과목
            val abolishedCourse = CourseFixture.createCourseWithDepartmentAndDetails(
                "전자공학세미나", "Electronics Seminar", "ELE401", "ELE401001",
                CourseDepartment.ELECTRONICS_ENGINEERING,
                CourseGrade.SENIOR,
            )

            // 다른 학부 과목
            val otherSchoolCourse = CourseFixture.createCourseWithDepartmentAndDetails(
                "생명과학개론", "Introduction to Life Science", "LIF101", "LIF101001",
                CourseDepartment.LIFE_SCIENCE_SCHOOL,
                CourseGrade.FRESHMAN,
            )

            courseRepository.saveAll(
                listOf(
                    schoolCourse, majorCourse, semiconductorCourse, abolishedCourse, otherSchoolCourse,
                )
            )
        }

        @Test
        fun 학부_소속_학생은_하위_전공_과목까지_함께_조회한다() {
            //given

            //when
            val response = courseService.getMajorCourses(electronicsMemberId)

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.courseCode }
                .containsExactlyInAnyOrder("ELE101", "ELE201", "ELE202", "ELE401")
        }

        @Test
        fun 폐지된_학과의_과목도_후신_학부_학생에게_조회된다() {
            //given

            //when
            val response = courseService.getMajorCourses(electronicsMemberId)

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.department }
                .contains("전자공학과")
        }

        @Test
        fun 다른_학부의_과목은_조회되지_않는다() {
            //given

            //when
            val response = courseService.getMajorCourses(electronicsMemberId)

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.courseCode }
                .isNotEmpty()
                .doesNotContain("LIF101")
        }

        @Test
        fun 대응_강의_학과가_없는_소속의_회원은_예외없이_빈_목록을_받는다() {
            //given

            //when
            val response = courseService.getMajorCourses(liberalArtsMemberId)

            //then
            assertThat(response.courseResponses).isEmpty()
        }

        @Test
        fun 타학과로_학부를_조회하면_학부가_개설한_과목만_조회된다() {
            //given
            val department = "ELECTRONICS_ENGINEERING_SCHOOL"

            //when
            val response = courseService.getOtherDepartmentCourses(department)

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.courseCode }
                .containsExactly("ELE101")
                .doesNotContain("ELE201", "ELE202", "ELE401")
        }

        @Test
        fun 전공_조회는_학부_소속_기준이라_하위_전공까지_함께_조회한다() {
            //given

            //when
            val response = courseService.getMajorCourses(electronicsMemberId)

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.courseCode }
                .contains("ELE101", "ELE201", "ELE202", "ELE401")
        }

        @Test
        fun 학부_소속_조회_결과도_학년_순으로_정렬된다() {
            //given

            //when
            val response = courseService.getMajorCourses(electronicsMemberId)
            val grades = response.courseResponses.map { it.grade }

            //then
            assertThat(grades).containsExactly("1학년", "2학년", "2학년", "4학년")
        }
    }

    @Nested
    inner class 교양_과목_조회_테스트 {
        @BeforeEach
        fun setUp() {
            // 핵심 인문 과목 2개
            val coreHumanities1 = CourseFixture.createCourse(
                "글쓰기", "Writing", "GEN101", "GEN101001",
                CourseCollege.GENERAL_EDUCATION, CourseDepartment.GENERAL_EDUCATION,
                CourseClassification.CORE_LIBERAL_ARTS, CourseArea.CORE_HUMANITIES,
                CourseType.LECTURE,
                CourseGrade.ALL,
                3, false, 50, 30,
            )
            val coreHumanities2 = CourseFixture.createCourse(
                "철학의이해", "Understanding Philosophy", "GEN102", "GEN102001",
                CourseCollege.GENERAL_EDUCATION, CourseDepartment.GENERAL_EDUCATION,
                CourseClassification.CORE_LIBERAL_ARTS, CourseArea.CORE_HUMANITIES,
                CourseType.LECTURE,
                CourseGrade.ALL,
                2, false, 40, 20,
            )

            // 핵심 외국어 과목 2개
            val coreForeignLanguage1 = CourseFixture.createCourse(
                "영어회화", "English Conversation", "GEN201", "GEN201001",
                CourseCollege.GENERAL_EDUCATION, CourseDepartment.GENERAL_EDUCATION,
                CourseClassification.CORE_LIBERAL_ARTS, CourseArea.CORE_FOREIGN_LANGUAGE,
                CourseType.LECTURE,
                CourseGrade.ALL,
                3, true, 45, 25,
            )
            val coreForeignLanguage2 = CourseFixture.createCourse(
                "중국어회화", "Chinese Conversation", "GEN202", "GEN202001",
                CourseCollege.GENERAL_EDUCATION, CourseDepartment.GENERAL_EDUCATION,
                CourseClassification.CORE_LIBERAL_ARTS, CourseArea.CORE_FOREIGN_LANGUAGE,
                CourseType.LECTURE,
                CourseGrade.ALL,
                3, false, 40, 15,
            )

            // 일반 사회 과목 1개
            val social = CourseFixture.createCourse(
                "현대사회와윤리", "Modern Society and Ethics", "GEN301", "GEN301001",
                CourseCollege.GENERAL_EDUCATION, CourseDepartment.GENERAL_EDUCATION,
                CourseClassification.ADVANCED_LIBERAL_ARTS, CourseArea.SOCIAL,
                CourseType.LECTURE,
                CourseGrade.ALL,
                3, false, 35, 20,
            )

            // 전공 과목 (교양이 아님)
            val majorCourse = CourseFixture.createCourse(
                "데이터구조", "Data Structure", "COM101", "COM101001",
                CourseCollege.INFORMATION_TECHNOLOGY, CourseDepartment.COMPUTER_ENGINEERING,
                CourseClassification.MAJOR_CORE, CourseArea.MAJOR_CORE,
                CourseType.LECTURE,
                CourseGrade.SOPHOMORE,
                3, false, 50, 40,
            )

            // 스케줄 추가
            val schedule1 = CourseScheduleFixture.createCourseSchedule(
                coreHumanities1, CourseDay.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0),
            )
            val schedule2 = CourseScheduleFixture.createCourseSchedule(
                coreHumanities1, CourseDay.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(11, 0),
            )
            val schedule3 = CourseScheduleFixture.createCourseSchedule(
                coreForeignLanguage1, CourseDay.TUESDAY, LocalTime.of(13, 0), LocalTime.of(15, 0),
            )

            coreHumanities1.addCourseSchedule(schedule1)
            coreHumanities1.addCourseSchedule(schedule2)
            coreForeignLanguage1.addCourseSchedule(schedule3)

            // 교직 과목 1개
            val teaching = CourseFixture.createCourse(
                "교육학개론", "Introduction to Education", "TEA101", "TEA101001",
                CourseCollege.TEACHING, CourseDepartment.TEACHING,
                CourseClassification.TEACHING, CourseArea.TEACHING,
                CourseType.LECTURE,
                CourseGrade.ALL,
                2, false, 30, 10,
            )

            // 일반선택 과목 1개
            val generalElective = CourseFixture.createCourse(
                "자기설계세미나", "Self Designed Seminar", "GEL101", "GEL101001",
                CourseCollege.GENERAL_ELECTIVE, CourseDepartment.GENERAL_ELECTIVE,
                CourseClassification.GENERAL_ELECTIVE, CourseArea.GENERAL_ELECTIVE,
                CourseType.LECTURE,
                CourseGrade.ALL,
                1, false, 30, 10,
            )

            courseRepository.saveAll(
                listOf(
                    coreHumanities1, coreHumanities2,
                    coreForeignLanguage1, coreForeignLanguage2,
                    social,
                    teaching, generalElective,
                    majorCourse,
                )
            )
        }

        @Test
        fun 이수영역까지_넘기면_해당_영역의_과목만_반환된다() {
            //given
            val classificationCode = CourseClassification.CORE_LIBERAL_ARTS.code
            val areaCode = CourseArea.CORE_HUMANITIES.code

            //when
            val response = courseService.getGeneralEducationCourses(classificationCode, areaCode)

            //then
            assertThat(response.courseResponses).hasSize(2)
            assertThat(response.courseResponses)
                .extracting<String> { it.courseArea }
                .containsOnly("(핵심)인문")
        }

        @Test
        fun 이수구분만_넘기면_하위_이수영역_전체가_반환된다() {
            //given
            val classificationCode = CourseClassification.CORE_LIBERAL_ARTS.code

            //when
            val response = courseService.getGeneralEducationCourses(classificationCode, null)

            //then
            assertThat(response.courseResponses).hasSize(4)
            assertThat(response.courseResponses)
                .extracting<String> { it.courseArea }
                .containsOnly("(핵심)인문", "(핵심)외국어")
        }

        @Test
        fun 심화교양_사회_영역으로_조회하면_해당_영역의_과목만_반환된다() {
            //given
            val classificationCode = CourseClassification.ADVANCED_LIBERAL_ARTS.code
            val areaCode = CourseArea.SOCIAL.code

            //when
            val response = courseService.getGeneralEducationCourses(classificationCode, areaCode)

            //then
            assertThat(response.courseResponses).hasSize(1)
            assertThat(response.courseResponses)
                .extracting<String> { it.courseArea }
                .containsOnly("사회")
        }

        @Test
        fun 교직으로_조회하면_교직_과목이_반환된다() {
            //given
            val classificationCode = CourseClassification.TEACHING.code

            //when
            val response = courseService.getGeneralEducationCourses(classificationCode, null)

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.courseCode }
                .containsExactly("TEA101")
        }

        @Test
        fun 일반선택으로_조회하면_일반선택_과목이_반환된다() {
            //given
            val classificationCode = CourseClassification.GENERAL_ELECTIVE.code

            //when
            val response = courseService.getGeneralEducationCourses(classificationCode, null)

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.courseCode }
                .containsExactly("GEL101")
        }

        @Test
        fun 교양이_아닌_과목의_이수영역은_빈_문자열로_반환된다() {
            //given
            val classificationCode = CourseClassification.TEACHING.code

            //when
            val response = courseService.getGeneralEducationCourses(classificationCode, null)

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.courseArea }
                .containsOnly("")
        }

        @Test
        fun 스케줄이_있는_교양_과목은_요일순으로_정렬되어_반환된다() {
            //given
            val classificationCode = CourseClassification.CORE_LIBERAL_ARTS.code
            val areaCode = CourseArea.CORE_HUMANITIES.code

            //when
            val response = courseService.getGeneralEducationCourses(classificationCode, areaCode)

            //then
            val coreHumanities1 = response.courseResponses.first { it.courseCode == "GEN101" }
            assertThat(coreHumanities1.schedule).isEqualTo("월 1-2A (07-401) 수 1-2A (07-401)")
        }

        @Test
        fun 스케줄이_없는_교양_과목은_빈_문자열로_반환된다() {
            //given
            val classificationCode = CourseClassification.CORE_LIBERAL_ARTS.code
            val areaCode = CourseArea.CORE_HUMANITIES.code

            //when
            val response = courseService.getGeneralEducationCourses(classificationCode, areaCode)

            //then
            val coreHumanities2 = response.courseResponses.first { it.courseCode == "GEN102" }
            assertThat(coreHumanities2.schedule).isEmpty()
        }

        @Test
        fun 전공_과목은_교양_조회시_포함되지_않는다() {
            //given
            val classificationCode = CourseClassification.CORE_LIBERAL_ARTS.code

            //when
            val response = courseService.getGeneralEducationCourses(classificationCode, null)

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.courseCode }
                .isNotEmpty()
                .doesNotContain("COM101")
        }

        @Test
        fun 전공_이수구분으로_조회하면_예외가_발생한다() {
            //given
            val majorClassificationCode = CourseClassification.MAJOR_CORE.code

            //when & then
            assertThatThrownBy { courseService.getGeneralEducationCourses(majorClassificationCode, null) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_GENERAL_EDUCATION_CLASSIFICATION)
        }

        @Test
        fun 존재하지_않는_이수구분_코드로_조회하면_예외가_발생한다() {
            //given
            val invalidClassificationCode = "99"

            //when & then
            assertThatThrownBy { courseService.getGeneralEducationCourses(invalidClassificationCode, null) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_ENUM_TYPE)
        }

        @Test
        fun 이수구분에_속하지_않는_이수영역으로_조회하면_예외가_발생한다() {
            //given
            val classificationCode = CourseClassification.CORE_LIBERAL_ARTS.code
            val otherAreaCode = CourseArea.SOCIAL.code

            //when & then
            assertThatThrownBy { courseService.getGeneralEducationCourses(classificationCode, otherAreaCode) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_GENERAL_EDUCATION_AREA)
        }

        @Test
        fun 존재하지_않는_이수영역_코드로_조회하면_예외가_발생한다() {
            //given
            val classificationCode = CourseClassification.CORE_LIBERAL_ARTS.code
            val invalidAreaCode = "999"

            //when & then
            assertThatThrownBy { courseService.getGeneralEducationCourses(classificationCode, invalidAreaCode) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_GENERAL_EDUCATION_AREA)
        }
    }

    @Nested
    inner class 타학과_전공과목_조회_테스트 {
        @BeforeEach
        fun setUp() {
            // 수학과 과목들
            // 전학년 과목 2개
            val mathAllGrade1 = CourseFixture.createCourseWithDepartmentAndDetails(
                "미적분학", "Calculus", "MATH101", "MATH101001",
                CourseDepartment.MATHEMATICS,
                CourseGrade.ALL,
            )
            val mathAllGrade2 = CourseFixture.createCourseWithDepartmentAndDetails(
                "선형대수", "Linear Algebra", "MATH102", "MATH102001",
                CourseDepartment.MATHEMATICS,
                CourseGrade.ALL,
            )

            // 1학년 과목 2개
            val mathFreshman1 = CourseFixture.createCourseWithDepartmentAndDetails(
                "수학의이해", "Understanding Mathematics", "MATH201", "MATH201001",
                CourseDepartment.MATHEMATICS,
                CourseGrade.FRESHMAN,
            )
            val mathFreshman2 = CourseFixture.createCourseWithDepartmentAndDetails(
                "기초수학", "Basic Mathematics", "MATH202", "MATH202001",
                CourseDepartment.MATHEMATICS,
                CourseGrade.FRESHMAN,
            )

            // 2학년 과목 2개
            val mathSophomore1 = CourseFixture.createCourseWithDepartmentAndDetails(
                "해석학", "Analysis", "MATH301", "MATH301001",
                CourseDepartment.MATHEMATICS,
                CourseGrade.SOPHOMORE,
            )
            val mathSophomore2 = CourseFixture.createCourseWithDepartmentAndDetails(
                "정수론", "Number Theory", "MATH302", "MATH302001",
                CourseDepartment.MATHEMATICS,
                CourseGrade.SOPHOMORE,
            )

            // 컴퓨터공학부 과목 (다른 학과)
            val cseCourse = CourseFixture.createCourseWithDetails(
                "자료구조", "Data Structure", "CSE101", "CSE101001",
                CourseGrade.SOPHOMORE,
            )

            // 스케줄 추가
            val schedule1 = CourseScheduleFixture.createCourseSchedule(
                mathAllGrade1, CourseDay.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0),
            )
            val schedule2 = CourseScheduleFixture.createCourseSchedule(
                mathAllGrade1, CourseDay.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(11, 0),
            )
            val schedule3 = CourseScheduleFixture.createCourseSchedule(
                mathFreshman1, CourseDay.TUESDAY, LocalTime.of(13, 0), LocalTime.of(15, 0),
            )

            mathAllGrade1.addCourseSchedule(schedule1)
            mathAllGrade1.addCourseSchedule(schedule2)
            mathFreshman1.addCourseSchedule(schedule3)

            courseRepository.saveAll(
                listOf(
                    mathAllGrade1, mathAllGrade2,
                    mathFreshman1, mathFreshman2,
                    mathSophomore1, mathSophomore2,
                    cseCourse,
                )
            )
        }

        @Test
        fun 수학과_학과코드로_조회하면_수학과_과목만_반환된다() {
            //given
            val department = "MATHEMATICS"

            //when
            val response = courseService.getOtherDepartmentCourses(department)

            //then
            assertThat(response.courseResponses).hasSize(6)
            assertThat(response.courseResponses)
                .extracting<String> { it.department }
                .containsOnly("수학과")
        }

        @Test
        fun 전학년_1학년_2학년_순서로_정렬되어_조회된다() {
            //given
            val department = "MATHEMATICS"

            //when
            val response = courseService.getOtherDepartmentCourses(department)
            val grades = response.courseResponses.map { it.grade }

            //then
            assertThat(grades).containsExactly(
                "전학년", "전학년", // MATH101, MATH102
                "1학년", "1학년", // MATH201, MATH202
                "2학년", "2학년", // MATH301, MATH302
            )
        }

        @Test
        fun 스케줄이_있는_과목은_요일순으로_정렬되어_반환된다() {
            //given
            val department = "MATHEMATICS"

            //when
            val response = courseService.getOtherDepartmentCourses(department)

            //then
            // MATH101: [07-401:월(1-2A),수(1-2A)]
            val mathAllGrade1 = response.courseResponses.first { it.courseCode == "MATH101" }
            assertThat(mathAllGrade1.schedule).isEqualTo("월 1-2A (07-401) 수 1-2A (07-401)")

            // MATH201: [07-401:화(1-2A)]
            val mathFreshman1 = response.courseResponses.first { it.courseCode == "MATH201" }
            assertThat(mathFreshman1.schedule).isEqualTo("화 1-2A (07-401)")
        }

        @Test
        fun 다른_학과_과목은_조회되지_않는다() {
            //given
            val department = "MATHEMATICS"

            //when
            val response = courseService.getOtherDepartmentCourses(department)

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.courseCode }
                .isNotEmpty()
                .doesNotContain("CSE101")
        }

        @Test
        fun 잘못된_학과_코드로_조회하면_예외가_발생한다() {
            //given
            val invalidDepartment = "INVALID_DEPARTMENT"

            //when & then
            assertThatThrownBy { courseService.getOtherDepartmentCourses(invalidDepartment) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_ENUM_TYPE)
        }

        @Test
        fun 학과가_아닌_값으로_조회하면_예외가_발생한다() {
            //given
            val generalEducation = "GENERAL_EDUCATION"

            //when & then
            assertThatThrownBy { courseService.getOtherDepartmentCourses(generalEducation) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_DEPARTMENT)
        }

        @Test
        fun 연계전공으로_조회하면_예외가_발생한다() {
            //given
            val interdisciplinary = "LOGISTICS"

            //when & then
            assertThatThrownBy { courseService.getOtherDepartmentCourses(interdisciplinary) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_DEPARTMENT)
        }

        @Test
        fun 폐지된_학과로_조회하면_예외가_발생한다() {
            //given
            val legacy = "TRADE"

            //when & then
            assertThatThrownBy { courseService.getOtherDepartmentCourses(legacy) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_DEPARTMENT)
        }

        @Test
        fun 학부를_넘겨도_하위_전공_과목은_함께_조회되지_않는다() {
            //given
            val schoolCourse = CourseFixture.createCourseWithDepartmentAndDetails(
                "전자회로", "Circuits", "ELE101", "ELE101001",
                CourseDepartment.ELECTRONICS_ENGINEERING_SCHOOL, CourseGrade.SOPHOMORE,
            )
            val majorCourse = CourseFixture.createCourseWithDepartmentAndDetails(
                "반도체소자", "Semiconductor", "SEM101", "SEM101001",
                CourseDepartment.SEMICONDUCTOR_CONVERGENCE_MAJOR, CourseGrade.SOPHOMORE,
            )
            courseRepository.saveAll(listOf(schoolCourse, majorCourse))

            //when
            val response = courseService.getOtherDepartmentCourses(
                CourseDepartment.ELECTRONICS_ENGINEERING_SCHOOL.name
            )

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.courseCode }
                .containsExactly("ELE101")
                .doesNotContain("SEM101")
        }

        @Test
        fun 하위_전공을_넘기면_그_전공_과목만_조회된다() {
            //given
            val majorCourse = CourseFixture.createCourseWithDepartmentAndDetails(
                "반도체소자", "Semiconductor", "SEM101", "SEM101001",
                CourseDepartment.SEMICONDUCTOR_CONVERGENCE_MAJOR, CourseGrade.SOPHOMORE,
            )
            courseRepository.save(majorCourse)

            //when
            val response = courseService.getOtherDepartmentCourses(
                CourseDepartment.SEMICONDUCTOR_CONVERGENCE_MAJOR.name
            )

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.courseCode }
                .containsExactly("SEM101")
        }

        @Test
        fun 과목이_없는_학과는_빈_목록을_반환한다() {
            //given
            val emptyDepartment = "HUSS_EXCHANGE_UNIVERSITY"

            //when
            val response = courseService.getOtherDepartmentCourses(emptyDepartment)

            //then
            assertThat(response.courseResponses).isEmpty()
        }
    }

    @Nested
    inner class 학제간융합전공_과목_조회_테스트 {
        @BeforeEach
        fun setUp() {
            // 소셜데이터사이언스 연계전공 과목들
            // 전학년 과목 2개
            val socialDataAllGrade1 = CourseFixture.createCourseWithDepartmentAndDetails(
                "빅데이터분석", "Big Data Analysis", "SDS101", "SDS101001",
                CourseDepartment.SOCIAL_DATA_SCIENCE,
                CourseGrade.ALL,
            )
            val socialDataAllGrade2 = CourseFixture.createCourseWithDepartmentAndDetails(
                "데이터사이언스개론", "Intro to Data Science", "SDS102", "SDS102001",
                CourseDepartment.SOCIAL_DATA_SCIENCE,
                CourseGrade.ALL,
            )

            // 1학년 과목 2개
            val socialDataFreshman1 = CourseFixture.createCourseWithDepartmentAndDetails(
                "통계학기초", "Basic Statistics", "SDS201", "SDS201001",
                CourseDepartment.SOCIAL_DATA_SCIENCE,
                CourseGrade.FRESHMAN,
            )
            val socialDataFreshman2 = CourseFixture.createCourseWithDepartmentAndDetails(
                "프로그래밍입문", "Programming Intro", "SDS202", "SDS202001",
                CourseDepartment.SOCIAL_DATA_SCIENCE,
                CourseGrade.FRESHMAN,
            )

            // 2학년 과목 2개
            val socialDataSophomore1 = CourseFixture.createCourseWithDepartmentAndDetails(
                "머신러닝", "Machine Learning", "SDS301", "SDS301001",
                CourseDepartment.SOCIAL_DATA_SCIENCE,
                CourseGrade.SOPHOMORE,
            )
            val socialDataSophomore2 = CourseFixture.createCourseWithDepartmentAndDetails(
                "데이터시각화", "Data Visualization", "SDS302", "SDS302001",
                CourseDepartment.SOCIAL_DATA_SCIENCE,
                CourseGrade.SOPHOMORE,
            )

            // 미래자동차 연계전공 과목 (다른 연계전공)
            val futureAutoCourse = CourseFixture.createCourseWithDepartmentAndDetails(
                "자율주행개론", "Intro to Autonomous Driving", "FA101", "FA101001",
                CourseDepartment.FUTURE_AUTOMOBILE,
                CourseGrade.SOPHOMORE,
            )

            // 일반 학과 과목 (컴퓨터공학부)
            val cseCourse = CourseFixture.createCourseWithDetails(
                "자료구조", "Data Structure", "CSE101", "CSE101001",
                CourseGrade.SOPHOMORE,
            )

            // 스케줄 추가
            val schedule1 = CourseScheduleFixture.createCourseSchedule(
                socialDataAllGrade1, CourseDay.MONDAY, LocalTime.of(9, 0), LocalTime.of(11, 0),
            )
            val schedule2 = CourseScheduleFixture.createCourseSchedule(
                socialDataAllGrade1, CourseDay.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(11, 0),
            )
            val schedule3 = CourseScheduleFixture.createCourseSchedule(
                socialDataFreshman1, CourseDay.TUESDAY, LocalTime.of(13, 0), LocalTime.of(15, 0),
            )

            socialDataAllGrade1.addCourseSchedule(schedule1)
            socialDataAllGrade1.addCourseSchedule(schedule2)
            socialDataFreshman1.addCourseSchedule(schedule3)

            courseRepository.saveAll(
                listOf(
                    socialDataAllGrade1, socialDataAllGrade2,
                    socialDataFreshman1, socialDataFreshman2,
                    socialDataSophomore1, socialDataSophomore2,
                    futureAutoCourse,
                    cseCourse,
                )
            )
        }

        @Test
        fun 소셜데이터사이언스_학과코드로_조회하면_해당_연계전공_과목만_반환된다() {
            //given
            val department = "SOCIAL_DATA_SCIENCE"

            //when
            val response = courseService.getInterdisciplinaryMajorCourses(department)

            //then
            assertThat(response.courseResponses).hasSize(6)
            assertThat(response.courseResponses)
                .extracting<String> { it.courseCode }
                .containsExactlyInAnyOrder("SDS101", "SDS102", "SDS201", "SDS202", "SDS301", "SDS302")
        }

        @Test
        fun 전학년_1학년_2학년_순서로_정렬되어_조회된다() {
            //given
            val department = "SOCIAL_DATA_SCIENCE"

            //when
            val response = courseService.getInterdisciplinaryMajorCourses(department)
            val grades = response.courseResponses.map { it.grade }

            //then
            assertThat(grades).containsExactly(
                "전학년", "전학년", // SDS101, SDS102
                "1학년", "1학년", // SDS201, SDS202
                "2학년", "2학년", // SDS301, SDS302
            )
        }

        @Test
        fun 스케줄이_있는_과목은_요일순으로_정렬되어_반환된다() {
            //given
            val department = "SOCIAL_DATA_SCIENCE"

            //when
            val response = courseService.getInterdisciplinaryMajorCourses(department)

            //then
            // SDS101: [07-401:월(1-2A),수(1-2A)]
            val allGrade1 = response.courseResponses.first { it.courseCode == "SDS101" }
            assertThat(allGrade1.schedule).isEqualTo("월 1-2A (07-401) 수 1-2A (07-401)")

            // SDS201: [07-401:화(1-2A)]
            val freshman1 = response.courseResponses.first { it.courseCode == "SDS201" }
            assertThat(freshman1.schedule).isEqualTo("화 1-2A (07-401)")
        }

        @Test
        fun 스케줄이_없는_과목은_빈_문자열로_반환된다() {
            //given
            val department = "SOCIAL_DATA_SCIENCE"

            //when
            val response = courseService.getInterdisciplinaryMajorCourses(department)

            //then
            val allGrade2 = response.courseResponses.first { it.courseCode == "SDS102" }
            assertThat(allGrade2.schedule).isEmpty()
        }

        @Test
        fun 다른_연계전공_과목은_조회되지_않는다() {
            //given
            val department = "SOCIAL_DATA_SCIENCE"

            //when
            val response = courseService.getInterdisciplinaryMajorCourses(department)

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.courseCode }
                .isNotEmpty()
                .doesNotContain("FA101")
        }

        @Test
        fun 일반_학과_과목은_조회되지_않는다() {
            //given
            val department = "SOCIAL_DATA_SCIENCE"

            //when
            val response = courseService.getInterdisciplinaryMajorCourses(department)

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.courseCode }
                .isNotEmpty()
                .doesNotContain("CSE101")
        }

        @Test
        fun 잘못된_학과_코드로_조회하면_예외가_발생한다() {
            //given
            val invalidDepartment = "INVALID_DEPARTMENT"

            //when & then
            assertThatThrownBy { courseService.getInterdisciplinaryMajorCourses(invalidDepartment) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_ENUM_TYPE)
        }

        @Test
        fun 일반_학과_코드로_조회하면_예외가_발생한다() {
            //given
            val normalDepartment = "COMPUTER_ENGINEERING"

            //when & then
            assertThatThrownBy { courseService.getInterdisciplinaryMajorCourses(normalDepartment) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_INTERDISCIPLINARY_DEPARTMENT)
        }
    }

    @Nested
    inner class 목록_정렬_테스트 {
        @BeforeEach
        fun setUp() {
            // 같은 학년 안에서 이수구분과 학수번호로 갈리도록 일부러 뒤섞어 저장한다
            courseRepository.saveAll(
                listOf(
                    createComputerEngineeringCourse("SORT004", CourseClassification.MAJOR_CORE, CourseGrade.FRESHMAN),
                    createComputerEngineeringCourse("SORT002", CourseClassification.BASIC_LIBERAL_ARTS, CourseGrade.ALL),
                    createComputerEngineeringCourse("SORT003", CourseClassification.MAJOR_BASIC, CourseGrade.FRESHMAN),
                    createComputerEngineeringCourse("SORT001", CourseClassification.BASIC_LIBERAL_ARTS, CourseGrade.ALL),
                )
            )
        }

        @Test
        fun 학년_이수구분_학수번호_순으로_정렬된다() {
            //given

            //when
            val response = courseService.getOtherDepartmentCourses(
                CourseDepartment.COMPUTER_ENGINEERING.name
            )

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.code }
                .containsExactly("SORT001", "SORT002", "SORT003", "SORT004")
        }
    }

    @Nested
    inner class 원어강의_표기_테스트 {
        @BeforeEach
        fun setUp() {
            courseRepository.saveAll(
                listOf(
                    createEnglishCourse("ENG001001", true),
                    createEnglishCourse("KOR001001", false),
                )
            )
        }

        @Test
        fun 원어강의가_아니면_원어강의명은_빈_문자열로_내려간다() {
            //given

            //when
            val response = courseService.getOtherDepartmentCourses(
                CourseDepartment.COMPUTER_ENGINEERING.name
            )

            //then
            assertThat(response.courseResponses)
                .extracting(CourseResponse::code, CourseResponse::englishCourseName)
                .containsExactlyInAnyOrder(
                    tuple("ENG001001", "원어강의(EN)"),
                    tuple("KOR001001", ""),
                )
        }
    }

    @Nested
    inner class HUSS_과목_조회_테스트 {
        @BeforeEach
        fun setUp() {
            courseRepository.saveAll(
                listOf(
                    // HUSS 교과목은 학과가 여러 개에 걸쳐 있다
                    CourseFixture.createHussCourse(
                        "글로벌리더십", "Global Leadership", "HUSS001", "HUSS001001",
                        CourseDepartment.HUSS_OTHER_UNIVERSITY,
                    ),
                    CourseFixture.createHussCourse(
                        "포용사회의이해", "Understanding Inclusive Society", "HUSS002", "HUSS002001",
                        CourseDepartment.GLOBAL_TRADE_SERVICE,
                    ),
                    // 같은 학과의 非HUSS 과목. 섞이면 안 된다
                    CourseFixture.createCourseWithDepartmentAndDetails(
                        "무역실무", "Trade Practice", "GTS001", "GTS001001",
                        CourseDepartment.GLOBAL_TRADE_SERVICE, CourseGrade.SOPHOMORE,
                    ),
                )
            )
        }

        @Test
        fun 학과를_가리지_않고_HUSS_교과목만_반환된다() {
            //given

            //when
            val response = courseService.getHussCourses()

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.code }
                .containsExactlyInAnyOrder("HUSS001001", "HUSS002001")
        }
    }

    @Nested
    inner class 카테고리_조회_테스트 {
        @BeforeEach
        fun setUp() {
            courseRepository.saveAll(
                listOf(
                    // 전공핵심(31) - 전공핵심(34)
                    createCategoryCourse("CAT001001", CourseClassification.MAJOR_CORE, CourseArea.MAJOR_CORE),
                    // 기초교양(11) - 학문의기초(161), 같은 조합을 두 번 넣어 중복 제거를 확인한다
                    createCategoryCourse("CAT002001", CourseClassification.BASIC_LIBERAL_ARTS, CourseArea.ACADEMIC_FOUNDATION),
                    createCategoryCourse("CAT003001", CourseClassification.BASIC_LIBERAL_ARTS, CourseArea.ACADEMIC_FOUNDATION),
                    // 기초교양(11) - 기초과학·공학(162)
                    createCategoryCourse("CAT004001", CourseClassification.BASIC_LIBERAL_ARTS, CourseArea.BASIC_SCIENCE_ENGINEERING),
                    // 핵심교양(21) - (핵심)인문(172)
                    createCategoryCourse("CAT005001", CourseClassification.CORE_LIBERAL_ARTS, CourseArea.CORE_HUMANITIES),
                )
            )
        }

        @Test
        fun 이수구분별로_이수영역이_묶여서_코드_오름차순으로_반환된다() {
            //given

            //when
            val response = courseService.getCategories()

            //then
            assertThat(response.categoryResponses)
                .extracting(CourseCategoryResponse::code, CourseCategoryResponse::name)
                .containsExactly(
                    tuple("11", "기초교양"),
                    tuple("21", "핵심교양"),
                    tuple("31", "전공핵심"),
                )

            assertThat(response.categoryResponses[0].areaResponses)
                .extracting(CourseAreaResponse::code, CourseAreaResponse::name)
                .containsExactly(
                    tuple("161", "학문의기초"),
                    tuple("162", "기초과학·공학"),
                )
        }
    }

    @Nested
    inner class 년도_학기_조회_테스트 {
        @BeforeEach
        fun setUp() {
            courseRepository.saveAll(
                listOf(
                    CourseFixture.createCourseWithDetails("자료구조", "Data Structure", "TERM001", "TERM001001", CourseGrade.ALL),
                    CourseFixture.createCourseWithDetails("알고리즘", "Algorithm", "TERM002", "TERM002001", CourseGrade.ALL),
                )
            )
        }

        @Test
        fun 적재된_년도와_학기가_중복_없이_반환된다() {
            //given

            //when
            val response = courseService.getTerms()

            //then
            assertThat(response.termResponses)
                .extracting(CourseTermResponse::academicYear, CourseTermResponse::termCode, CourseTermResponse::termName)
                .containsExactly(tuple(2026, "20", "2학기"))
        }
    }

    @Nested
    inner class 연계전공_조회_테스트 {
        @BeforeEach
        fun setUp() {
            courseRepository.saveAll(
                listOf(
                    createInterdisciplinaryCourse("SDS001001", CourseDepartment.SOCIAL_DATA_SCIENCE),
                    createInterdisciplinaryCourse("LOG001001", CourseDepartment.LOGISTICS),
                    // 연계전공이 아닌 일반 학과. 목록에 섞이면 안 된다
                    CourseFixture.createCourseWithDepartmentAndDetails(
                        "미적분학", "Calculus", "MATH101", "MATH101001",
                        CourseDepartment.MATHEMATICS, CourseGrade.FRESHMAN,
                    ),
                )
            )
        }

        @Test
        fun 과목_적재_여부와_무관하게_연계전공_전건이_반환된다() {
            //given

            //when
            val response = courseService.getInterdisciplinaryMajors()

            //then
            assertThat(response.interdisciplinaryMajorResponses).hasSize(32)
            assertThat(response.interdisciplinaryMajorResponses)
                .extracting(InterdisciplinaryMajorResponse::code, InterdisciplinaryMajorResponse::name)
                .contains(
                    tuple("LOGISTICS", "물류학전공(연계)"),
                    tuple("SOCIAL_DATA_SCIENCE", "소셜데이터사이언스연계전공"),
                    tuple("ANTIBODY_ENGINEERING", "항체공학연계전공"),
                )
        }

        @Test
        fun 연계전공이_아닌_학과는_목록에_섞이지_않는다() {
            //given

            //when
            val response = courseService.getInterdisciplinaryMajors()

            //then
            assertThat(response.interdisciplinaryMajorResponses)
                .extracting<String> { it.name }
                .isNotEmpty()
                .doesNotContain("수학과", "교양", "HUSS(타대학)")
        }
    }

    private fun createComputerEngineeringCourse(
        haksuCode: String,
        classification: CourseClassification,
        grade: CourseGrade,
    ): Course {
        return CourseFixture.createCourse(
            "정렬대상", "Sort Target", haksuCode.substring(0, 7), haksuCode,
            CourseCollege.INFORMATION_TECHNOLOGY,
            CourseDepartment.COMPUTER_ENGINEERING,
            classification,
            CourseArea.MAJOR_CORE,
            CourseType.LECTURE,
            grade,
            3, false, 50, 30,
        )
    }

    private fun createEnglishCourse(
        haksuCode: String,
        isEnglishCourse: Boolean,
    ): Course {
        return CourseFixture.createCourse(
            "원어강의대상", "English Target", haksuCode.substring(0, 7), haksuCode,
            CourseCollege.INFORMATION_TECHNOLOGY,
            CourseDepartment.COMPUTER_ENGINEERING,
            CourseClassification.MAJOR_CORE,
            CourseArea.MAJOR_CORE,
            CourseType.LECTURE,
            CourseGrade.SOPHOMORE,
            3, isEnglishCourse, 50, 30,
        )
    }

    private fun createCategoryCourse(
        haksuCode: String,
        classification: CourseClassification,
        area: CourseArea,
    ): Course {
        return CourseFixture.createCourse(
            "카테고리대상", "Category Target", haksuCode.substring(0, 7), haksuCode,
            CourseCollege.GENERAL_EDUCATION,
            CourseDepartment.GENERAL_EDUCATION,
            classification,
            area,
            CourseType.LECTURE,
            CourseGrade.ALL,
            3, false, 50, 30,
        )
    }

    private fun createInterdisciplinaryCourse(
        haksuCode: String,
        department: CourseDepartment,
    ): Course {
        return CourseFixture.createCourse(
            "연계전공대상", "Interdisciplinary Target", haksuCode.substring(0, 7), haksuCode,
            department.courseCollege,
            department,
            CourseClassification.MAJOR_CORE,
            CourseArea.MAJOR_CORE,
            CourseType.LECTURE,
            CourseGrade.SOPHOMORE,
            3, false, 50, 30,
        )
    }

    @Nested
    inner class 폐강_강의_제외_테스트 {
        private val testStudentId = "202099999"
        private val testName = "폐강테스터"

        private var memberId = 0L

        @BeforeEach
        fun setUp() {
            val member = MemberFixture.createMember(
                testStudentId,
                testName,
                MemberDepartment.COMPUTER_ENGINEERING,
                MemberGrade.JUNIOR,
                AcademicStatus.ENROLLED,
                3.5,
            )
            memberRepository.save(member)
            memberId = member.id

            val activeCourse = CourseFixture.createCourseWithDepartmentAndDetails(
                "개설과목", "Active Course", "CSE5010", "CSE5010001",
                CourseDepartment.COMPUTER_ENGINEERING, CourseGrade.JUNIOR,
            )
            val closedCourse = CourseFixture.createCourseWithDepartmentAndDetails(
                "폐강과목", "Closed Course", "CSE5020", "CSE5020001",
                CourseDepartment.COMPUTER_ENGINEERING, CourseGrade.JUNIOR,
            )
            closedCourse.close()

            courseRepository.saveAll(listOf(activeCourse, closedCourse))
        }

        @Test
        fun 전공_조회에서_폐강_강의가_빠진다() {
            //when
            val response = courseService.getMajorCourses(memberId)

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.name }
                .containsExactly("개설과목")
        }

        @Test
        fun 타학과_조회에서_폐강_강의가_빠진다() {
            //when
            val response = courseService.getOtherDepartmentCourses(
                CourseDepartment.COMPUTER_ENGINEERING.name
            )

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.name }
                .containsExactly("개설과목")
        }

        @Test
        fun HUSS_조회에서_폐강_강의가_빠진다() {
            //given
            val activeHuss = CourseFixture.createHussCourse(
                "HUSS개설", "Active Huss", "CSE6010", "CSE6010001",
                CourseDepartment.COMPUTER_ENGINEERING,
            )
            val closedHuss = CourseFixture.createHussCourse(
                "HUSS폐강", "Closed Huss", "CSE6020", "CSE6020001",
                CourseDepartment.COMPUTER_ENGINEERING,
            )
            closedHuss.close()
            courseRepository.saveAll(listOf(activeHuss, closedHuss))

            //when
            val response = courseService.getHussCourses()

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.name }
                .containsExactly("HUSS개설")
        }

        @Test
        fun 교양_조회에서_폐강_강의가_빠진다() {
            //given
            val activeGeneral = CourseFixture.createCourse(
                "교양개설", "Active General", "GEN1010", "GEN1010001",
                CourseCollege.GENERAL_EDUCATION,
                CourseDepartment.COMPUTER_ENGINEERING,
                CourseClassification.CORE_LIBERAL_ARTS,
                CourseArea.CORE_HUMANITIES,
                CourseType.LECTURE,
                CourseGrade.ALL,
                3, false, 50, 0,
            )
            val closedGeneral = CourseFixture.createCourse(
                "교양폐강", "Closed General", "GEN1020", "GEN1020001",
                CourseCollege.GENERAL_EDUCATION,
                CourseDepartment.COMPUTER_ENGINEERING,
                CourseClassification.CORE_LIBERAL_ARTS,
                CourseArea.CORE_HUMANITIES,
                CourseType.LECTURE,
                CourseGrade.ALL,
                3, false, 50, 0,
            )
            closedGeneral.close()
            courseRepository.saveAll(listOf(activeGeneral, closedGeneral))

            //when
            val response = courseService.getGeneralEducationCourses(
                CourseClassification.CORE_LIBERAL_ARTS.code,
                CourseArea.CORE_HUMANITIES.code,
            )

            //then
            assertThat(response.courseResponses)
                .extracting<String> { it.name }
                .containsExactly("교양개설")
        }

        @Test
        fun 폐강_강의도_카테고리_목록에는_남는다() {
            //when
            val response = courseService.getCategories()

            //then
            assertThat(response.categoryResponses).isNotEmpty()
        }
    }

    @Nested
    inner class 학과_목록_조회_테스트 {
        @Test
        fun 과목_적재_여부와_무관하게_학과_전건이_반환된다() {
            //given

            //when
            val response = courseService.getDepartments()

            //then
            assertThat(response.departmentResponses).hasSize(76)
        }

        @Test
        fun 학과_코드와_이름이_함께_반환된다() {
            //given

            //when
            val response = courseService.getDepartments()

            //then
            assertThat(response.departmentResponses)
                .extracting(DepartmentResponse::code, DepartmentResponse::name)
                .contains(
                    tuple("COMPUTER_ENGINEERING", "컴퓨터공학부"),
                    tuple("SEMICONDUCTOR_CONVERGENCE_MAJOR", "반도체융합전공"),
                    tuple("HUSS_EXCHANGE_UNIVERSITY", "HUSS(교류대학)"),
                )
        }

        @Test
        fun 교양과_교직과_일선과_군사학은_학과_목록에_없다() {
            //given

            //when
            val response = courseService.getDepartments()

            //then
            assertThat(response.departmentResponses)
                .extracting<String> { it.name }
                .isNotEmpty()
                .doesNotContain("교양", "교직", "일선", "군사학")
        }

        @Test
        fun 연계전공은_학과_목록에_없다() {
            //given

            //when
            val response = courseService.getDepartments()

            //then
            assertThat(response.departmentResponses)
                .extracting<String> { it.name }
                .isNotEmpty()
                .doesNotContain("물류학전공(연계)", "항체공학연계전공")
        }

        @Test
        fun 폐지된_학과는_학과_목록에_없다() {
            //given

            //when
            val response = courseService.getDepartments()

            //then
            assertThat(response.departmentResponses)
                .extracting<String> { it.code }
                .isNotEmpty()
                .doesNotContain("TRADE")
        }

        @Test
        fun HUSS_두_건은_학과_목록에_있다() {
            //given

            //when
            val response = courseService.getDepartments()

            //then
            assertThat(response.departmentResponses)
                .extracting<String> { it.name }
                .contains("HUSS(타대학)", "HUSS포용사회이니셔티브학부")
        }
    }
}
