package uss.code.course.dto.response;

import lombok.Builder;
import uss.code.course.domain.Course;
import uss.code.course.dto.common.CachedCourse;
import uss.code.course.dto.common.CourseCapacity;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Builder(access = PRIVATE)
public record CourseResponse(
        String id,

        String code,

        String courseCode,

        String name,

        String nameEn,

        String englishCourseName,

        String professor,

        int credits,

        int capacity,

        int enrolled,

        int cartCount,

        String courseType,

        String courseArea,

        String department,

        String grade,

        String schedule,

        List<String> tags,

        boolean isEnglish,

        boolean isNight,

        boolean isClosed
) {
    public static CourseResponse from(final Course course) {
        return CourseResponse.of(
                CachedCourse.from(course),
                course.getMaxCapacity(),
                course.getCurrentEnrollment(),
                course.getCartCount(),
                !(course.isActive() && course.isRegisterable())
        );
    }

    public static CourseResponse of(
            final CachedCourse course,
            final CourseCapacity capacity
    ) {
        return CourseResponse.of(
                course,
                capacity.maxCapacity(),
                capacity.currentEnrollment(),
                capacity.cartCount(),
                !capacity.isRegisterable()
        );
    }

    private static CourseResponse of(
            final CachedCourse course,
            final int capacity,
            final int enrolled,
            final int cartCount,
            final boolean isClosed
    ) {
        return CourseResponse.builder()
                .id(String.valueOf(course.id()))
                .code(course.code())
                .courseCode(course.courseCode())
                .name(course.name())
                .nameEn(course.nameEn())
                .englishCourseName(course.englishCourseName())
                .professor(course.professor())
                .credits(course.credits())
                .capacity(capacity)
                .enrolled(enrolled)
                .cartCount(cartCount)
                .courseType(course.courseType())
                .courseArea(course.courseArea())
                .department(course.department())
                .grade(course.grade())
                .schedule(course.schedule())
                .tags(course.tags())
                .isEnglish(course.isEnglish())
                .isNight(course.isNight())
                .isClosed(isClosed)
                .build();
    }
}
