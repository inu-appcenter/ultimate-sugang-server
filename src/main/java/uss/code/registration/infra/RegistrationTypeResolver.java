package uss.code.registration.infra;

import lombok.experimental.UtilityClass;
import uss.code.course.domain.Course;
import uss.code.course.domain.CourseDepartment;
import uss.code.member.domain.Member;

import java.util.Set;

import static uss.code.course.domain.CourseClassification.GENERAL_ELECTIVE;
import static uss.code.course.domain.CourseClassification.MAJOR_ADVANCED;
import static uss.code.course.domain.CourseClassification.MAJOR_BASIC;
import static uss.code.course.domain.CourseClassification.MAJOR_CORE;

@UtilityClass
public class RegistrationTypeResolver {

    private static final String GENERAL_ELECTIVE_NAME = GENERAL_ELECTIVE.getName();
    private static final Set<String> MAJOR_CLASSIFICATION_CODES = Set.of(
            MAJOR_BASIC.getCode(), MAJOR_CORE.getCode(), MAJOR_ADVANCED.getCode()
    );

    public static String resolve(
            final Member member,
            final Course course
    ) {
        if (isOtherDepartmentMajor(member, course)) {
            return GENERAL_ELECTIVE_NAME;
        }

        return course.getClassificationName();
    }

    private static boolean isOtherDepartmentMajor(
            final Member member,
            final Course course
    ) {
        final boolean ownDepartment = CourseDepartment.ownedBy(member.getDepartment())
                .contains(course.getDepartment());

        return !ownDepartment && MAJOR_CLASSIFICATION_CODES.contains(course.getClassificationCode());
    }
}
