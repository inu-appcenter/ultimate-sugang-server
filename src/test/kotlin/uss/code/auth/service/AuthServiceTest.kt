package uss.code.auth.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import uss.code.auth.dto.request.LoginRequest
import uss.code.auth.dto.request.SignUpRequest
import uss.code.auth.infra.JwtProvider
import uss.code.auth.infra.MemberPasswordEncoder
import uss.code.global.exception.domain.ExceptionCode.COLLEGE_DEPARTMENT_MISMATCH
import uss.code.global.exception.domain.ExceptionCode.EMAIL_ALREADY_EXISTS
import uss.code.global.exception.domain.ExceptionCode.EXPIRED_ACCESS_TOKEN
import uss.code.global.exception.domain.ExceptionCode.INVALID_ENUM_TYPE
import uss.code.global.exception.domain.ExceptionCode.INVALID_FORM_ACCESS_TOKEN
import uss.code.global.exception.domain.ExceptionCode.INVALID_SIGNATURE_ACCESS_TOKEN
import uss.code.global.exception.domain.ExceptionCode.MEMBER_NOT_FOUND
import uss.code.global.exception.domain.ExceptionCode.MISSING_ACCESS_TOKEN
import uss.code.global.exception.domain.ExceptionCode.PASSWORD_NOT_MATCH
import uss.code.global.exception.domain.ExceptionCode.STUDENT_ID_ALREADY_EXISTS
import uss.code.global.exception.domain.JwtTokenInvalidException
import uss.code.global.exception.domain.JwtTokenMissingException
import uss.code.global.exception.domain.RestApiException
import uss.code.global.infra.IntegrationTest
import uss.code.member.domain.MemberCollege
import uss.code.member.fixture.MemberFixture
import uss.code.member.repository.MemberRepository

@IntegrationTest
class AuthServiceTest(
    private val authService: AuthService,

    private val memberRepository: MemberRepository,

    private val jwtProvider: JwtProvider,
    private val passwordEncoder: MemberPasswordEncoder,
    @param:Value("\${security.jwt.secret-key}")
    private val secretKey: String,
) {
    private fun createSignUpRequest(
        college: String,
        department: String,
    ): SignUpRequest {
        return SignUpRequest(
            email = TEST_EMAIL,
            password = TEST_RAW_PASSWORD,
            studentId = TEST_STUDENT_ID,
            name = TEST_NAME,
            college = college,
            department = department,
            grade = TEST_GRADE,
            academicStatus = TEST_ACADEMIC_STATUS,
            lastSemesterGpa = TEST_GPA,
        )
    }

    private fun createSignUpRequest(): SignUpRequest {
        return createSignUpRequest(TEST_COLLEGE, TEST_DEPARTMENT)
    }

    private fun createSignUpRequestOf(
        email: String,
        studentId: String,
    ): SignUpRequest {
        return SignUpRequest(
            email = email,
            password = TEST_RAW_PASSWORD,
            studentId = studentId,
            name = TEST_NAME,
            college = TEST_COLLEGE,
            department = TEST_DEPARTMENT,
            grade = TEST_GRADE,
            academicStatus = TEST_ACADEMIC_STATUS,
            lastSemesterGpa = TEST_GPA,
        )
    }

    private fun generateExpiredToken(memberId: Long): String {
        return JwtProvider(
            secretKey = secretKey,
            accessTokenExpirationTime = EXPIRED_VALIDITY_TIME,
            adminAccessTokenExpirationTime = ADMIN_VALIDITY_TIME,
        )
            .generateAuthToken(memberId)
            .accessToken
    }

    private fun generateTokenSignedWithOtherKey(memberId: Long): String {
        return JwtProvider(
            secretKey = OTHER_SECRET_KEY,
            accessTokenExpirationTime = VALID_VALIDITY_TIME,
            adminAccessTokenExpirationTime = ADMIN_VALIDITY_TIME,
        )
            .generateAuthToken(memberId)
            .accessToken
    }

    @Nested
    inner class 회원가입할_때 {
        @Test
        fun 유효한_요청이면_가입에_성공하고_토큰을_반환한다() {
            //given
            val request = createSignUpRequest()

            //when
            val response = authService.signUp(request)

            //then
            assertThat(response.accessToken).isNotBlank()

            val member = checkNotNull(memberRepository.findByEmail(TEST_EMAIL))
            assertThat(jwtProvider.getMemberId(response.accessToken)).isEqualTo(member.id)
            assertThat(member.college).isEqualTo(MemberCollege.INFORMATION_TECHNOLOGY)
        }

        @Test
        fun 비밀번호는_평문으로_저장되지_않는다() {
            //given
            val request = createSignUpRequest()

            //when
            authService.signUp(request)

            //then
            val member = checkNotNull(memberRepository.findByEmail(TEST_EMAIL))
            assertThat(member.password).isNotEqualTo(TEST_RAW_PASSWORD)
            assertThat(passwordEncoder.matches(TEST_RAW_PASSWORD, member.password)).isTrue()
        }

        @Test
        fun 이미_사용_중인_이메일이면_예외를_반환한다() {
            //given
            authService.signUp(createSignUpRequest())

            //when & then
            assertThatThrownBy { authService.signUp(createSignUpRequestOf(TEST_EMAIL, UNKNOWN_STUDENT_ID)) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", EMAIL_ALREADY_EXISTS)
        }

        @Test
        fun 이미_사용_중인_학번이면_예외를_반환한다() {
            //given
            authService.signUp(createSignUpRequest())

            //when & then
            assertThatThrownBy { authService.signUp(createSignUpRequestOf(UNKNOWN_EMAIL, TEST_STUDENT_ID)) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", STUDENT_ID_ALREADY_EXISTS)
        }

        @Test
        fun 학번과_이메일이_모두_겹치면_학번_사유가_먼저_나간다() {
            //given
            authService.signUp(createSignUpRequest())

            //when & then
            assertThatThrownBy { authService.signUp(createSignUpRequest()) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", STUDENT_ID_ALREADY_EXISTS)
        }

        @Test
        fun 학과와_단과대학이_어긋나면_예외를_반환한다() {
            //given
            val request = createSignUpRequest(MISMATCHED_COLLEGE, TEST_DEPARTMENT)

            //when & then
            assertThatThrownBy { authService.signUp(request) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", COLLEGE_DEPARTMENT_MISMATCH)
        }

        @Test
        fun 유효하지_않은_학과면_예외를_반환한다() {
            //given
            val request = createSignUpRequest(TEST_COLLEGE, UNKNOWN_DEPARTMENT)

            //when & then
            assertThatThrownBy { authService.signUp(request) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", INVALID_ENUM_TYPE)
        }
    }

    @Nested
    inner class 이메일_중복을_검사할_때 {
        @Test
        fun 쓰이지_않은_이메일이면_사용_가능으로_응답한다() {
            //given

            //when
            val response = authService.checkEmailAvailability(UNKNOWN_EMAIL)

            //then
            assertThat(response.available).isTrue()
        }

        @Test
        fun 이미_쓰이는_이메일이면_사용_불가로_응답한다() {
            //given
            authService.signUp(createSignUpRequest())

            //when
            val response = authService.checkEmailAvailability(TEST_EMAIL)

            //then
            assertThat(response.available).isFalse()
        }
    }

    @Nested
    inner class 학번_중복을_검사할_때 {
        @Test
        fun 쓰이지_않은_학번이면_사용_가능으로_응답한다() {
            //given

            //when
            val response = authService.checkStudentIdAvailability(TEST_STUDENT_ID)

            //then
            assertThat(response.available).isTrue()
        }

        @Test
        fun 이미_쓰이는_학번이면_사용_불가로_응답한다() {
            //given
            authService.signUp(createSignUpRequest())

            //when
            val response = authService.checkStudentIdAvailability(TEST_STUDENT_ID)

            //then
            assertThat(response.available).isFalse()
        }
    }

    @Nested
    inner class 로그인할_때 {
        @BeforeEach
        fun setUp() {
            authService.signUp(createSignUpRequest())
        }

        @Test
        fun 학번과_비밀번호가_맞으면_토큰을_반환한다() {
            //given
            val request = LoginRequest(
                studentId = TEST_STUDENT_ID,
                password = TEST_RAW_PASSWORD,
            )

            //when
            val response = authService.login(request)

            //then
            assertThat(response.accessToken).isNotBlank()

            val member = checkNotNull(memberRepository.findByStudentId(TEST_STUDENT_ID))
            assertThat(jwtProvider.getMemberId(response.accessToken)).isEqualTo(member.id)
        }

        @Test
        fun 없는_학번이면_예외를_반환한다() {
            //given
            val request = LoginRequest(
                studentId = UNKNOWN_STUDENT_ID,
                password = TEST_RAW_PASSWORD,
            )

            //when & then
            assertThatThrownBy { authService.login(request) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", MEMBER_NOT_FOUND)
        }

        @Test
        fun 이메일로는_로그인할_수_없다() {
            //given
            val request = LoginRequest(
                studentId = TEST_EMAIL,
                password = TEST_RAW_PASSWORD,
            )

            //when & then
            assertThatThrownBy { authService.login(request) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", MEMBER_NOT_FOUND)
        }

        @Test
        fun 비밀번호가_틀리면_예외를_반환한다() {
            //given
            val request = LoginRequest(
                studentId = TEST_STUDENT_ID,
                password = WRONG_PASSWORD,
            )

            //when & then
            assertThatThrownBy { authService.login(request) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", PASSWORD_NOT_MATCH)
        }
    }

    @Nested
    inner class 액세스_토큰_재발급_테스트 {
        private var savedMemberId = 0L

        @BeforeEach
        fun setUp() {
            val member = memberRepository.save(MemberFixture.createMember())
            savedMemberId = member.id
        }

        @Test
        fun 만료된_토큰이어도_서명이_유효하면_재발급에_성공한다() {
            //given
            val expiredToken = generateExpiredToken(savedMemberId)

            assertThatThrownBy { jwtProvider.validateToken(expiredToken) }
                .hasFieldOrPropertyWithValue("code", EXPIRED_ACCESS_TOKEN.code)

            //when
            val response = authService.reissue(expiredToken)

            //then
            assertThat(response.accessToken).isNotBlank()
            assertThat(response.accessToken).isNotEqualTo(expiredToken)
            assertThat(jwtProvider.getMemberId(response.accessToken)).isEqualTo(savedMemberId)
            assertThatCode { jwtProvider.validateToken(response.accessToken) }
                .doesNotThrowAnyException()
        }

        @Test
        fun 아직_만료되지_않은_토큰으로도_재발급에_성공한다() {
            //given
            val validToken = jwtProvider.generateAuthToken(savedMemberId).accessToken

            //when
            val response = authService.reissue(validToken)

            //then
            assertThat(jwtProvider.getMemberId(response.accessToken)).isEqualTo(savedMemberId)
            assertThatCode { jwtProvider.validateToken(response.accessToken) }
                .doesNotThrowAnyException()
        }

        @Test
        fun 토큰이_없으면_예외가_발생한다() {
            //given

            //when & then
            assertThatThrownBy { authService.reissue(null) }
                .isInstanceOf(JwtTokenMissingException::class.java)
                .hasFieldOrPropertyWithValue("code", MISSING_ACCESS_TOKEN.code)
        }

        @Test
        fun 서명이_다른_토큰이면_예외가_발생한다() {
            //given
            val otherKeyToken = generateTokenSignedWithOtherKey(savedMemberId)

            //when & then
            assertThatThrownBy { authService.reissue(otherKeyToken) }
                .isInstanceOf(JwtTokenInvalidException::class.java)
                .hasFieldOrPropertyWithValue("code", INVALID_SIGNATURE_ACCESS_TOKEN.code)
        }

        @Test
        fun 형식이_올바르지_않은_토큰이면_예외가_발생한다() {
            //given

            //when & then
            assertThatThrownBy { authService.reissue(MALFORMED_TOKEN) }
                .isInstanceOf(JwtTokenInvalidException::class.java)
                .hasFieldOrPropertyWithValue("code", INVALID_FORM_ACCESS_TOKEN.code)
        }

        @Test
        fun 토큰의_회원이_존재하지_않으면_예외가_발생한다() {
            //given
            val unknownMemberToken = generateExpiredToken(UNKNOWN_MEMBER_ID)

            //when & then
            assertThatThrownBy { authService.reissue(unknownMemberToken) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", MEMBER_NOT_FOUND)
        }
    }

    companion object {
        private const val TEST_EMAIL = "student@inu.ac.kr"
        private const val TEST_RAW_PASSWORD = "password1234"
        private const val TEST_STUDENT_ID = "202012345"
        private const val TEST_NAME = "홍길동"
        private const val TEST_COLLEGE = "INFORMATION_TECHNOLOGY"
        private const val TEST_DEPARTMENT = "COMPUTER_ENGINEERING"
        private const val TEST_GRADE = "JUNIOR"
        private const val TEST_ACADEMIC_STATUS = "ENROLLED"
        private const val TEST_GPA = 3.5

        private const val UNKNOWN_EMAIL = "unknown@inu.ac.kr"
        private const val UNKNOWN_STUDENT_ID = "209900000"
        private const val WRONG_PASSWORD = "wrongPassword1234"
        private const val MISMATCHED_COLLEGE = "ENGINEERING"
        private const val UNKNOWN_DEPARTMENT = "존재하지_않는_학과"
        private const val UNKNOWN_MEMBER_ID = 999_999L

        private const val OTHER_SECRET_KEY = "another-secret-key-for-signature-mismatch-test"
        private const val MALFORMED_TOKEN = "this-is-not-a-jwt"
        private const val EXPIRED_VALIDITY_TIME = -60_000L
        private const val VALID_VALIDITY_TIME = 600_000L
        private const val ADMIN_VALIDITY_TIME = 7_200_000L
    }
}
