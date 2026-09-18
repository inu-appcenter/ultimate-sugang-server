package uss.code.course.dto.response

data class DepartmentsResponse(
    val departmentResponses: List<DepartmentResponse>,
) {
    companion object {
        fun of(departmentResponses: List<DepartmentResponse>): DepartmentsResponse {
            return DepartmentsResponse(departmentResponses = departmentResponses)
        }
    }
}
