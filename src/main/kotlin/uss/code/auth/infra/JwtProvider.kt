package uss.code.auth.infra

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uss.code.auth.dto.response.AuthTokenResponse
import uss.code.global.exception.domain.ExceptionCode.ADMIN_ACCESS_DENIED
import uss.code.global.exception.domain.ExceptionCode.EXPIRED_ACCESS_TOKEN
import uss.code.global.exception.domain.ExceptionCode.INVALID_ACCESS_TOKEN
import uss.code.global.exception.domain.ExceptionCode.INVALID_FORM_ACCESS_TOKEN
import uss.code.global.exception.domain.ExceptionCode.INVALID_SIGNATURE_ACCESS_TOKEN
import uss.code.global.exception.domain.ExceptionCode.MISSING_ACCESS_TOKEN
import uss.code.global.exception.domain.JwtTokenExpiredException
import uss.code.global.exception.domain.JwtTokenInvalidException
import uss.code.global.exception.domain.JwtTokenMissingException
import uss.code.global.exception.domain.RestApiException
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(
    @Value("\${security.jwt.secret-key}")
    secretKey: String,
    @param:Value("\${security.jwt.access-token-expiration-time}")
    private val accessTokenExpirationTime: Long,
    @param:Value("\${security.jwt.admin-access-token-expiration-time}")
    private val adminAccessTokenExpirationTime: Long,
) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))

    fun generateAuthToken(memberId: Long): AuthTokenResponse {
        return AuthTokenResponse.of(generateToken(memberId, accessTokenExpirationTime))
    }

    fun generateAdminToken(adminId: Long): String {
        val now = Date()
        val validityDate = Date(now.time + adminAccessTokenExpirationTime)

        return Jwts.builder()
            .subject(adminId.toString())
            .claim(ROLE_CLAIM, ADMIN_ROLE)
            .issuedAt(now)
            .expiration(validityDate)
            .signWith(secretKey)
            .compact()
    }

    private fun generateToken(
        memberId: Long,
        validityTime: Long,
    ): String {
        val now = Date()
        val validityDate = Date(now.time + validityTime)

        return Jwts.builder()
            .subject(memberId.toString())
            .issuedAt(now)
            .expiration(validityDate)
            .signWith(secretKey)
            .compact()
    }

    fun getMemberId(token: String?): Long {
        return extractId(parseJwt(token).payload)
    }

    fun getAdminId(accessToken: String?): Long {
        return getMemberId(accessToken)
    }

    fun getAdminIdAllowingExpiration(accessToken: String?): Long {
        return getMemberIdAllowingExpiration(accessToken)
    }

    fun isAdminToken(accessToken: String?): Boolean {
        return ADMIN_ROLE == extractRole(accessToken)
    }

    private fun extractId(claims: Claims): Long {
        val id: String = claims.subject
            ?: throw JwtTokenInvalidException(INVALID_ACCESS_TOKEN)

        return id.toLong()
    }

    private fun extractRole(accessToken: String?): String? {
        return try {
            parseJwt(accessToken).payload[ROLE_CLAIM, String::class.java]
        } catch (e: ExpiredJwtException) {
            e.claims[ROLE_CLAIM, String::class.java]
        }
    }

    private fun parseJwt(token: String?): Jws<Claims> {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
    }

    fun validateToken(accessToken: String?) {
        if (accessToken == null) {
            throw JwtTokenMissingException(MISSING_ACCESS_TOKEN)
        }

        validateAccessToken(accessToken)
    }

    fun validateAdminToken(accessToken: String?) {
        validateToken(accessToken)

        if (!isAdminToken(accessToken)) {
            throw RestApiException(ADMIN_ACCESS_DENIED)
        }
    }

    fun getMemberIdAllowingExpiration(accessToken: String?): Long {
        if (accessToken == null) {
            throw JwtTokenMissingException(MISSING_ACCESS_TOKEN)
        }

        try {
            return extractId(parseJwt(accessToken).payload)
        } catch (e: ExpiredJwtException) {
            return extractId(e.claims)
        } catch (e: MalformedJwtException) {
            throw JwtTokenInvalidException(INVALID_FORM_ACCESS_TOKEN)
        } catch (e: SignatureException) {
            throw JwtTokenInvalidException(INVALID_SIGNATURE_ACCESS_TOKEN)
        } catch (e: JwtException) {
            throw JwtTokenInvalidException(INVALID_ACCESS_TOKEN)
        } catch (e: IllegalArgumentException) {
            throw JwtTokenInvalidException(INVALID_ACCESS_TOKEN)
        }
    }

    private fun validateAccessToken(accessToken: String) {
        try {
            parseJwt(accessToken)
        } catch (e: ExpiredJwtException) {
            throw JwtTokenExpiredException(EXPIRED_ACCESS_TOKEN)
        } catch (e: MalformedJwtException) {
            throw JwtTokenInvalidException(INVALID_FORM_ACCESS_TOKEN)
        } catch (e: SignatureException) {
            throw JwtTokenInvalidException(INVALID_SIGNATURE_ACCESS_TOKEN)
        } catch (e: JwtException) {
            throw JwtTokenInvalidException(INVALID_ACCESS_TOKEN)
        } catch (e: IllegalArgumentException) {
            throw JwtTokenInvalidException(INVALID_ACCESS_TOKEN)
        }
    }

    companion object {
        private const val ROLE_CLAIM = "role"
        private const val ADMIN_ROLE = "ADMIN"
    }
}
