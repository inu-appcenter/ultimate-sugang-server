package uss.code.course.dto.response;

import lombok.Builder;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Builder(access = PRIVATE)
public record DepartmentsResponse(
        List<DepartmentResponse> departmentResponses
) {
    public static DepartmentsResponse of(final List<DepartmentResponse> departmentResponses) {
        return DepartmentsResponse.builder()
                .departmentResponses(departmentResponses)
                .build();
    }
}
