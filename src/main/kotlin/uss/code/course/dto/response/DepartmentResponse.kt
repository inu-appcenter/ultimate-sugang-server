package uss.code.course.dto.response

import uss.code.course.domain.CourseDepartment

data class DepartmentResponse(
    val code: String,

    val name: String,
) {
    companion object {
        fun from(department: CourseDepartment): DepartmentResponse {
            return DepartmentResponse(
                code = department.name,
                name = department.displayName,
            )
        }
    }
}
