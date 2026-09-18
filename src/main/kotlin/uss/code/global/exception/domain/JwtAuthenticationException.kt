package uss.code.global.exception.domain

open class JwtAuthenticationException(
    exceptionCode: ExceptionCode,
) : RuntimeException() {
    val code: String = exceptionCode.code
    override val message: String = exceptionCode.message
}
