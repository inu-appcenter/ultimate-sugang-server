package uss.code.course.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uss.code.course.domain.Course;
import uss.code.course.domain.CourseArea;
import uss.code.course.domain.CourseClassification;
import uss.code.course.domain.CourseDepartment;
import uss.code.course.dto.common.CachedCourse;
import uss.code.course.dto.common.CachedCourses;
import uss.code.course.dto.common.CourseCapacity;
import uss.code.course.dto.common.CourseCategory;
import uss.code.course.dto.common.CourseTermInfo;
import uss.code.course.dto.response.CourseAreaResponse;
import uss.code.course.dto.response.CourseCategoriesResponse;
import uss.code.course.dto.response.CourseCategoryResponse;
import uss.code.course.dto.response.CourseResponse;
import uss.code.course.dto.response.CourseTermResponse;
import uss.code.course.dto.response.CourseTermsResponse;
import uss.code.course.dto.response.CoursesResponse;
import uss.code.course.dto.response.DepartmentResponse;
import uss.code.course.dto.response.DepartmentsResponse;
import uss.code.course.dto.response.InterdisciplinaryMajorResponse;
import uss.code.course.dto.response.InterdisciplinaryMajorsResponse;
import uss.code.course.infra.CourseCacheLoader;
import uss.code.course.infra.SearchKeywordSanitizer;
import uss.code.course.repository.CourseRepository;
import uss.code.global.exception.domain.RestApiException;
import uss.code.member.domain.Member;
import uss.code.member.domain.MemberDepartment;
import uss.code.member.repository.MemberRepository;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uss.code.global.exception.domain.ExceptionCode.INVALID_GENERAL_EDUCATION_AREA;
import static uss.code.global.exception.domain.ExceptionCode.MEMBER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseCacheLoader courseCacheLoader;

    private final CourseRepository courseRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public CoursesResponse getMajorCourses(final long memberId) {
        final Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RestApiException(MEMBER_NOT_FOUND));

        final List<CourseDepartment> departments = CourseDepartment.ownedBy(member.getDepartment());
        if (departments.isEmpty()) {
            return CoursesResponse.of(List.of());
        }

        final CachedCourses cachedCourses = courseCacheLoader.loadMajorCourses(member.getDepartment());
        final Map<Long, CourseCapacity> capacities = courseRepository.findCapacitiesByDepartmentIn(departments).stream()
                .collect(toMap(CourseCapacity::id, identity()));

        return CoursesResponse.of(toCourseResponses(cachedCourses.courses(), capacities));
    }

    @Transactional(readOnly = true)
    public CoursesResponse getGeneralEducationCourses(
            final String classificationCode,
            final String areaCode
    ) {
        final CourseClassification classification = CourseClassification.fromLiberalArtsScreen(classificationCode);
        final Optional<CourseArea> area = resolveArea(classification, areaCode);

        final CachedCourses cachedCourses = courseCacheLoader.loadGeneralEducationCourses(classification);
        final Map<Long, CourseCapacity> capacities = courseRepository.findCapacitiesByClassificationCode(classification.getCode()).stream()
                .collect(toMap(CourseCapacity::id, identity()));

        final List<CachedCourse> courses = cachedCourses.courses().stream()
                .filter(course -> area.map(value -> value.getCode().equals(course.areaCode())).orElse(true))
                .toList();

        return CoursesResponse.of(toCourseResponses(courses, capacities));
    }

    @Transactional(readOnly = true)
    public CoursesResponse getOtherDepartmentCourses(final String department) {
        final MemberDepartment memberDepartment = MemberDepartment.from(department);

        final List<CourseDepartment> departments = CourseDepartment.ownedBy(memberDepartment);
        if (departments.isEmpty()) {
            return CoursesResponse.of(List.of());
        }

        final List<Course> courses = courseRepository.findByDepartmentIn(departments);

        return CoursesResponse.of(toCourseResponses(courses));
    }

    @Transactional(readOnly = true)
    public CoursesResponse getInterdisciplinaryMajorCourses(final String department) {
        final CourseDepartment courseDepartment = CourseDepartment.fromInterdisciplinary(department);

        final List<Course> courses = courseRepository.findByDepartment(courseDepartment);

        return CoursesResponse.of(toCourseResponses(courses));
    }

    @Transactional(readOnly = true)
    public CoursesResponse searchCourses(final String keyword) {
        final String sanitized = SearchKeywordSanitizer.sanitize(keyword);
        if (sanitized.isEmpty()) {
            return CoursesResponse.of(List.of());
        }

        final List<Course> courses = courseRepository.findByKeyword(sanitized);

        return CoursesResponse.of(toCourseResponses(courses));
    }

    @Transactional(readOnly = true)
    public CoursesResponse getHussCourses() {
        final List<Course> courses = courseRepository.findHussCourses();

        return CoursesResponse.of(toCourseResponses(courses));
    }

    @Transactional(readOnly = true)
    public CourseCategoriesResponse getCategories() {
        final Map<String, List<CourseCategory>> areasByClassification = courseRepository.findCategories().stream()
                .collect(groupingBy(CourseCategory::classificationCode, LinkedHashMap::new, toList()));

        final List<CourseCategoryResponse> categoryResponses = areasByClassification.values().stream()
                .map(areas -> CourseCategoryResponse.of(
                        areas.get(0).classificationCode(),
                        areas.get(0).classificationName(),
                        areas.stream().map(CourseAreaResponse::from).toList()
                ))
                .toList();

        return CourseCategoriesResponse.of(categoryResponses);
    }

    @Transactional(readOnly = true)
    public CourseTermsResponse getTerms() {
        final List<CourseTermInfo> termInfos = courseRepository.findTerms();

        final List<CourseTermResponse> termResponses = termInfos.stream()
                .map(CourseTermResponse::from)
                .toList();

        return CourseTermsResponse.of(termResponses);
    }

    @Transactional(readOnly = true)
    public InterdisciplinaryMajorsResponse getInterdisciplinaryMajors() {
        final List<CourseDepartment> interdisciplinaryDepartments = CourseDepartment.interdisciplinaryValues();
        final List<CourseDepartment> existingDepartments = courseRepository.findDepartmentsIn(interdisciplinaryDepartments);

        final List<InterdisciplinaryMajorResponse> interdisciplinaryMajorResponses = interdisciplinaryDepartments.stream()
                .filter(existingDepartments::contains)
                .map(InterdisciplinaryMajorResponse::from)
                .toList();

        return InterdisciplinaryMajorsResponse.of(interdisciplinaryMajorResponses);
    }

    @Transactional(readOnly = true)
    public DepartmentsResponse getDepartments() {
        final List<CourseDepartment> ownedDepartments = Arrays.stream(CourseDepartment.values())
                .filter(CourseDepartment::hasOwner)
                .toList();
        final List<CourseDepartment> existingDepartments = courseRepository.findDepartmentsIn(ownedDepartments);

        final List<DepartmentResponse> departmentResponses = Arrays.stream(MemberDepartment.values())
                .filter(department -> CourseDepartment.ownedBy(department).stream().anyMatch(existingDepartments::contains))
                .map(DepartmentResponse::from)
                .toList();

        return DepartmentsResponse.of(departmentResponses);
    }

    private Optional<CourseArea> resolveArea(
            final CourseClassification classification,
            final String areaCode
    ) {
        if (areaCode == null) {
            return Optional.empty();
        }

        final CourseArea area = CourseArea.tryFromCode(areaCode)
                .filter(classification::hasArea)
                .orElseThrow(() -> new RestApiException(INVALID_GENERAL_EDUCATION_AREA));

        return Optional.of(area);
    }

    private List<CourseResponse> toCourseResponses(
            final List<CachedCourse> courses,
            final Map<Long, CourseCapacity> capacities
    ) {
        return courses.stream()
                .filter(course -> capacities.containsKey(course.id()))
                .map(course -> CourseResponse.of(course, capacities.get(course.id())))
                .toList();
    }

    private List<CourseResponse> toCourseResponses(final List<Course> courses) {
        return courses.stream()
                .map(CourseResponse::from)
                .toList();
    }
}
