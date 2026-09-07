package uss.code.registration.dto.response;

import lombok.Builder;
import uss.code.course.domain.Course;
import uss.code.course.dto.response.CourseResponse;

import static lombok.AccessLevel.PRIVATE;

@Builder(access = PRIVATE)
public record RegistrationResponse(
        CourseResponse courseResponse
) {
    public static RegistrationResponse from(final Course course) {
        return RegistrationResponse.builder()
                .courseResponse(CourseResponse.from(course))
                .build();
    }
}
