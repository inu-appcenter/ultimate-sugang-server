package uss.code.course.dto.response

@JvmRecord
data class DepartmentsResponse(
    val departmentResponses: List<DepartmentResponse>,
) {
    companion object {
        @JvmStatic
        fun of(departmentResponses: List<DepartmentResponse>): DepartmentsResponse =
            DepartmentsResponse(departmentResponses = departmentResponses)
    }
}
