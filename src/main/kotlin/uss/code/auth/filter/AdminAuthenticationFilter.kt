package uss.code.auth.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter
import uss.code.auth.infra.JwtProvider
import uss.code.global.http.AdminEndpoint

class AdminAuthenticationFilter(
    private val jwtProvider: JwtProvider,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val accessToken: String? = request.getHeader(ACCESS_TOKEN_HEADER)

        jwtProvider.validateAdminToken(accessToken)

        val adminId = jwtProvider.getAdminId(accessToken)
        request.setAttribute(ADMIN_ID_ATTRIBUTE, adminId)

        filterChain.doFilter(request, response)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val uri: String = request.requestURI

        return !AdminEndpoint.isAdminPath(uri) ||
            AdminEndpoint.isWhitelisted(uri, request.method)
    }

    companion object {
        private const val ACCESS_TOKEN_HEADER = "access-token"
        private const val ADMIN_ID_ATTRIBUTE = "admin-id"
    }
}
