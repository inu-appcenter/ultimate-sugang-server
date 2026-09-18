package uss.code.auth.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED
import org.springframework.web.filter.OncePerRequestFilter
import tools.jackson.databind.ObjectMapper
import uss.code.global.exception.domain.JwtAuthenticationException
import uss.code.global.exception.domain.RestApiException
import uss.code.global.exception.dto.response.ErrorResponse

class JwtExceptionFilter(
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (e: JwtAuthenticationException) {
            setErrorResponse(response, SC_UNAUTHORIZED, e.code, e.message)
        } catch (e: RestApiException) {
            val exceptionCode = e.exceptionCode
            setErrorResponse(response, exceptionCode.status.value(), exceptionCode.code, exceptionCode.message)
        }
    }

    private fun setErrorResponse(
        response: HttpServletResponse,
        status: Int,
        code: String,
        message: String,
    ) {
        response.contentType = JSON_CONTENT_TYPE
        response.status = status

        val errorResponse = ErrorResponse.of(
            code = code,
            message = message,
        )

        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }

    companion object {
        private const val JSON_CONTENT_TYPE = "application/json;charset=UTF-8"
    }
}
