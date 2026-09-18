package uss.code.auth.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter
import uss.code.auth.infra.JwtProvider
import uss.code.global.exception.domain.ExceptionCode.INVALID_ACCESS_TOKEN
import uss.code.global.exception.domain.JwtTokenInvalidException
import uss.code.global.http.AdminEndpoint
import uss.code.global.http.WhitelistEndpoint

class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val accessToken: String? = request.getHeader(ACCESS_TOKEN_HEADER)

        jwtProvider.validateToken(accessToken)

        if (jwtProvider.isAdminToken(accessToken)) {
            throw JwtTokenInvalidException(INVALID_ACCESS_TOKEN)
        }

        val memberId = jwtProvider.getMemberId(accessToken)
        request.setAttribute(MEMBER_ID_ATTRIBUTE, memberId)

        filterChain.doFilter(request, response)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val uri: String = request.requestURI

        return AdminEndpoint.isAdminPath(uri) ||
            WhitelistEndpoint.isWhitelisted(uri, request.method)
    }

    companion object {
        private const val ACCESS_TOKEN_HEADER = "access-token"
        private const val MEMBER_ID_ATTRIBUTE = "member-id"
    }
}
