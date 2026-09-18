package uss.code.course.dto.response

data class InterdisciplinaryMajorsResponse(
    val interdisciplinaryMajorResponses: List<InterdisciplinaryMajorResponse>,
) {
    companion object {
        fun of(
            interdisciplinaryMajorResponses: List<InterdisciplinaryMajorResponse>,
        ): InterdisciplinaryMajorsResponse {
            return InterdisciplinaryMajorsResponse(
                interdisciplinaryMajorResponses = interdisciplinaryMajorResponses,
            )
        }
    }
}
