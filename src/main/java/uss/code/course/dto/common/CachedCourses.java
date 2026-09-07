package uss.code.course.dto.common;

import lombok.Builder;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Builder(access = PRIVATE)
public record CachedCourses(
        List<CachedCourse> courses
) {
    public static CachedCourses of(final List<CachedCourse> courses) {
        return CachedCourses.builder()
                .courses(courses)
                .build();
    }
}
