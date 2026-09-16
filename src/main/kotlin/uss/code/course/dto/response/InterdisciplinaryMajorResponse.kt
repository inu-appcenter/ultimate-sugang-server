package uss.code.course.dto.response

import uss.code.course.domain.CourseDepartment

@JvmRecord
data class InterdisciplinaryMajorResponse(
    val code: String,

    val name: String,
) {
    companion object {
        @JvmStatic
        fun from(department: CourseDepartment): InterdisciplinaryMajorResponse = InterdisciplinaryMajorResponse(
            code = department.name,
            name = department.displayName,
        )
    }
}
