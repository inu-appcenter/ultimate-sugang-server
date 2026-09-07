package uss.code.registration.dto.response;

import lombok.Builder;
import uss.code.course.dto.response.CourseResponse;
import uss.code.registration.domain.Registration;

import java.time.format.DateTimeFormatter;

import static lombok.AccessLevel.PRIVATE;

@Builder(access = PRIVATE)
public record RegistrationCourseResponse(
        String studentId,

        String resolvedType,

        String reAttendance,

        String createdAt,

        CourseResponse courseResponse
) {
    private static final String NO_RE_ATTENDANCE = "";
    private static final DateTimeFormatter CREATED_AT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public static RegistrationCourseResponse of(
            final Registration registration,
            final String studentId,
            final String resolvedType
    ) {
        return RegistrationCourseResponse.builder()
                .studentId(studentId)
                .resolvedType(resolvedType)
                .reAttendance(NO_RE_ATTENDANCE)
                .createdAt(registration.getCreatedAt().format(CREATED_AT_FORMAT))
                .courseResponse(CourseResponse.from(registration.getCourse()))
                .build();
    }
}
