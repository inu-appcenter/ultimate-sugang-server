package uss.code.course.infra

import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uss.code.course.domain.CourseClassification
import uss.code.course.domain.CourseDepartment
import uss.code.course.dto.internal.CachedCourseDto
import uss.code.course.dto.internal.CachedCoursesDto
import uss.code.course.repository.CourseRepository
import uss.code.member.domain.MemberDepartment

@Component
class CourseCacheLoader(
    private val courseRepository: CourseRepository,
) {
    @Cacheable(cacheNames = [MAJOR_COURSES], key = "#memberDepartment.name()")
    @Transactional(readOnly = true)
    fun loadMajorCourses(memberDepartment: MemberDepartment): CachedCoursesDto {
        return readMajorCourses(memberDepartment)
    }

    @CachePut(cacheNames = [MAJOR_COURSES], key = "#memberDepartment.name()")
    @Transactional(readOnly = true)
    fun refreshMajorCourses(memberDepartment: MemberDepartment): CachedCoursesDto {
        return readMajorCourses(memberDepartment)
    }

    @Cacheable(cacheNames = [GENERAL_EDUCATION_COURSES], key = "#classification.name()")
    @Transactional(readOnly = true)
    fun loadGeneralEducationCourses(classification: CourseClassification): CachedCoursesDto {
        return readGeneralEducationCourses(classification)
    }

    @CachePut(cacheNames = [GENERAL_EDUCATION_COURSES], key = "#classification.name()")
    @Transactional(readOnly = true)
    fun refreshGeneralEducationCourses(classification: CourseClassification): CachedCoursesDto {
        return readGeneralEducationCourses(classification)
    }

    private fun readMajorCourses(memberDepartment: MemberDepartment): CachedCoursesDto {
        val departments = CourseDepartment.ownedBy(memberDepartment)

        val courses = courseRepository.findByDepartmentIn(departments)
            .map { CachedCourseDto.from(it) }

        return CachedCoursesDto.of(courses)
    }

    private fun readGeneralEducationCourses(classification: CourseClassification): CachedCoursesDto {
        val courses = courseRepository.findByClassificationCode(classification.code)
            .map { CachedCourseDto.from(it) }

        return CachedCoursesDto.of(courses)
    }

    companion object {
        const val MAJOR_COURSES = "major-courses"
        const val GENERAL_EDUCATION_COURSES = "general-education-courses"
    }
}
