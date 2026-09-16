package uss.code.course.dto.response

@JvmRecord
data class InterdisciplinaryMajorsResponse(
    val interdisciplinaryMajorResponses: List<InterdisciplinaryMajorResponse>,
) {
    companion object {
        @JvmStatic
        fun of(
            interdisciplinaryMajorResponses: List<InterdisciplinaryMajorResponse>,
        ): InterdisciplinaryMajorsResponse = InterdisciplinaryMajorsResponse(
            interdisciplinaryMajorResponses = interdisciplinaryMajorResponses,
        )
    }
}
