package uss.code.course.dto.response;

import lombok.Builder;
import uss.code.course.domain.CourseDepartment;

import static lombok.AccessLevel.PRIVATE;

@Builder(access = PRIVATE)
public record DepartmentResponse(
        String code,

        String name
) {
    public static DepartmentResponse from(final CourseDepartment department) {
        return DepartmentResponse.builder()
                .code(department.name())
                .name(department.getName())
                .build();
    }
}
