package uss.code.admin.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import uss.code.admin.domain.Admin
import uss.code.admin.dto.request.AdminLoginRequest
import uss.code.admin.fixture.AdminFixture
import uss.code.admin.repository.AdminRepository
import uss.code.auth.infra.JwtProvider
import uss.code.global.exception.domain.ExceptionCode.ADMIN_ACCESS_DENIED
import uss.code.global.exception.domain.ExceptionCode.ADMIN_LOGIN_FAILED
import uss.code.global.exception.domain.ExceptionCode.ADMIN_NOT_FOUND
import uss.code.global.exception.domain.ExceptionCode.INVALID_FORM_ACCESS_TOKEN
import uss.code.global.exception.domain.ExceptionCode.INVALID_SIGNATURE_ACCESS_TOKEN
import uss.code.global.exception.domain.ExceptionCode.MISSING_ACCESS_TOKEN
import uss.code.global.exception.domain.JwtTokenInvalidException
import uss.code.global.exception.domain.JwtTokenMissingException
import uss.code.global.exception.domain.RestApiException
import uss.code.global.infra.IntegrationTest

@IntegrationTest
class AdminAuthServiceTest(
    private val adminAuthService: AdminAuthService,

    private val adminRepository: AdminRepository,

    private val jwtProvider: JwtProvider,
    @param:Value("\${security.jwt.secret-key}")
    private val secretKey: String,
) {
    private lateinit var savedAdmin: Admin

    @BeforeEach
    fun setUp() {
        savedAdmin = adminRepository.save(AdminFixture.createAdmin())
    }

    private fun generateExpiredAdminToken(adminId: Long): String {
        return JwtProvider(
            secretKey = secretKey,
            accessTokenExpirationTime = VALID_VALIDITY_TIME,
            adminAccessTokenExpirationTime = EXPIRED_VALIDITY_TIME,
        ).generateAdminToken(adminId)
    }

    private fun generateAdminTokenSignedWithOtherKey(adminId: Long): String {
        return JwtProvider(
            secretKey = OTHER_SECRET_KEY,
            accessTokenExpirationTime = VALID_VALIDITY_TIME,
            adminAccessTokenExpirationTime = VALID_VALIDITY_TIME,
        ).generateAdminToken(adminId)
    }

    @Nested
    inner class 관리자_로그인_테스트 {
        @Test
        fun 아이디와_비밀번호가_맞으면_토큰과_이름을_받는다() {
            //given
            val request = AdminLoginRequest(
                loginId = AdminFixture.DEFAULT_LOGIN_ID,
                password = AdminFixture.DEFAULT_PASSWORD,
            )

            //when
            val response = adminAuthService.login(request)

            //then
            assertThat(response.accessToken).isNotBlank()
            assertThat(response.name).isEqualTo(AdminFixture.DEFAULT_NAME)
        }

        @Test
        fun 발급된_토큰은_관리자_토큰이다() {
            //given
            val request = AdminLoginRequest(
                loginId = AdminFixture.DEFAULT_LOGIN_ID,
                password = AdminFixture.DEFAULT_PASSWORD,
            )

            //when
            val response = adminAuthService.login(request)

            //then
            assertThat(jwtProvider.isAdminToken(response.accessToken)).isTrue()
            assertThat(jwtProvider.getAdminId(response.accessToken)).isEqualTo(savedAdmin.id)
        }

        @Test
        fun 존재하지_않는_아이디면_예외가_발생한다() {
            //given
            val request = AdminLoginRequest(
                loginId = "unknown-admin",
                password = AdminFixture.DEFAULT_PASSWORD,
            )

            //when & then
            assertThatThrownBy { adminAuthService.login(request) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", ADMIN_LOGIN_FAILED)
        }

        @Test
        fun 비밀번호가_틀리면_아이디_없음과_같은_코드로_실패한다() {
            //given
            val request = AdminLoginRequest(
                loginId = AdminFixture.DEFAULT_LOGIN_ID,
                password = "wrong-password",
            )

            //when & then
            assertThatThrownBy { adminAuthService.login(request) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", ADMIN_LOGIN_FAILED)
        }
    }

    @Nested
    inner class 관리자_토큰_재발급_테스트 {
        @Test
        fun 유효한_토큰이면_재발급에_성공한다() {
            //given
            val accessToken = jwtProvider.generateAdminToken(savedAdmin.id)

            //when
            val response = adminAuthService.reissue(accessToken)

            //then
            assertThat(response.accessToken).isNotBlank()
            assertThat(response.name).isEqualTo(AdminFixture.DEFAULT_NAME)
        }

        @Test
        fun 만료된_토큰이어도_서명이_유효하면_재발급에_성공한다() {
            //given
            val expiredToken = generateExpiredAdminToken(savedAdmin.id)

            //when
            val response = adminAuthService.reissue(expiredToken)

            //then
            assertThat(response.accessToken).isNotBlank()
            assertThat(jwtProvider.isAdminToken(response.accessToken)).isTrue()
        }

        @Test
        fun 토큰이_없으면_예외가_발생한다() {
            //when & then
            assertThatThrownBy { adminAuthService.reissue(null) }
                .isInstanceOf(JwtTokenMissingException::class.java)
                .hasFieldOrPropertyWithValue("code", MISSING_ACCESS_TOKEN.code)
        }

        @Test
        fun 형식이_올바르지_않은_토큰이면_예외가_발생한다() {
            //when & then
            assertThatThrownBy { adminAuthService.reissue(MALFORMED_TOKEN) }
                .isInstanceOf(JwtTokenInvalidException::class.java)
                .hasFieldOrPropertyWithValue("code", INVALID_FORM_ACCESS_TOKEN.code)
        }

        @Test
        fun 서명이_다른_토큰이면_예외가_발생한다() {
            //given
            val otherKeyToken = generateAdminTokenSignedWithOtherKey(savedAdmin.id)

            //when & then
            assertThatThrownBy { adminAuthService.reissue(otherKeyToken) }
                .isInstanceOf(JwtTokenInvalidException::class.java)
                .hasFieldOrPropertyWithValue("code", INVALID_SIGNATURE_ACCESS_TOKEN.code)
        }

        @Test
        fun 관리자_토큰이_아니면_예외가_발생한다() {
            //given
            val memberToken = jwtProvider.generateAuthToken(savedAdmin.id).accessToken

            //when & then
            assertThatThrownBy { adminAuthService.reissue(memberToken) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", ADMIN_ACCESS_DENIED)
        }

        @Test
        fun 토큰의_관리자가_존재하지_않으면_예외가_발생한다() {
            //given
            val accessToken = jwtProvider.generateAdminToken(savedAdmin.id + 999L)

            //when & then
            assertThatThrownBy { adminAuthService.reissue(accessToken) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", ADMIN_NOT_FOUND)
        }
    }

    companion object {
        private const val OTHER_SECRET_KEY = "other-secret-key-for-unit-testing-purposes-only"
        private const val EXPIRED_VALIDITY_TIME = -60_000L
        private const val VALID_VALIDITY_TIME = 600_000L
        private const val MALFORMED_TOKEN = "malformed.token"
    }
}
