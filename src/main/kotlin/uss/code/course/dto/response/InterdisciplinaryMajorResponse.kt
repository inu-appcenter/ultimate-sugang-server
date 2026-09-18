package uss.code.course.dto.response

import uss.code.course.domain.CourseDepartment

data class InterdisciplinaryMajorResponse(
    val code: String,

    val name: String,
) {
    companion object {
        fun from(department: CourseDepartment): InterdisciplinaryMajorResponse {
            return InterdisciplinaryMajorResponse(
                code = department.name,
                name = department.displayName,
            )
        }
    }
}
