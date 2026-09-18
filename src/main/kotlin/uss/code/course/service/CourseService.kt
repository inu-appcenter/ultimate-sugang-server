package uss.code.course.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uss.code.course.domain.Course
import uss.code.course.domain.CourseArea
import uss.code.course.domain.CourseClassification
import uss.code.course.domain.CourseDepartment
import uss.code.course.dto.internal.CachedCourseDto
import uss.code.course.dto.internal.CourseCapacityDto
import uss.code.course.dto.response.*
import uss.code.course.infra.CourseCacheLoader
import uss.code.course.infra.SearchKeywordSanitizer
import uss.code.course.repository.CourseRepository
import uss.code.global.exception.domain.ExceptionCode.INVALID_GENERAL_EDUCATION_AREA
import uss.code.global.exception.domain.ExceptionCode.MEMBER_NOT_FOUND
import uss.code.global.exception.domain.RestApiException
import uss.code.member.repository.MemberRepository

@Service
class CourseService(
    private val courseRepository: CourseRepository,
    private val memberRepository: MemberRepository,

    private val courseCacheLoader: CourseCacheLoader,
) {
    @Transactional(readOnly = true)
    fun getMajorCourses(memberId: Long): CoursesResponse {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: throw RestApiException(MEMBER_NOT_FOUND)

        val departments = CourseDepartment.ownedBy(member.department)
        if (departments.isEmpty()) {
            return CoursesResponse.of(emptyList())
        }

        val cachedCourses = courseCacheLoader.loadMajorCourses(member.department)
        val capacities = courseRepository.findCapacitiesByDepartmentIn(departments)
            .associateBy { it.id }

        return CoursesResponse.of(toCourseResponses(cachedCourses.courses, capacities))
    }

    @Transactional(readOnly = true)
    fun getGeneralEducationCourses(
        classificationCode: String,
        areaCode: String?,
    ): CoursesResponse {
        val classification = CourseClassification.fromLiberalArtsScreen(classificationCode)
        val area = resolveArea(classification, areaCode)

        val cachedCourses = courseCacheLoader.loadGeneralEducationCourses(classification)
        val capacities = courseRepository.findCapacitiesByClassificationCode(classification.code)
            .associateBy { it.id }

        val courses = cachedCourses.courses.filter { area == null || area.code == it.areaCode }

        return CoursesResponse.of(toCourseResponses(courses, capacities))
    }

    @Transactional(readOnly = true)
    fun getOtherDepartmentCourses(department: String): CoursesResponse {
        val courseDepartment = CourseDepartment.fromDepartment(department)

        val courses = courseRepository.findByDepartment(courseDepartment)

        return CoursesResponse.of(toCourseResponses(courses))
    }

    @Transactional(readOnly = true)
    fun getInterdisciplinaryMajorCourses(department: String): CoursesResponse {
        val courseDepartment = CourseDepartment.fromInterdisciplinary(department)

        val courses = courseRepository.findByDepartment(courseDepartment)

        return CoursesResponse.of(toCourseResponses(courses))
    }

    @Transactional(readOnly = true)
    fun searchCourses(keyword: String): CoursesResponse {
        val sanitized: String = SearchKeywordSanitizer.sanitize(keyword)
        if (sanitized.isEmpty()) {
            return CoursesResponse.of(emptyList())
        }

        val courses = courseRepository.findByKeyword(sanitized)

        return CoursesResponse.of(toCourseResponses(courses))
    }

    @Transactional(readOnly = true)
    fun getHussCourses(): CoursesResponse {
        val courses = courseRepository.findHussCourses()

        return CoursesResponse.of(toCourseResponses(courses))
    }

    @Transactional(readOnly = true)
    fun getCategories(): CourseCategoriesResponse {
        val areasByClassification = courseRepository.findCategories()
            .groupBy { it.classificationCode }

        val categoryResponses = areasByClassification.values.map { areas ->
            CourseCategoryResponse.of(
                code = areas[0].classificationCode,
                name = areas[0].classificationName,
                areaResponses = areas.map { CourseAreaResponse.from(it) },
            )
        }

        return CourseCategoriesResponse.of(categoryResponses)
    }

    @Transactional(readOnly = true)
    fun getTerms(): CourseTermsResponse {
        val termInfos = courseRepository.findTerms()

        val termResponses = termInfos.map { CourseTermResponse.from(it) }

        return CourseTermsResponse.of(termResponses)
    }

    @Transactional(readOnly = true)
    fun getInterdisciplinaryMajors(): InterdisciplinaryMajorsResponse {
        val interdisciplinaryMajorResponses = CourseDepartment.interdisciplinaryValues()
            .map { InterdisciplinaryMajorResponse.from(it) }

        return InterdisciplinaryMajorsResponse.of(interdisciplinaryMajorResponses)
    }

    @Transactional(readOnly = true)
    fun getDepartments(): DepartmentsResponse {
        val departmentResponses = CourseDepartment.departmentValues()
            .map { DepartmentResponse.from(it) }

        return DepartmentsResponse.of(departmentResponses)
    }

    private fun resolveArea(
        classification: CourseClassification,
        areaCode: String?,
    ): CourseArea? {
        if (areaCode == null) {
            return null
        }

        return CourseArea.tryFromCode(areaCode)
            .filter { classification.hasArea(it) }
            .orElseThrow { RestApiException(INVALID_GENERAL_EDUCATION_AREA) }
    }

    private fun toCourseResponses(
        courses: List<CachedCourseDto>,
        capacities: Map<Long, CourseCapacityDto>,
    ): List<CourseResponse> {
        return courses.mapNotNull { course ->
            capacities[course.id]?.let { capacity -> CourseResponse.of(course, capacity) }
        }
    }

    private fun toCourseResponses(courses: List<Course>): List<CourseResponse> {
        return courses.map { CourseResponse.from(it) }
    }
}
