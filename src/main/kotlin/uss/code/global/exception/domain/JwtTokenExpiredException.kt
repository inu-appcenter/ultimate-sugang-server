package uss.code.global.exception.domain

class JwtTokenExpiredException(
    exceptionCode: ExceptionCode,
) : JwtAuthenticationException(exceptionCode)
