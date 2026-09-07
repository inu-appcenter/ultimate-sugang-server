package uss.code.course.dto.response;

import lombok.Builder;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Builder(access = PRIVATE)
public record CoursesResponse(
        List<CourseResponse> courseResponses
) {
    public static CoursesResponse of(final List<CourseResponse> courseResponses) {
        return CoursesResponse.builder()
                .courseResponses(courseResponses)
                .build();
    }
}
