package uss.code.course.dto.response

import uss.code.course.domain.CourseDepartment

@JvmRecord
data class DepartmentResponse(
    val code: String,

    val name: String,
) {
    companion object {
        @JvmStatic
        fun from(department: CourseDepartment): DepartmentResponse = DepartmentResponse(
            code = department.name,
            name = department.displayName,
        )
    }
}
