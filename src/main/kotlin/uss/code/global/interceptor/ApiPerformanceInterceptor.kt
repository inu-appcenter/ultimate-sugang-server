package uss.code.global.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class ApiPerformanceInterceptor : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        request.setAttribute(START_TIME_ATTRIBUTE, System.nanoTime())

        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?,
    ) {
        val startTime = request.getAttribute(START_TIME_ATTRIBUTE) as Long?

        if (startTime == null) {
            return
        }

        val responseTime = (System.nanoTime() - startTime) / NANOS_PER_MILLI
        val method: String = request.method
        val uri: String = request.requestURI
        val status = response.status

        if (responseTime > RESPONSE_TIME_THRESHOLD_MS) {
            log.warn(LOG_FORMAT, method, uri, responseTime, status)
            return
        }

        log.info(LOG_FORMAT, method, uri, responseTime, status)
    }

    companion object {
        private val log = LogManager.getLogger("API_PERF")

        private const val LOG_FORMAT = "type=API_PERFORMANCE method={} uri={} response_time={} status={}"

        private const val START_TIME_ATTRIBUTE = "start-time"
        private const val RESPONSE_TIME_THRESHOLD_MS = 3_000L
        private const val NANOS_PER_MILLI = 1_000_000L
    }
}
