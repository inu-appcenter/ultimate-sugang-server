package uss.code.global.exception.handler

import org.apache.logging.log4j.LogManager
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import java.lang.reflect.Method

class AsyncExceptionHandler : AsyncUncaughtExceptionHandler {
    override fun handleUncaughtException(
        ex: Throwable,
        method: Method,
        vararg params: Any?,
    ) {
        log.error("Uncaught async exception. method={}, message={}", method.name, ex.message, ex)
    }

    companion object {
        private val log = LogManager.getLogger(AsyncExceptionHandler::class.java)
    }
}
