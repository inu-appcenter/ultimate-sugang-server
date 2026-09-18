package uss.code.auth.infra

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import uss.code.global.exception.domain.ExceptionCode.ADMIN_ACCESS_DENIED
import uss.code.global.exception.domain.ExceptionCode.EXPIRED_ACCESS_TOKEN
import uss.code.global.exception.domain.ExceptionCode.INVALID_FORM_ACCESS_TOKEN
import uss.code.global.exception.domain.ExceptionCode.MISSING_ACCESS_TOKEN
import uss.code.global.exception.domain.JwtTokenExpiredException
import uss.code.global.exception.domain.JwtTokenInvalidException
import uss.code.global.exception.domain.JwtTokenMissingException
import uss.code.global.exception.domain.RestApiException
import uss.code.global.infra.IntegrationTest

@IntegrationTest
class JwtProviderTest(
    private val jwtProvider: JwtProvider,

    @param:Value("\${security.jwt.secret-key}")
    private val secretKey: String,
) {
    @Nested
    inner class 관리자_토큰_발급_테스트 {
        @Test
        fun 관리자_토큰은_관리자_토큰으로_식별된다() {
            //given
            val adminToken = jwtProvider.generateAdminToken(ADMIN_ID)

            //when & then
            assertThat(jwtProvider.isAdminToken(adminToken)).isTrue()
            assertThat(jwtProvider.getAdminId(adminToken)).isEqualTo(ADMIN_ID)
        }

        @Test
        fun 학생_토큰은_관리자_토큰이_아니다() {
            //given
            val memberToken = jwtProvider.generateAuthToken(MEMBER_ID).accessToken

            //when & then
            assertThat(jwtProvider.isAdminToken(memberToken)).isFalse()
        }

        @Test
        fun 만료된_관리자_토큰도_권한을_읽을_수_있다() {
            //given
            val expiredAdminToken = JwtProvider(
                secretKey = secretKey,
                accessTokenExpirationTime = VALID_VALIDITY_TIME,
                adminAccessTokenExpirationTime = EXPIRED_VALIDITY_TIME,
            ).generateAdminToken(ADMIN_ID)

            //when & then
            assertThat(jwtProvider.isAdminToken(expiredAdminToken)).isTrue()
        }
    }

    @Nested
    inner class 관리자_토큰_검증_테스트 {
        @Test
        fun 유효한_관리자_토큰이면_통과한다() {
            //given
            val adminToken = jwtProvider.generateAdminToken(ADMIN_ID)

            //when & then
            assertThatCode { jwtProvider.validateAdminToken(adminToken) }
                .doesNotThrowAnyException()
        }

        @Test
        fun 학생_토큰이면_권한_예외가_발생한다() {
            //given
            val memberToken = jwtProvider.generateAuthToken(MEMBER_ID).accessToken

            //when & then
            assertThatThrownBy { jwtProvider.validateAdminToken(memberToken) }
                .isInstanceOf(RestApiException::class.java)
                .hasFieldOrPropertyWithValue("exceptionCode", ADMIN_ACCESS_DENIED)
        }

        @Test
        fun 토큰이_없으면_누락_예외가_발생한다() {
            //when & then
            assertThatThrownBy { jwtProvider.validateAdminToken(null) }
                .isInstanceOf(JwtTokenMissingException::class.java)
                .hasFieldOrPropertyWithValue("code", MISSING_ACCESS_TOKEN.code)
        }

        @Test
        fun 형식이_올바르지_않으면_형식_예외가_발생한다() {
            //when & then
            assertThatThrownBy { jwtProvider.validateAdminToken(MALFORMED_TOKEN) }
                .isInstanceOf(JwtTokenInvalidException::class.java)
                .hasFieldOrPropertyWithValue("code", INVALID_FORM_ACCESS_TOKEN.code)
        }

        @Test
        fun 만료됐으면_권한_검사보다_만료_예외가_먼저_발생한다() {
            //given
            val expiredAdminToken = JwtProvider(
                secretKey = secretKey,
                accessTokenExpirationTime = VALID_VALIDITY_TIME,
                adminAccessTokenExpirationTime = EXPIRED_VALIDITY_TIME,
            ).generateAdminToken(ADMIN_ID)

            //when & then
            assertThatThrownBy { jwtProvider.validateAdminToken(expiredAdminToken) }
                .isInstanceOf(JwtTokenExpiredException::class.java)
                .hasFieldOrPropertyWithValue("code", EXPIRED_ACCESS_TOKEN.code)
        }
    }

    companion object {
        private const val ADMIN_ID = 7L
        private const val MEMBER_ID = 11L
        private const val VALID_VALIDITY_TIME = 600_000L
        private const val EXPIRED_VALIDITY_TIME = -60_000L
        private const val MALFORMED_TOKEN = "malformed.token"
    }
}
