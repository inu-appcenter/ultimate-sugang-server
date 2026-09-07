package uss.code.course.dto.common;

import lombok.Builder;
import uss.code.course.domain.Course;
import uss.code.course.domain.CourseType;
import uss.code.course.infra.CourseScheduleFormatter;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Builder(access = PRIVATE)
public record CachedCourse(
        long id,

        String code,

        String courseCode,

        String name,

        String nameEn,

        String englishCourseName,

        String professor,

        int credits,

        String courseType,

        String courseArea,

        String areaCode,

        String department,

        String grade,

        String schedule,

        List<String> tags,

        boolean isEnglish,

        boolean isNight
) {
    private static final String NO_PROFESSOR = "";
    private static final String NO_ENGLISH_COURSE_NAME = "";
    private static final String NO_COURSE_AREA = "";
    private static final String LONG_LESSON_TAG = "75분수업";

    public static CachedCourse from(final Course course) {
        return CachedCourse.builder()
                .id(course.getId())
                .code(course.getHaksuCode())
                .courseCode(course.getCourseCode())
                .name(course.getTitleKr())
                .nameEn(course.getTitleEn())
                .englishCourseName(course.isEnglishCourse() ? course.getEnglishName() : NO_ENGLISH_COURSE_NAME)
                .professor(NO_PROFESSOR)
                .credits(course.getCredits())
                .courseType(course.getClassificationName())
                .courseArea(course.getArea().isGeneralEducationArea() ? course.getAreaName() : NO_COURSE_AREA)
                .areaCode(course.getAreaCode())
                .department(course.getDepartment().getName())
                .grade(course.getGradeName())
                .schedule(CourseScheduleFormatter.format(course.getSchedules()))
                .tags(buildTags(course))
                .isEnglish(course.isEnglishCourse())
                .isNight(course.getDepartment().isNight())
                .build();
    }

    private static List<String> buildTags(final Course course) {
        final List<String> tags = new ArrayList<>();

        if (course.is75MinLesson()) {
            tags.add(LONG_LESSON_TAG);
        }
        if (CourseType.isTagType(course.getTypeCode())) {
            tags.add(course.getTypeName());
        }

        return List.copyOf(tags);
    }
}
