package uss.code.global.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.LogManager
import org.slf4j.MDC
import org.springframework.web.filter.OncePerRequestFilter
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.UUID

class HttpLoggingFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        MDC.put(TRACE_ID_KEY, generateTraceId())

        try {
            if (isExcluded(request)) {
                filterChain.doFilter(request, response)
                return
            }

            logRequest(request)
            filterChain.doFilter(request, response)
            logResponse(request, response)
        } finally {
            MDC.remove(TRACE_ID_KEY)
        }
    }

    private fun generateTraceId(): String {
        return UUID.randomUUID()
            .toString()
            .replace("-", "")
            .take(TRACE_ID_LENGTH)
    }

    private fun isExcluded(request: HttpServletRequest): Boolean {
        val uri: String = request.requestURI

        return EXCLUDE_PREFIXES.any { uri.startsWith(it) }
    }

    private fun logRequest(request: HttpServletRequest) {
        log.info("[REQUEST] {} {}", request.method, buildUri(request))
    }

    private fun logResponse(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val memberId: Any? = request.getAttribute(MEMBER_ID_ATTRIBUTE)

        log.info("[RESPONSE] {} {} memberId={} status={}", request.method, buildUri(request), memberId, response.status)
    }

    private fun buildUri(request: HttpServletRequest): String {
        val path: String = request.requestURI
        val query: String? = request.queryString

        if (query == null || query.all(Character::isWhitespace)) {
            return path
        }

        return "$path?${maskSensitiveParams(query)}"
    }

    private fun maskSensitiveParams(query: String): String {
        return query.split(QUERY_DELIMITER)
            .dropLastWhile { it.isEmpty() }
            .joinToString(QUERY_DELIMITER) { maskIfSensitive(it) }
    }

    private fun maskIfSensitive(param: String): String {
        val delimiterIndex = param.indexOf(KEY_VALUE_DELIMITER)

        if (delimiterIndex < 0) {
            return decode(param)
        }

        val key = decode(param.substring(0, delimiterIndex))

        if (isSensitiveKey(key)) {
            return "$key$KEY_VALUE_DELIMITER$MASK_VALUE"
        }

        val value = decode(param.substring(delimiterIndex + 1))

        return "$key$KEY_VALUE_DELIMITER$value"
    }

    private fun decode(value: String): String {
        return try {
            URLDecoder.decode(value, StandardCharsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            value
        }
    }

    private fun isSensitiveKey(key: String): Boolean {
        return SENSITIVE_KEYS.any { it.equals(key, ignoreCase = true) }
    }

    companion object {
        private val log = LogManager.getLogger(HttpLoggingFilter::class.java)

        private const val TRACE_ID_KEY = "traceId"
        private const val TRACE_ID_LENGTH = 16

        private const val MEMBER_ID_ATTRIBUTE = "member-id"

        private val EXCLUDE_PREFIXES = listOf(
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
        )

        private val SENSITIVE_KEYS = listOf("access-token", "password")
        private const val MASK_VALUE = "****"

        private const val QUERY_DELIMITER = "&"
        private const val KEY_VALUE_DELIMITER = "="
    }
}
