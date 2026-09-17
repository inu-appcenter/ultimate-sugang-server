package uss.code.global.exception.dto.response

@JvmRecord
data class ErrorResponse(
    val code: String,
    val message: String,
) {
    companion object {
        @JvmStatic
        fun of(
            code: String,
            message: String,
        ): ErrorResponse {
            return ErrorResponse(
                code = code,
                message = message,
            )
        }
    }
}
