package uss.code.member.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uss.code.global.exception.domain.ExceptionCode.INVALID_ENUM_TYPE
import uss.code.global.exception.domain.ExceptionCode.MEMBER_NOT_FOUND
import uss.code.global.exception.domain.RestApiException
import uss.code.global.infra.IntegrationTest
import uss.code.member.domain.AcademicStatus
import uss.code.member.domain.MemberCollege
import uss.code.member.domain.MemberDepartment
import uss.code.member.domain.MemberGrade
import uss.code.member.dto.request.DepartmentUpdateRequest
import uss.code.member.fixture.MemberFixture
import uss.code.member.repository.MemberRepository

@IntegrationTest
class MemberServiceTest(
    private val memberService: MemberService,

    private val memberRepository: MemberRepository,
) {
    @Nested
    inner class 사용자의_헤더_정보를_조회할_때 {
        private val testStudentId = "202012345"
        private val testName = "홍길동"
        private val testDepartment = MemberDepartment.COMPUTER_ENGINEERING
        private val testGrade = MemberGrade.JUNIOR
        private val testAcademicStatus = AcademicStatus.ENROLLED
        private val testGpa = 3.5

        private val expectedDepartmentName = "컴퓨터공학부"
        private val expectedGradeName = "3학년"
        private val expectedAcademicStatusName = "재학"

        private var validMemberId = 0L
        private val invalidMemberId = 999L

        @BeforeEach
        fun setUp() {
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
        }

        @Test
        fun 사용자_아이디가_유효하면_조회에_성공한다() {
            //given

            //when
            val response = memberService.getProfile(validMemberId)

            //then
            assertThat(response.studentId).isEqualTo(testStudentId)
            assertThat(response.name).isEqualTo(testName)
            assertThat(response.department).isEqualTo(expectedDepartmentName)
            assertThat(response.grade).isEqualTo(expectedGradeName)
            assertThat(response.academicStatus).isEqualTo(expectedAcademicStatusName)
            assertThat(response.gpa).isEqualTo(testGpa)
            assertThat(response.creditLimit).isEqualTo(21)
        }

        @Test
        fun 사용자_아이디가_유효하지_않으면_예외를_반환한다() {
            //given

            //when & then
            assertThatThrownBy { memberService.getProfile(invalidMemberId) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", MEMBER_NOT_FOUND)
        }
    }

    @Nested
    inner class 사용자의_학과를_수정할_때 {
        private val testStudentId = "202054321"
        private val testName = "김인천"
        private val testGrade = MemberGrade.FRESHMAN
        private val testAcademicStatus = AcademicStatus.ENROLLED
        private val testGpa = 0.0

        private val validDepartment = "COMPUTER_ENGINEERING"
        private val unknownDepartment = "존재하지_않는_학과"

        private var validMemberId = 0L
        private val invalidMemberId = 999L

        @BeforeEach
        fun setUp() {
            val member = MemberFixture.createMember(
                testStudentId,
                testName,
                MemberDepartment.MECHANICAL_ENGINEERING,
                testGrade,
                testAcademicStatus,
                testGpa,
            )

            memberRepository.save(member)

            validMemberId = member.id
        }

        @Test
        fun 유효한_학과가_들어오면_수정에_성공한다() {
            //given
            val request = DepartmentUpdateRequest(validDepartment)

            //when
            memberService.updateDepartment(validMemberId, request)

            //then
            val updatedMember = memberRepository.findById(validMemberId).orElseThrow()
            assertThat(updatedMember.department).isEqualTo(MemberDepartment.COMPUTER_ENGINEERING)
        }

        @Test
        fun 학과를_수정하면_단과대학도_함께_바뀐다() {
            //given
            val request = DepartmentUpdateRequest(validDepartment)

            //when
            memberService.updateDepartment(validMemberId, request)

            //then
            val updatedMember = memberRepository.findById(validMemberId).orElseThrow()
            assertThat(updatedMember.college).isEqualTo(MemberCollege.INFORMATION_TECHNOLOGY)
        }

        @Test
        fun 단과대학이_없던_학부의_학과로_수정하면_새_단과대학이_설정된다() {
            //given
            val request = DepartmentUpdateRequest("IBE_MAJOR")

            //when
            memberService.updateDepartment(validMemberId, request)

            //then
            val updatedMember = memberRepository.findById(validMemberId).orElseThrow()
            assertThat(updatedMember.department).isEqualTo(MemberDepartment.IBE_MAJOR)
            assertThat(updatedMember.college).isEqualTo(MemberCollege.NORTHEAST_ASIA_TRADE_LOGISTICS)
        }

        @Test
        fun 유효하지_않은_학과가_들어오면_예외를_반환한다() {
            //given
            val request = DepartmentUpdateRequest(unknownDepartment)

            //when & then
            assertThatThrownBy { memberService.updateDepartment(validMemberId, request) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_ENUM_TYPE)
        }

        @Test
        fun 사용자_아이디가_유효하지_않으면_예외를_반환한다() {
            //given
            val request = DepartmentUpdateRequest(validDepartment)

            //when & then
            assertThatThrownBy { memberService.updateDepartment(invalidMemberId, request) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", MEMBER_NOT_FOUND)
        }
    }

    @Nested
    inner class 최대_이수_학점_노출_테스트 {
        private fun createMemberWithGpa(gpa: Double): Long {
            val member = MemberFixture.createMember(
                "202012345",
                "홍길동",
                MemberDepartment.COMPUTER_ENGINEERING,
                MemberGrade.JUNIOR,
                AcademicStatus.ENROLLED,
                gpa,
            )
            memberRepository.save(member)

            return member.id
        }

        @Test
        fun 성적이_4점_이상이면_24학점이_내려간다() {
            //given
            val memberId = createMemberWithGpa(4.2)

            //when
            val response = memberService.getProfile(memberId)

            //then
            assertThat(response.creditLimit).isEqualTo(24)
        }

        @Test
        fun 성적이_3점5_이상_4점_미만이면_21학점이_내려간다() {
            //given
            val memberId = createMemberWithGpa(3.7)

            //when
            val response = memberService.getProfile(memberId)

            //then
            assertThat(response.creditLimit).isEqualTo(21)
        }

        @Test
        fun 성적이_3점5_미만이면_19학점이_내려간다() {
            //given
            val memberId = createMemberWithGpa(3.0)

            //when
            val response = memberService.getProfile(memberId)

            //then
            assertThat(response.creditLimit).isEqualTo(19)
        }
    }

    @Nested
    inner class 학적_상태_노출_테스트 {
        @Test
        fun 유예_상태가_그대로_내려간다() {
            //given
            val member = MemberFixture.createMember(
                MemberFixture.nextStudentId(),
                "홍길동",
                MemberDepartment.COMPUTER_ENGINEERING,
                MemberGrade.SENIOR,
                AcademicStatus.DEFERMENT,
                3.2,
            )
            memberRepository.save(member)

            //when
            val response = memberService.getProfile(member.id)

            //then
            assertThat(response.academicStatus).isEqualTo("유예")
        }

        @Test
        fun 휴학_상태도_그대로_내려간다() {
            //given
            val member = MemberFixture.createMember(
                MemberFixture.nextStudentId(),
                "홍길동",
                MemberDepartment.COMPUTER_ENGINEERING,
                MemberGrade.SENIOR,
                AcademicStatus.LEAVE_OF_ABSENCE,
                3.2,
            )
            memberRepository.save(member)

            //when
            val response = memberService.getProfile(member.id)

            //then
            assertThat(response.academicStatus).isEqualTo("휴학")
        }
    }
}
