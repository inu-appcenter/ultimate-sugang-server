package uss.code.registration.service

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uss.code.course.domain.CourseArea
import uss.code.course.domain.CourseClassification
import uss.code.course.domain.CourseCollege
import uss.code.course.domain.CourseDay
import uss.code.course.domain.CourseDepartment
import uss.code.course.domain.CourseGrade
import uss.code.course.domain.CourseType
import uss.code.course.fixture.CourseFixture
import uss.code.course.fixture.CourseScheduleFixture
import uss.code.course.repository.CourseRepository
import uss.code.global.exception.domain.ExceptionCode.COURSE_CLOSED
import uss.code.global.exception.domain.ExceptionCode.COURSE_MAX_CAPACITY_EXCEEDED
import uss.code.global.exception.domain.ExceptionCode.COURSE_NOT_FOUND
import uss.code.global.exception.domain.ExceptionCode.COURSE_SCHEDULE_CONFLICT
import uss.code.global.exception.domain.ExceptionCode.COURSE_TYPE_LIMIT_EXCEEDED
import uss.code.global.exception.domain.ExceptionCode.CREDIT_LIMIT_EXCEEDED
import uss.code.global.exception.domain.ExceptionCode.DUPLICATE_SUBJECT_REGISTERED
import uss.code.global.exception.domain.ExceptionCode.MEMBER_NOT_FOUND
import uss.code.global.exception.domain.ExceptionCode.REGISTERED_COURSE_NOT_FOUND
import uss.code.global.exception.domain.ExceptionCode.REGISTRATION_CANCEL_CONFLICT
import uss.code.global.exception.domain.RestApiException
import uss.code.global.infra.IntegrationTest
import uss.code.member.fixture.MemberFixture
import uss.code.member.repository.MemberRepository
import uss.code.registration.dto.response.RegistrationCourseResponse
import uss.code.registration.dto.response.RegistrationCoursesResponse
import uss.code.registration.fixture.RegistrationFixture
import uss.code.registration.repository.RegistrationRepository
import java.time.LocalDateTime
import java.time.LocalTime

@IntegrationTest
class RegistrationServiceTest(
    private val registrationService: RegistrationService,

    private val registrationRepository: RegistrationRepository,
    private val memberRepository: MemberRepository,
    private val courseRepository: CourseRepository,

    private val entityManager: EntityManager,
) {
    @Nested
    inner class 수강신청_과목_조회_테스트 {
        private var testMemberId = 0L
        private var otherMemberId = 0L

        @BeforeEach
        fun setUp() {
            // 회원 생성
            val testMember = MemberFixture.createMember()
            val otherMember = MemberFixture.createMember()
            memberRepository.saveAll(listOf(testMember, otherMember))
            testMemberId = testMember.id
            otherMemberId = otherMember.id

            // 과목 생성
            val course1 = CourseFixture.createCourseWithDetails(
                "자료구조", "Data Structure", "CSE101", "CSE101001",
                CourseGrade.SOPHOMORE,
            )
            val course2 = CourseFixture.createCourseWithDetails(
                "알고리즘", "Algorithm", "CSE201", "CSE201001",
                CourseGrade.SOPHOMORE,
            )
            val course3 = CourseFixture.createCourseWithDetails(
                "데이터베이스", "Database", "CSE301", "CSE301001",
                CourseGrade.JUNIOR,
            )

            // 스케줄 추가
            val schedule1 = CourseScheduleFixture.createCourseSchedule(
                course1, CourseDay.MONDAY, LocalTime.of(13, 0), LocalTime.of(15, 0),
            )
            val schedule2 = CourseScheduleFixture.createCourseSchedule(
                course1, CourseDay.WEDNESDAY, LocalTime.of(13, 0), LocalTime.of(15, 0),
            )
            val schedule3 = CourseScheduleFixture.createCourseSchedule(
                course2, CourseDay.TUESDAY, LocalTime.of(9, 0), LocalTime.of(11, 0),
            )

            course1.addCourseSchedule(schedule1)
            course1.addCourseSchedule(schedule2)
            course2.addCourseSchedule(schedule3)

            courseRepository.saveAll(listOf(course1, course2, course3))

            // 수강신청 생성
            // testMember: course1, course2, course3
            val registration1 = RegistrationFixture.createRegistration(
                testMember, course1, LocalDateTime.now().minusDays(3),
            )
            val registration2 = RegistrationFixture.createRegistration(
                testMember, course2, LocalDateTime.now().minusDays(2),
            )
            val registration3 = RegistrationFixture.createRegistration(
                testMember, course3, LocalDateTime.now().minusDays(1),
            )

            // otherMember: course1, course2 (같은 과목)
            val registration4 = RegistrationFixture.createRegistration(otherMember, course1)
            val registration5 = RegistrationFixture.createRegistration(otherMember, course2)

            registrationRepository.saveAll(
                listOf(
                    registration1, registration2, registration3, registration4, registration5,
                )
            )
        }

        @Test
        fun 회원의_수강신청_과목을_조회하면_성공한다() {
            //given

            //when
            val response = registrationService.getRegistrationCourse(testMemberId)

            //then
            assertThat(response.registrationCourseResponses).hasSize(3)
        }

        @Test
        fun 수강신청한_과목이_올바르게_조회된다() {
            //given

            //when
            val response = registrationService.getRegistrationCourse(testMemberId)

            //then
            assertThat(response.registrationCourseResponses)
                .extracting<String> { it.courseResponse.courseCode }
                .containsExactlyInAnyOrder("CSE101", "CSE201", "CSE301")
        }

        @Test
        fun 다른_회원의_수강신청은_조회되지_않는다() {
            //given

            //when
            val response = registrationService.getRegistrationCourse(testMemberId)

            //then
            // testMember는 3개의 과목만 조회되어야 함
            assertThat(response.registrationCourseResponses).hasSize(3)

            // otherMember 조회
            val otherResponse = registrationService.getRegistrationCourse(otherMemberId)
            assertThat(otherResponse.registrationCourseResponses).hasSize(2)
            assertThat(otherResponse.registrationCourseResponses)
                .extracting<String> { it.courseResponse.courseCode }
                .containsExactlyInAnyOrder("CSE101", "CSE201")
        }

        @Test
        fun 스케줄이_있는_과목은_요일순으로_정렬되어_반환된다() {
            //given

            //when
            val response = registrationService.getRegistrationCourse(testMemberId)

            //then
            // CSE101: [07-401:월(1-2A),수(1-2A)]
            val course1 = response.registrationCourseResponses.first { it.courseResponse.courseCode == "CSE101" }
            assertThat(course1.courseResponse.schedule).isEqualTo("월 1-2A (07-401) 수 1-2A (07-401)")

            // CSE201: [07-401:화(1-2A)]
            val course2 = response.registrationCourseResponses.first { it.courseResponse.courseCode == "CSE201" }
            assertThat(course2.courseResponse.schedule).isEqualTo("화 1-2A (07-401)")
        }

        @Test
        fun 스케줄이_없는_과목은_빈_문자열로_반환된다() {
            //given

            //when
            val response = registrationService.getRegistrationCourse(testMemberId)

            //then
            val course3 = response.registrationCourseResponses.first { it.courseResponse.courseCode == "CSE301" }
            assertThat(course3.courseResponse.schedule).isEmpty()
        }

        @Test
        fun 과목_정보가_올바르게_매핑된다() {
            //given

            //when
            val response = registrationService.getRegistrationCourse(testMemberId)

            //then
            val course1 = response.registrationCourseResponses.first { it.courseResponse.courseCode == "CSE101" }

            assertThat(course1.courseResponse.name).isEqualTo("자료구조")
            assertThat(course1.courseResponse.nameEn).isEqualTo("Data Structure")
            assertThat(course1.courseResponse.code).isEqualTo("CSE101001")
        }

        @Test
        fun 수강신청_내역이_없으면_빈_리스트가_반환된다() {
            //given
            val emptyMember = MemberFixture.createMember()
            memberRepository.save(emptyMember)

            //when
            val response = registrationService.getRegistrationCourse(emptyMember.id)

            //then
            assertThat(response.registrationCourseResponses).isEmpty()
        }
    }

    @Nested
    inner class 수강신청_테스트 {
        private var testMemberId = 0L
        private var course1Id = 0L

        @BeforeEach
        fun setUp() {
            // 회원 생성
            val testMember = MemberFixture.createMember()
            memberRepository.save(testMember)
            testMemberId = testMember.id

            // 과목 생성
            val course1 = CourseFixture.createCourseWithDetails(
                "자료구조", "Data Structure", "CSE101", "CSE101001",
                CourseGrade.SOPHOMORE,
            )
            val course2 = CourseFixture.createCourseWithDetails(
                "알고리즘", "Algorithm", "CSE201", "CSE201001",
                CourseGrade.SOPHOMORE,
            )

            // 스케줄 추가
            val schedule1 = CourseScheduleFixture.createCourseSchedule(
                course1, CourseDay.MONDAY, LocalTime.of(13, 0), LocalTime.of(15, 0),
            )
            course1.addCourseSchedule(schedule1)

            val schedule2 = CourseScheduleFixture.createCourseSchedule(
                course2, CourseDay.TUESDAY, LocalTime.of(9, 0), LocalTime.of(11, 0),
            )
            course2.addCourseSchedule(schedule2)

            courseRepository.saveAll(listOf(course1, course2))
            course1Id = course1.id
        }

        @Test
        fun 수강신청에_성공한다() {
            //given

            //when
            registrationService.registerCourse(testMemberId, course1Id)

            //then
            val registrations = registrationRepository.findByMemberId(testMemberId)
            assertThat(registrations).hasSize(1)
            assertThat(registrations[0].course.id).isEqualTo(course1Id)
        }

        @Test
        fun 존재하지_않는_회원이_수강신청하면_예외가_발생한다() {
            //given
            val nonExistentMemberId = 99999L

            //when & then
            assertThatThrownBy { registrationService.registerCourse(nonExistentMemberId, course1Id) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", MEMBER_NOT_FOUND)
        }

        @Test
        fun 존재하지_않는_과목을_수강신청하면_예외가_발생한다() {
            //given
            val nonExistentCourseId = 99999L

            //when & then
            assertThatThrownBy { registrationService.registerCourse(testMemberId, nonExistentCourseId) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", COURSE_NOT_FOUND)
        }

        @Test
        fun 이미_신청한_과목을_다시_신청하면_시간표_중복으로_막힌다() {
            //given
            registrationService.registerCourse(testMemberId, course1Id)

            //when & then
            assertThatThrownBy { registrationService.registerCourse(testMemberId, course1Id) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", COURSE_SCHEDULE_CONFLICT)
        }

        @Test
        fun 시간표가_겹치는_과목을_신청하면_예외가_발생한다() {
            //given
            // course1을 먼저 신청 (월 13:00-15:00)
            registrationService.registerCourse(testMemberId, course1Id)

            // 겹치는 시간대의 새 과목 생성 (월 14:00-16:00)
            val conflictCourse = CourseFixture.createCourseWithDetails(
                "운영체제", "Operating System", "CSE301", "CSE301001",
                CourseGrade.JUNIOR,
            )
            val conflictSchedule = CourseScheduleFixture.createCourseSchedule(
                conflictCourse, CourseDay.MONDAY, LocalTime.of(14, 0), LocalTime.of(16, 0),
            )
            conflictCourse.addCourseSchedule(conflictSchedule)
            courseRepository.save(conflictCourse)

            //when & then
            assertThatThrownBy { registrationService.registerCourse(testMemberId, conflictCourse.id) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", COURSE_SCHEDULE_CONFLICT)
        }

        @Test
        fun 시간표가_겹치지_않으면_신청할_수_있다() {
            //given
            // course1을 먼저 신청 (월 13:00-15:00)
            registrationService.registerCourse(testMemberId, course1Id)

            // 겹치지 않는 시간대의 새 과목 생성 (월 15:00-17:00)
            val nonConflictCourse = CourseFixture.createCourseWithDetails(
                "운영체제", "Operating System", "CSE301", "CSE301001",
                CourseGrade.JUNIOR,
            )
            val nonConflictSchedule = CourseScheduleFixture.createCourseSchedule(
                nonConflictCourse, CourseDay.MONDAY, LocalTime.of(15, 0), LocalTime.of(17, 0),
            )
            nonConflictCourse.addCourseSchedule(nonConflictSchedule)
            courseRepository.save(nonConflictCourse)

            //when
            registrationService.registerCourse(testMemberId, nonConflictCourse.id)

            //then
            val registrations = registrationRepository.findByMemberId(testMemberId)
            assertThat(registrations).hasSize(2)
        }

        @Test
        fun OCU_과목이_2개_있으면_신청할_수_없다() {
            //given
            // OCU 과목 2개 생성 및 신청
            val ocu1 = CourseFixture.createCourse(
                "OCU과목1", "OCU Course 1", "OCU001", "OCU001001",
                CourseFixture.createCourse().college,
                CourseFixture.createCourse().department,
                CourseClassification.MAJOR_CORE,
                CourseFixture.createCourse().area,
                CourseType.OCU,
                CourseGrade.SOPHOMORE,
                3, false, 50, 30,
            )
            val ocu2 = CourseFixture.createCourse(
                "OCU과목2", "OCU Course 2", "OCU002", "OCU002001",
                CourseFixture.createCourse().college,
                CourseFixture.createCourse().department,
                CourseClassification.MAJOR_CORE,
                CourseFixture.createCourse().area,
                CourseType.OCU,
                CourseGrade.SOPHOMORE,
                3, false, 50, 30,
            )
            val ocu3 = CourseFixture.createCourse(
                "OCU과목3", "OCU Course 3", "OCU003", "OCU003001",
                CourseFixture.createCourse().college,
                CourseFixture.createCourse().department,
                CourseClassification.MAJOR_CORE,
                CourseFixture.createCourse().area,
                CourseType.OCU,
                CourseGrade.SOPHOMORE,
                3, false, 50, 30,
            )

            courseRepository.saveAll(listOf(ocu1, ocu2, ocu3))

            val member = memberRepository.findById(testMemberId).orElseThrow()
            val registration1 = RegistrationFixture.createRegistration(member, ocu1)
            val registration2 = RegistrationFixture.createRegistration(member, ocu2)
            registrationRepository.saveAll(listOf(registration1, registration2))

            //when & then
            assertThatThrownBy { registrationService.registerCourse(testMemberId, ocu3.id) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", COURSE_TYPE_LIMIT_EXCEEDED)
        }

        @Test
        fun OCU_과목이_1개_있으면_신청할_수_있다() {
            //given
            // OCU 과목 1개 생성 및 신청
            val ocu1 = CourseFixture.createCourse(
                "OCU과목1", "OCU Course 1", "OCU001", "OCU001001",
                CourseFixture.createCourse().college,
                CourseFixture.createCourse().department,
                CourseClassification.MAJOR_CORE,
                CourseFixture.createCourse().area,
                CourseType.OCU,
                CourseGrade.SOPHOMORE,
                3, false, 50, 30,
            )
            val ocu2 = CourseFixture.createCourse(
                "OCU과목2", "OCU Course 2", "OCU002", "OCU002001",
                CourseFixture.createCourse().college,
                CourseFixture.createCourse().department,
                CourseClassification.MAJOR_CORE,
                CourseFixture.createCourse().area,
                CourseType.OCU,
                CourseGrade.SOPHOMORE,
                3, false, 50, 30,
            )

            courseRepository.saveAll(listOf(ocu1, ocu2))

            val member = memberRepository.findById(testMemberId).orElseThrow()
            val registration1 = RegistrationFixture.createRegistration(member, ocu1)
            registrationRepository.save(registration1)

            //when
            registrationService.registerCourse(testMemberId, ocu2.id)

            //then
            val registrations = registrationRepository.findByMemberId(testMemberId)
            assertThat(registrations).hasSize(2)
        }

        @Test
        fun K_MOOC_과목이_1개_있으면_신청할_수_없다() {
            //given
            // K-MOOC 과목 1개 생성 및 신청
            val kMooc1 = CourseFixture.createCourse(
                "K-MOOC과목1", "K-MOOC Course 1", "KMOOC001", "KMOOC001001",
                CourseFixture.createCourse().college,
                CourseFixture.createCourse().department,
                CourseClassification.MAJOR_CORE,
                CourseFixture.createCourse().area,
                CourseType.K_MOOC,
                CourseGrade.SOPHOMORE,
                3, false, 50, 30,
            )
            val kMooc2 = CourseFixture.createCourse(
                "K-MOOC과목2", "K-MOOC Course 2", "KMOOC002", "KMOOC002001",
                CourseFixture.createCourse().college,
                CourseFixture.createCourse().department,
                CourseClassification.MAJOR_CORE,
                CourseFixture.createCourse().area,
                CourseType.K_MOOC,
                CourseGrade.SOPHOMORE,
                3, false, 50, 30,
            )

            courseRepository.saveAll(listOf(kMooc1, kMooc2))

            val member = memberRepository.findById(testMemberId).orElseThrow()
            val registration1 = RegistrationFixture.createRegistration(member, kMooc1)
            registrationRepository.save(registration1)

            //when & then
            assertThatThrownBy { registrationService.registerCourse(testMemberId, kMooc2.id) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", COURSE_TYPE_LIMIT_EXCEEDED)
        }

        @Test
        fun 일반_과목은_타입_제한이_없다() {
            //given
            // 일반 과목 여러 개 신청
            for (i in 0 until 5) {
                val course = CourseFixture.createCourseWithDetails(
                    "과목$i", "Course$i", "CSE30$i", "CSE30${i}001",
                    CourseGrade.SOPHOMORE,
                )
                courseRepository.save(course)

                val member = memberRepository.findById(testMemberId).orElseThrow()
                val registration = RegistrationFixture.createRegistration(member, course)
                registrationRepository.save(registration)
            }

            //when
            registrationService.registerCourse(testMemberId, course1Id)

            //then
            val registrations = registrationRepository.findByMemberId(testMemberId)
            assertThat(registrations).hasSize(6)
        }

        @Test
        fun 수강_정원이_마감된_과목은_신청할_수_없다() {
            //given
            // 정원이 가득 찬 과목 생성 (maxCapacity: 2, currentEnrollment: 2)
            val fullCourse = CourseFixture.createCourse(
                "정원마감과목", "Full Course", "CSE999", "CSE999001",
                CourseFixture.createCourse().college,
                CourseFixture.createCourse().department,
                CourseClassification.MAJOR_CORE,
                CourseFixture.createCourse().area,
                CourseType.LECTURE,
                CourseGrade.SOPHOMORE,
                3, false, 2, 2,
            )
            courseRepository.save(fullCourse)

            //when & then
            assertThatThrownBy { registrationService.registerCourse(testMemberId, fullCourse.id) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", COURSE_MAX_CAPACITY_EXCEEDED)
        }

        @Test
        fun 학점_제한을_초과하면_신청할_수_없다() {
            //given
            val member = memberRepository.findById(testMemberId).orElseThrow()
            val maxCredit = member.maxCredit // 기본 GPA 3.5 = 21학점

            // 이미 21학점 신청한 상태로 만들기 (7과목 * 3학점)
            val course3Credit1 = CourseFixture.createCourse(
                "과목1", "Course1", "CSE301", "CSE301001",
                CourseFixture.createCourse().college,
                CourseFixture.createCourse().department,
                CourseClassification.MAJOR_CORE,
                CourseFixture.createCourse().area,
                CourseType.LECTURE,
                CourseGrade.SOPHOMORE,
                3, false, 50, 30,
            )
            val course3Credit2 = CourseFixture.createCourse(
                "과목2", "Course2", "CSE302", "CSE302001",
                CourseFixture.createCourse().college,
                CourseFixture.createCourse().department,
                CourseClassification.MAJOR_CORE,
                CourseFixture.createCourse().area,
                CourseType.LECTURE,
                CourseGrade.SOPHOMORE,
                3, false, 50, 30,
            )
            val course3Credit3 = CourseFixture.createCourse(
                "과목3", "Course3", "CSE303", "CSE303001",
                CourseFixture.createCourse().college,
                CourseFixture.createCourse().department,
                CourseClassification.MAJOR_CORE,
                CourseFixture.createCourse().area,
                CourseType.LECTURE,
                CourseGrade.SOPHOMORE,
                3, false, 50, 30,
            )
            val course3Credit4 = CourseFixture.createCourse(
                "과목4", "Course4", "CSE304", "CSE304001",
                CourseFixture.createCourse().college,
                CourseFixture.createCourse().department,
                CourseClassification.MAJOR_CORE,
                CourseFixture.createCourse().area,
                CourseType.LECTURE,
                CourseGrade.SOPHOMORE,
                3, false, 50, 30,
            )
            val course3Credit5 = CourseFixture.createCourse(
                "과목5", "Course5", "CSE305", "CSE305001",
                CourseFixture.createCourse().college,
                CourseFixture.createCourse().department,
                CourseClassification.MAJOR_CORE,
                CourseFixture.createCourse().area,
                CourseType.LECTURE,
                CourseGrade.SOPHOMORE,
                3, false, 50, 30,
            )
            val course3Credit6 = CourseFixture.createCourse(
                "과목6", "Course6", "CSE306", "CSE306001",
                CourseFixture.createCourse().college,
                CourseFixture.createCourse().department,
                CourseClassification.MAJOR_CORE,
                CourseFixture.createCourse().area,
                CourseType.LECTURE,
                CourseGrade.SOPHOMORE,
                3, false, 50, 30,
            )
            val course3Credit7 = CourseFixture.createCourse(
                "과목7", "Course7", "CSE307", "CSE307001",
                CourseFixture.createCourse().college,
                CourseFixture.createCourse().department,
                CourseClassification.MAJOR_CORE,
                CourseFixture.createCourse().area,
                CourseType.LECTURE,
                CourseGrade.SOPHOMORE,
                3, false, 50, 30,
            )

            courseRepository.saveAll(
                listOf(
                    course3Credit1, course3Credit2, course3Credit3,
                    course3Credit4, course3Credit5, course3Credit6, course3Credit7,
                )
            )

            val reg1 = RegistrationFixture.createRegistration(member, course3Credit1)
            val reg2 = RegistrationFixture.createRegistration(member, course3Credit2)
            val reg3 = RegistrationFixture.createRegistration(member, course3Credit3)
            val reg4 = RegistrationFixture.createRegistration(member, course3Credit4)
            val reg5 = RegistrationFixture.createRegistration(member, course3Credit5)
            val reg6 = RegistrationFixture.createRegistration(member, course3Credit6)
            val reg7 = RegistrationFixture.createRegistration(member, course3Credit7)
            registrationRepository.saveAll(listOf(reg1, reg2, reg3, reg4, reg5, reg6, reg7)) // 총 21학점

            // 3학점 과목 추가 시도 (21 + 3 = 24 > 21)
            val extraCourse = CourseFixture.createCourse(
                "추가과목", "Extra Course", "CSE308", "CSE308001",
                CourseFixture.createCourse().college,
                CourseFixture.createCourse().department,
                CourseClassification.MAJOR_CORE,
                CourseFixture.createCourse().area,
                CourseType.LECTURE,
                CourseGrade.SOPHOMORE,
                3, false, 50, 30,
            )
            courseRepository.save(extraCourse)

            //when & then
            assertThatThrownBy { registrationService.registerCourse(testMemberId, extraCourse.id) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", CREDIT_LIMIT_EXCEEDED)
        }

        @Test
        fun 학점_제한_내에서는_신청할_수_있다() {
            //given
            val member = memberRepository.findById(testMemberId).orElseThrow()
            val maxCredit = member.maxCredit // 기본 GPA 3.5 = 21학점

            // 이미 15학점 신청한 상태로 만들기 (5과목 * 3학점)
            for (i in 0 until 5) {
                val course = CourseFixture.createCourse(
                    "과목$i", "Course$i", "CSE30$i", "CSE30${i}001",
                    CourseFixture.createCourse().college,
                    CourseFixture.createCourse().department,
                    CourseClassification.MAJOR_CORE,
                    CourseFixture.createCourse().area,
                    CourseType.LECTURE,
                    CourseGrade.SOPHOMORE,
                    3, false, 50, 30,
                )
                courseRepository.save(course)
                val registration = RegistrationFixture.createRegistration(member, course)
                registrationRepository.save(registration)
            }
            // 현재 15학점, 3학점 과목 추가 시 18학점 (21학점 이하)

            //when
            registrationService.registerCourse(testMemberId, course1Id) // 3학점

            //then
            val registrations = registrationRepository.findByMemberId(testMemberId)
            assertThat(registrations).hasSize(6)

            val totalCredits = registrations.sumOf { it.course.credits }
            assertThat(totalCredits).isEqualTo(18)
            assertThat(totalCredits).isLessThanOrEqualTo(maxCredit)
        }
    }

    @Nested
    inner class 수강신청_삭제_테스트 {
        private var testMemberId = 0L
        private var otherMemberId = 0L
        private var course1Id = 0L
        private var course2Id = 0L
        private var course3Id = 0L

        @BeforeEach
        fun setUp() {
            // 회원 생성
            val testMember = MemberFixture.createMember()
            val otherMember = MemberFixture.createMember()
            memberRepository.saveAll(listOf(testMember, otherMember))
            testMemberId = testMember.id
            otherMemberId = otherMember.id

            // 과목 생성
            val course1 = CourseFixture.createCourseWithDetails(
                "자료구조", "Data Structure", "CSE101", "CSE101001",
                CourseGrade.SOPHOMORE,
            )
            val course2 = CourseFixture.createCourseWithDetails(
                "알고리즘", "Algorithm", "CSE201", "CSE201001",
                CourseGrade.SOPHOMORE,
            )
            val course3 = CourseFixture.createCourseWithDetails(
                "데이터베이스", "Database", "CSE301", "CSE301001",
                CourseGrade.JUNIOR,
            )

            courseRepository.saveAll(listOf(course1, course2, course3))
            course1Id = course1.id
            course2Id = course2.id
            course3Id = course3.id

            // 수강신청 생성
            // testMember: course1, course2
            val registration1 = RegistrationFixture.createRegistration(testMember, course1)
            val registration2 = RegistrationFixture.createRegistration(testMember, course2)

            // otherMember: course3 (testMember와 겹치지 않음)
            val registration3 = RegistrationFixture.createRegistration(otherMember, course3)

            registrationRepository.saveAll(listOf(registration1, registration2, registration3))
        }

        @Test
        fun 수강신청_내역에서_과목을_삭제하면_성공한다() {
            //given

            //when
            registrationService.deleteRegisteredCourse(testMemberId, course1Id)

            //then
            val registrations = registrationRepository.findByMemberId(testMemberId)
            assertThat(registrations).hasSize(1)
            assertThat(registrations)
                .extracting<Long> { it.course.id }
                .containsExactly(course2Id)
        }

        @Test
        fun 수강신청_삭제_후_다른_회원의_수강신청은_영향받지_않는다() {
            //given

            //when
            registrationService.deleteRegisteredCourse(testMemberId, course1Id)

            //then
            // otherMember의 수강신청은 그대로 (course3)
            val otherRegistrations = registrationRepository.findByMemberId(otherMemberId)
            assertThat(otherRegistrations).hasSize(1)
            assertThat(otherRegistrations[0].course.id).isEqualTo(course3Id)
        }

        @Test
        fun 존재하지_않는_수강신청을_삭제하면_예외가_발생한다() {
            //given
            val nonExistentCourseId = 99999L

            //when & then
            assertThatThrownBy { registrationService.deleteRegisteredCourse(testMemberId, nonExistentCourseId) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", REGISTERED_COURSE_NOT_FOUND)
        }

        @Test
        fun 다른_회원의_수강신청을_삭제하면_예외가_발생한다() {
            //given
            // course3은 otherMember만 수강신청 함

            //when & then
            // testMember가 otherMember의 수강신청(course3) 삭제 시도
            assertThatThrownBy { registrationService.deleteRegisteredCourse(testMemberId, course3Id) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", REGISTERED_COURSE_NOT_FOUND)

            // otherMember의 수강신청은 그대로
            val otherRegistrations = registrationRepository.findByMemberId(otherMemberId)
            assertThat(otherRegistrations).hasSize(1)
            assertThat(otherRegistrations[0].course.id).isEqualTo(course3Id)
        }

        @Test
        fun 모든_수강신청을_삭제할_수_있다() {
            //given

            //when
            registrationService.deleteRegisteredCourse(testMemberId, course1Id)
            registrationService.deleteRegisteredCourse(testMemberId, course2Id)

            //then
            val registrations = registrationRepository.findByMemberId(testMemberId)
            assertThat(registrations).isEmpty()
        }

        @Test
        fun 수강신청을_삭제하면_현재_수강인원이_1_줄어든다() {
            //given
            val before = courseRepository.findById(course1Id).orElseThrow().currentEnrollment

            //when
            registrationService.deleteRegisteredCourse(testMemberId, course1Id)

            //then
            // 감소는 벌크 UPDATE라 영속성 컨텍스트의 Course는 갱신되지 않는다. 비우고 다시 읽는다.
            entityManager.clear()
            val after = courseRepository.findById(course1Id).orElseThrow().currentEnrollment
            assertThat(after).isEqualTo(before - 1)
        }

        @Test
        fun 현재_수강인원이_0인_과목은_취소할_수_없다() {
            //given
            // 신청 내역은 남아 있는데 수강인원이 0인, 이미 어긋난 상태를 만든다
            val zeroEnrollmentCourse = CourseFixture.createCourse(
                "인원0과목", "Zero Enrollment Course", "CSE998", "CSE998001",
                CourseFixture.createCourse().college,
                CourseFixture.createCourse().department,
                CourseClassification.MAJOR_CORE,
                CourseFixture.createCourse().area,
                CourseType.LECTURE,
                CourseGrade.SOPHOMORE,
                3, false, 50, 0,
            )
            courseRepository.save(zeroEnrollmentCourse)

            val member = memberRepository.findById(testMemberId).orElseThrow()
            registrationRepository.save(RegistrationFixture.createRegistration(member, zeroEnrollmentCourse))

            //when & then
            assertThatThrownBy { registrationService.deleteRegisteredCourse(testMemberId, zeroEnrollmentCourse.id) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", REGISTRATION_CANCEL_CONFLICT)
        }
    }

    @Nested
    inner class 폐강_강의_신청_테스트 {
        private var testMemberId = 0L
        private var closedCourseId = 0L

        @BeforeEach
        fun setUp() {
            val member = MemberFixture.createMember()
            memberRepository.save(member)
            testMemberId = member.id

            val closedCourse = CourseFixture.createCourse()
            closedCourse.close()
            courseRepository.save(closedCourse)
            closedCourseId = closedCourse.id
        }

        @Test
        fun 폐강된_강의는_신청할_수_없다() {
            //when & then
            assertThatThrownBy { registrationService.registerCourse(testMemberId, closedCourseId) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", COURSE_CLOSED)
        }

        @Test
        fun 폐강_검증은_정원_검증보다_먼저_수행한다() {
            //given
            val fullAndClosedCourse = CourseFixture.createCourse(
                "정원마감폐강", "Closed And Full", "CSE4010", "CSE4010001",
                CourseCollege.INFORMATION_TECHNOLOGY,
                CourseDepartment.COMPUTER_ENGINEERING,
                CourseClassification.MAJOR_CORE,
                CourseArea.MAJOR_CORE,
                CourseType.LECTURE,
                CourseGrade.SOPHOMORE,
                3, false, 30, 30,
            )
            fullAndClosedCourse.close()
            courseRepository.save(fullAndClosedCourse)

            //when & then
            assertThatThrownBy { registrationService.registerCourse(testMemberId, fullAndClosedCourse.id) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", COURSE_CLOSED)
        }

        @Test
        fun 이미_신청한_강의가_폐강돼도_취소할_수_있다() {
            //given
            val course = CourseFixture.createCourseWithDetails(
                "운영체제", "Operating System", "CSE3010", "CSE3010001", CourseGrade.JUNIOR,
            )
            courseRepository.save(course)
            registrationService.registerCourse(testMemberId, course.id)

            course.close()
            courseRepository.save(course)

            //when
            registrationService.deleteRegisteredCourse(testMemberId, course.id)

            //then
            assertThat(registrationRepository.findByMemberIdAndCourseId(testMemberId, course.id)).isNull()
        }
    }

    @Nested
    inner class 동일_과목명_신청_차단_테스트 {
        private var testMemberId = 0L
        private var otherSectionId = 0L
        private var otherSubjectId = 0L

        @BeforeEach
        fun setUp() {
            val testMember = MemberFixture.createMember()
            memberRepository.save(testMember)
            testMemberId = testMember.id

            val registered = CourseFixture.createCourseWithDetails(
                "자료구조", "Data Structure", "CSE101", "CSE101001",
                CourseGrade.SOPHOMORE,
            )
            registered.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    registered, CourseDay.MONDAY, LocalTime.of(13, 0), LocalTime.of(15, 0),
                )
            )

            val otherSection = CourseFixture.createCourseWithDetails(
                "자료구조", "Data Structure", "CSE101", "CSE101002",
                CourseGrade.SOPHOMORE,
            )
            otherSection.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    otherSection, CourseDay.TUESDAY, LocalTime.of(9, 0), LocalTime.of(11, 0),
                )
            )

            val otherSubject = CourseFixture.createCourseWithDetails(
                "알고리즘", "Algorithm", "CSE201", "CSE201001",
                CourseGrade.SOPHOMORE,
            )
            otherSubject.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    otherSubject, CourseDay.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(11, 0),
                )
            )

            courseRepository.saveAll(listOf(registered, otherSection, otherSubject))
            registrationRepository.save(RegistrationFixture.createRegistration(testMember, registered))

            otherSectionId = otherSection.id
            otherSubjectId = otherSubject.id
        }

        @Test
        fun 분반이_달라도_과목명이_같으면_신청할_수_없다() {
            //given

            //when & then
            assertThatThrownBy { registrationService.registerCourse(testMemberId, otherSectionId) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", DUPLICATE_SUBJECT_REGISTERED)
        }

        @Test
        fun 과목명이_다르면_신청할_수_있다() {
            //given

            //when
            registrationService.registerCourse(testMemberId, otherSubjectId)

            //then
            assertThat(registrationRepository.findByMemberId(testMemberId)).hasSize(2)
        }
    }

    @Nested
    inner class 검증_순서_테스트 {
        private var testMemberId = 0L
        private var conflictCourseId = 0L
        private var sameSubjectCourseId = 0L
        private var normalCourseId = 0L

        @BeforeEach
        fun setUp() {
            val testMember = MemberFixture.createMember()
            memberRepository.save(testMember)
            testMemberId = testMember.id

            val registered = CourseFixture.createCourse(
                "자료구조", "Data Structure", "CSE101", "CSE101001",
                CourseCollege.INFORMATION_TECHNOLOGY, CourseDepartment.COMPUTER_ENGINEERING,
                CourseClassification.MAJOR_CORE, CourseArea.MAJOR_CORE,
                CourseType.LECTURE, CourseGrade.SOPHOMORE,
                21, false, 50, 0,
            )
            registered.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    registered, CourseDay.MONDAY, LocalTime.of(13, 0), LocalTime.of(15, 0),
                )
            )

            val conflictCourse = CourseFixture.createCourseWithDetails(
                "운영체제", "Operating System", "CSE202", "CSE202001",
                CourseGrade.SOPHOMORE,
            )
            conflictCourse.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    conflictCourse, CourseDay.MONDAY, LocalTime.of(14, 0), LocalTime.of(16, 0),
                )
            )

            val sameSubjectCourse = CourseFixture.createCourseWithDetails(
                "자료구조", "Data Structure", "CSE101", "CSE101002",
                CourseGrade.SOPHOMORE,
            )
            sameSubjectCourse.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    sameSubjectCourse, CourseDay.TUESDAY, LocalTime.of(9, 0), LocalTime.of(11, 0),
                )
            )

            val normalCourse = CourseFixture.createCourseWithDetails(
                "알고리즘", "Algorithm", "CSE201", "CSE201001",
                CourseGrade.SOPHOMORE,
            )
            normalCourse.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    normalCourse, CourseDay.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(11, 0),
                )
            )

            courseRepository.saveAll(listOf(registered, conflictCourse, sameSubjectCourse, normalCourse))
            registrationRepository.save(RegistrationFixture.createRegistration(testMember, registered))

            conflictCourseId = conflictCourse.id
            sameSubjectCourseId = sameSubjectCourse.id
            normalCourseId = normalCourse.id
        }

        @Test
        fun 시간표_중복은_학점_상한보다_먼저_판정된다() {
            //given

            //when & then
            assertThatThrownBy { registrationService.registerCourse(testMemberId, conflictCourseId) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", COURSE_SCHEDULE_CONFLICT)
        }

        @Test
        fun 동일_과목명은_학점_상한보다_먼저_판정된다() {
            //given

            //when & then
            assertThatThrownBy { registrationService.registerCourse(testMemberId, sameSubjectCourseId) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", DUPLICATE_SUBJECT_REGISTERED)
        }

        @Test
        fun 앞선_조건에_걸리지_않으면_학점_상한으로_판정된다() {
            //given

            //when & then
            assertThatThrownBy { registrationService.registerCourse(testMemberId, normalCourseId) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", CREDIT_LIMIT_EXCEEDED)
        }
    }

    @Nested
    inner class 신청_성공_응답_테스트 {
        private var testMemberId = 0L
        private var courseId = 0L

        @BeforeEach
        fun setUp() {
            val testMember = MemberFixture.createMember()
            memberRepository.save(testMember)
            testMemberId = testMember.id

            val course = CourseFixture.createCourseWithDetails(
                "자료구조", "Data Structure", "CSE101", "CSE101001",
                CourseGrade.SOPHOMORE,
            )
            courseRepository.save(course)
            courseId = course.id
        }

        @Test
        fun 신청에_성공하면_신청한_강의가_함께_반환된다() {
            //given

            //when
            val response = registrationService.registerCourse(testMemberId, courseId)

            //then
            assertThat(response.courseResponse.id).isEqualTo(courseId.toString())
            assertThat(response.courseResponse.name).isEqualTo("자료구조")
            assertThat(response.courseResponse.code).isEqualTo("CSE101001")
        }
    }

    @Nested
    inner class 학생_기준_이수구분_테스트 {
        private var testMemberId = 0L

        @BeforeEach
        fun setUp() {
            val testMember = MemberFixture.createMember()
            memberRepository.save(testMember)
            testMemberId = testMember.id

            val ownMajor = CourseFixture.createCourse(
                "자료구조", "Data Structure", "CSE101", "CSE101001",
                CourseCollege.INFORMATION_TECHNOLOGY, CourseDepartment.COMPUTER_ENGINEERING,
                CourseClassification.MAJOR_CORE, CourseArea.MAJOR_CORE,
                CourseType.LECTURE, CourseGrade.SOPHOMORE,
                3, false, 50, 0,
            )
            val otherMajor = CourseFixture.createCourse(
                "경영프로그래밍", "Business Programming", "BUS101", "BUS101001",
                CourseCollege.BUSINESS, CourseDepartment.BUSINESS_ADMINISTRATION,
                CourseClassification.MAJOR_ADVANCED, CourseArea.MAJOR_ADVANCED,
                CourseType.LECTURE, CourseGrade.SOPHOMORE,
                3, false, 50, 0,
            )
            val liberalArts = CourseFixture.createCourse(
                "글쓰기", "Writing", "GEN101", "GEN101001",
                CourseCollege.GENERAL_EDUCATION, CourseDepartment.GENERAL_EDUCATION,
                CourseClassification.CORE_LIBERAL_ARTS, CourseArea.CORE_HUMANITIES,
                CourseType.LECTURE, CourseGrade.ALL,
                3, false, 50, 0,
            )

            courseRepository.saveAll(listOf(ownMajor, otherMajor, liberalArts))
            registrationRepository.saveAll(
                listOf(
                    RegistrationFixture.createRegistration(testMember, ownMajor),
                    RegistrationFixture.createRegistration(testMember, otherMajor),
                    RegistrationFixture.createRegistration(testMember, liberalArts),
                )
            )
        }

        @Test
        fun 타_학과_전공과목은_일반선택으로_산출된다() {
            //given

            //when
            val response = registrationService.getRegistrationCourse(testMemberId)

            //then
            val otherMajor = findByCourseCode(response, "BUS101")
            assertThat(otherMajor.courseResponse.courseType).isEqualTo("전공심화")
            assertThat(otherMajor.resolvedType).isEqualTo("일반선택")
        }

        @Test
        fun 소속_학과_전공과목은_원문_이수구분을_유지한다() {
            //given

            //when
            val response = registrationService.getRegistrationCourse(testMemberId)

            //then
            val ownMajor = findByCourseCode(response, "CSE101")
            assertThat(ownMajor.resolvedType).isEqualTo("전공핵심")
        }

        @Test
        fun 교양과목은_타_학과_개설이어도_원문_이수구분을_유지한다() {
            //given

            //when
            val response = registrationService.getRegistrationCourse(testMemberId)

            //then
            val liberalArts = findByCourseCode(response, "GEN101")
            assertThat(liberalArts.resolvedType).isEqualTo("핵심교양")
        }

        @Test
        fun 학번과_재수강_구분과_신청_시각이_함께_담긴다() {
            //given
            val testMember = memberRepository.findById(testMemberId).orElseThrow()

            //when
            val response = registrationService.getRegistrationCourse(testMemberId)

            //then
            assertThat(response.registrationCourseResponses)
                .isNotEmpty()
                .allSatisfy { registration ->
                    assertThat(registration.studentId).isEqualTo(testMember.studentId)
                    assertThat(registration.reAttendance).isEmpty()
                    assertThat(registration.createdAt).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}")
                }
        }

        private fun findByCourseCode(
            response: RegistrationCoursesResponse,
            courseCode: String,
        ): RegistrationCourseResponse {
            return response.registrationCourseResponses.first { it.courseResponse.courseCode == courseCode }
        }
    }

    @Nested
    inner class 같은_강의_재신청_판정_테스트 {
        private var testMemberId = 0L
        private var scheduledCourseId = 0L
        private var onlineCourseId = 0L

        @BeforeEach
        fun setUp() {
            val testMember = MemberFixture.createMember()
            memberRepository.save(testMember)
            testMemberId = testMember.id

            val scheduled = CourseFixture.createCourseWithDetails(
                "자료구조", "Data Structure", "CSE101", "CSE101001",
                CourseGrade.SOPHOMORE,
            )
            scheduled.addCourseSchedule(
                CourseScheduleFixture.createCourseSchedule(
                    scheduled, CourseDay.MONDAY, LocalTime.of(13, 0), LocalTime.of(15, 0),
                )
            )

            val online = CourseFixture.createCourseWithDetails(
                "온라인특강", "Online Lecture", "CSE900", "CSE900001",
                CourseGrade.SOPHOMORE,
            )

            courseRepository.saveAll(listOf(scheduled, online))
            scheduledCourseId = scheduled.id
            onlineCourseId = online.id
        }

        @Test
        fun 시간표가_있는_강의를_다시_신청하면_시간표_중복으로_잡힌다() {
            //given
            registrationService.registerCourse(testMemberId, scheduledCourseId)

            //when & then
            assertThatThrownBy { registrationService.registerCourse(testMemberId, scheduledCourseId) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", COURSE_SCHEDULE_CONFLICT)
        }

        @Test
        fun 시간표가_없는_강의를_다시_신청하면_동일_과목명으로_잡힌다() {
            //given
            registrationService.registerCourse(testMemberId, onlineCourseId)

            //when & then
            assertThatThrownBy { registrationService.registerCourse(testMemberId, onlineCourseId) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", DUPLICATE_SUBJECT_REGISTERED)
        }
    }
}
