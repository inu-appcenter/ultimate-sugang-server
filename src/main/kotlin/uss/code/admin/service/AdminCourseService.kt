package uss.code.admin.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uss.code.admin.domain.SyncJobStatus.RUNNING
import uss.code.admin.dto.common.LastJobInfo
import uss.code.admin.dto.internal.SemesterRefDto
import uss.code.admin.dto.response.CourseSummaryResponse
import uss.code.admin.repository.CourseSyncJobRepository
import uss.code.course.repository.CourseRepository
import uss.code.course.repository.CourseScheduleRepository

@Service
class AdminCourseService(
    private val courseRepository: CourseRepository,
    private val courseScheduleRepository: CourseScheduleRepository,
    private val courseSyncJobRepository: CourseSyncJobRepository,
) {
    @Transactional(readOnly = true)
    fun getSummary(): CourseSummaryResponse {
        val semester = findLoadedSemester()

        val lastJob: LastJobInfo? = courseSyncJobRepository.findFirstByOrderByStartedAtDesc()
            .map { LastJobInfo.from(it) }
            .orElse(null)

        val runningJobId: Long? = courseSyncJobRepository.findFirstByStatus(RUNNING)
            .map { it.id }
            .orElse(null)

        return CourseSummaryResponse.of(
            semester = semester,
            courseCount = courseRepository.count(),
            scheduleCount = courseScheduleRepository.count(),
            lastJob = lastJob,
            runningJobId = runningJobId,
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
