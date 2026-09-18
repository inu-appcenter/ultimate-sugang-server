package uss.code.admin.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uss.code.admin.dto.internal.SemesterRefDto
import uss.code.admin.dto.response.CourseSummaryResponse
import uss.code.course.repository.CourseRepository
import uss.code.course.repository.CourseScheduleRepository

@Service
class AdminCourseService(
    private val courseRepository: CourseRepository,
    private val courseScheduleRepository: CourseScheduleRepository,
) {
    @Transactional(readOnly = true)
    fun getSummary(): CourseSummaryResponse {
        val semester = findLoadedSemester()

        return CourseSummaryResponse.of(
            semester = semester,
            courseCount = courseRepository.count(),
            scheduleCount = courseScheduleRepository.count(),
        )
    }

    private fun findLoadedSemester(): SemesterRefDto? {
        val loaded = courseRepository.findTerms().firstOrNull()
            ?: return null

        return SemesterRefDto.of(
            academicYear = loaded.academicYear,
            term = loaded.term,
        )
    }
}
