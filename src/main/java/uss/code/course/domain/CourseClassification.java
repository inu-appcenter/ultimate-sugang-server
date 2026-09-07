package uss.code.course.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uss.code.global.exception.domain.RestApiException;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static uss.code.global.exception.domain.ExceptionCode.INVALID_ENUM_TYPE;
import static uss.code.global.exception.domain.ExceptionCode.INVALID_GENERAL_EDUCATION_CLASSIFICATION;

@Getter
@RequiredArgsConstructor
public enum CourseClassification {
    MAJOR_ADVANCED("41", "전공심화"),
    MAJOR_BASIC("25", "전공기초"),
    MAJOR_CORE("31", "전공핵심"),
    BASIC_LIBERAL_ARTS("11", "기초교양"),
    CORE_LIBERAL_ARTS("21", "핵심교양"),
    ADVANCED_LIBERAL_ARTS("23", "심화교양"),
    TEACHING("50", "교직"),
    GENERAL_ELECTIVE("80", "일반선택"),
    MILITARY("70", "군사학");

    private static final Set<CourseClassification> LIBERAL_ARTS_SCREEN = EnumSet.of(
            BASIC_LIBERAL_ARTS, CORE_LIBERAL_ARTS, ADVANCED_LIBERAL_ARTS, TEACHING, GENERAL_ELECTIVE, MILITARY
    );

    private static final Map<CourseClassification, Set<CourseArea>> AREAS = Map.of(
            MAJOR_ADVANCED, EnumSet.of(CourseArea.MAJOR_ADVANCED),
            MAJOR_BASIC, EnumSet.of(CourseArea.MAJOR_BASIC),
            MAJOR_CORE, EnumSet.of(CourseArea.MAJOR_CORE),
            BASIC_LIBERAL_ARTS, EnumSet.of(CourseArea.ACADEMIC_FOUNDATION, CourseArea.BASIC_SCIENCE_ENGINEERING),
            CORE_LIBERAL_ARTS, EnumSet.of(
                    CourseArea.CORE_INU_SEMINAR, CourseArea.CORE_HUMANITIES, CourseArea.CORE_SOCIAL,
                    CourseArea.CORE_SCIENCE_TECHNOLOGY, CourseArea.CORE_ARTS_SPORTS, CourseArea.CORE_FOREIGN_LANGUAGE
            ),
            ADVANCED_LIBERAL_ARTS, EnumSet.of(
                    CourseArea.HUMANITIES, CourseArea.SOCIAL, CourseArea.SCIENCE_TECHNOLOGY,
                    CourseArea.ARTS_SPORTS, CourseArea.FOREIGN_LANGUAGE
            ),
            TEACHING, EnumSet.of(CourseArea.TEACHING),
            GENERAL_ELECTIVE, EnumSet.of(CourseArea.GENERAL_ELECTIVE),
            MILITARY, EnumSet.of(CourseArea.MILITARY)
    );

    private final String code;
    private final String name;

    public static CourseClassification fromCode(final String code) {
        return Arrays.stream(values())
                .filter(classification -> !classification.code.isBlank())
                .filter(classification -> classification.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new RestApiException(INVALID_ENUM_TYPE));
    }

    public static CourseClassification fromLiberalArtsScreen(final String code) {
        final CourseClassification classification = fromCode(code);

        if (!classification.isLiberalArtsScreen()) {
            throw new RestApiException(INVALID_GENERAL_EDUCATION_CLASSIFICATION);
        }

        return classification;
    }

    public boolean isLiberalArtsScreen() {
        return LIBERAL_ARTS_SCREEN.contains(this);
    }

    public boolean hasArea(final CourseArea area) {
        return AREAS.getOrDefault(this, Set.of()).contains(area);
    }
}
