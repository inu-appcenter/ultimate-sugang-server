package uss.code.course.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uss.code.course.domain.CourseClassification;
import uss.code.course.domain.CourseDepartment;
import uss.code.course.dto.common.CachedCourse;
import uss.code.course.dto.common.CachedCourses;
import uss.code.course.repository.CourseRepository;
import uss.code.member.domain.MemberDepartment;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CourseCacheLoader {

    public static final String MAJOR_COURSES = "major-courses";
    public static final String GENERAL_EDUCATION_COURSES = "general-education-courses";

    private final CourseRepository courseRepository;

    @Cacheable(cacheNames = MAJOR_COURSES, key = "#memberDepartment.name()")
    @Transactional(readOnly = true)
    public CachedCourses loadMajorCourses(final MemberDepartment memberDepartment) {
        return readMajorCourses(memberDepartment);
    }

    @CachePut(cacheNames = MAJOR_COURSES, key = "#memberDepartment.name()")
    @Transactional(readOnly = true)
    public CachedCourses refreshMajorCourses(final MemberDepartment memberDepartment) {
        return readMajorCourses(memberDepartment);
    }

    @Cacheable(cacheNames = GENERAL_EDUCATION_COURSES, key = "#classification.name()")
    @Transactional(readOnly = true)
    public CachedCourses loadGeneralEducationCourses(final CourseClassification classification) {
        return readGeneralEducationCourses(classification);
    }

    @CachePut(cacheNames = GENERAL_EDUCATION_COURSES, key = "#classification.name()")
    @Transactional(readOnly = true)
    public CachedCourses refreshGeneralEducationCourses(final CourseClassification classification) {
        return readGeneralEducationCourses(classification);
    }

    private CachedCourses readMajorCourses(final MemberDepartment memberDepartment) {
        final List<CourseDepartment> departments = CourseDepartment.ownedBy(memberDepartment);

        final List<CachedCourse> courses = courseRepository.findByDepartmentIn(departments).stream()
                .map(CachedCourse::from)
                .toList();

        return CachedCourses.of(courses);
    }

    private CachedCourses readGeneralEducationCourses(final CourseClassification classification) {
        final List<CachedCourse> courses = courseRepository.findByClassificationCode(classification.getCode()).stream()
                .map(CachedCourse::from)
                .toList();

        return CachedCourses.of(courses);
    }
}
