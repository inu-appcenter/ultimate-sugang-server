package uss.code.global.exception.domain

class RestApiException(
    val exceptionCode: ExceptionCode,
) : RuntimeException()
