package uss.code.global.exception.handler

import jakarta.validation.ConstraintViolationException
import org.apache.logging.log4j.LogManager
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import tools.jackson.databind.exc.InvalidFormatException
import uss.code.global.exception.domain.ExceptionCode
import uss.code.global.exception.domain.ExceptionCode.INVALID_ENUM_TYPE
import uss.code.global.exception.domain.ExceptionCode.INVALID_REQUEST_PARAMETER
import uss.code.global.exception.domain.ExceptionCode.UNEXPECTED_SERVER_ERROR
import uss.code.global.exception.domain.JwtAuthenticationException
import uss.code.global.exception.domain.RestApiException
import uss.code.global.exception.dto.response.ErrorResponse

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(RestApiException::class)
    fun handleRestApiException(e: RestApiException): ResponseEntity<ErrorResponse> {
        log.warn("Business exception. code={}, message={}", e.exceptionCode.code, e.message)
        return makeExceptionResponse(e.exceptionCode)
    }

    @ExceptionHandler(JwtAuthenticationException::class)
    fun handleJwtAuthenticationException(e: JwtAuthenticationException): ResponseEntity<ErrorResponse> {
        log.warn("Authentication failed. code={}, message={}", e.code, e.message)

        val response = ErrorResponse.of(
            code = e.code,
            message = e.message,
        )
        return ResponseEntity.status(UNAUTHORIZED).body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = e.bindingResult.fieldErrors.joinToString("\n") { error ->
            "[ ${error.field} ] [ ${error.defaultMessage} ] [ ${error.rejectedValue} ]"
        }
        log.warn("Request validation failed. errors={}", message)

        return makeExceptionResponse(message)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(e: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        log.warn("Request parameter invalid. message={}", e.message)
        return makeExceptionResponse(INVALID_REQUEST_PARAMETER)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        log.warn("Request body unreadable. message={}", e.message)

        if (isEnumConversionFailure(e)) {
            return makeExceptionResponse(INVALID_ENUM_TYPE)
        }

        return makeExceptionResponse(INVALID_REQUEST_PARAMETER)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unexpected exception. message={}", e.message, e)
        return makeExceptionResponse(UNEXPECTED_SERVER_ERROR)
    }

    private fun isEnumConversionFailure(e: HttpMessageNotReadableException): Boolean {
        var cause = e.cause

        while (cause != null) {
            if (cause is InvalidFormatException) {
                val targetType = cause.targetType
                return targetType != null && targetType.isEnum
            }
            cause = cause.cause
        }

        return false
    }

    private fun makeExceptionResponse(exceptionCode: ExceptionCode): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(exceptionCode.status).body(makeErrorResponse(exceptionCode))
    }

    private fun makeExceptionResponse(message: String): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(BAD_REQUEST).body(makeErrorResponse(message))
    }

    private fun makeErrorResponse(exceptionCode: ExceptionCode): ErrorResponse {
        return ErrorResponse.of(
            code = exceptionCode.code,
            message = exceptionCode.message,
        )
    }

    private fun makeErrorResponse(message: String): ErrorResponse {
        return ErrorResponse.of(
            code = INVALID_REQUEST_PARAMETER.code,
            message = message,
        )
    }

    companion object {
        private val log = LogManager.getLogger(GlobalExceptionHandler::class.java)
    }
}
