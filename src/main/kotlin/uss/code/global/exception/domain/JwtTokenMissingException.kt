package uss.code.global.exception.domain

class JwtTokenMissingException(
    exceptionCode: ExceptionCode,
) : JwtAuthenticationException(exceptionCode)
