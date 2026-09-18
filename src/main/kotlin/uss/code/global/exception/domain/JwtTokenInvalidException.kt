package uss.code.global.exception.domain

class JwtTokenInvalidException(
    exceptionCode: ExceptionCode,
) : JwtAuthenticationException(exceptionCode)
