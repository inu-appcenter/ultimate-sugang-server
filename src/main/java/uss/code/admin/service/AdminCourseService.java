package uss.code.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uss.code.admin.domain.CourseSyncJob;
import uss.code.admin.dto.common.LastJobInfo;
import uss.code.admin.dto.internal.SemesterRefDto;
import uss.code.admin.dto.response.CourseSummaryResponse;
import uss.code.admin.repository.CourseSyncJobRepository;
import uss.code.course.dto.internal.CourseTermInfoDto;
import uss.code.course.repository.CourseRepository;
import uss.code.course.repository.CourseScheduleRepository;

import java.util.List;

import static uss.code.admin.domain.SyncJobStatus.RUNNING;

@Service
@RequiredArgsConstructor
public class AdminCourseService {

    private final CourseRepository courseRepository;
    private final CourseScheduleRepository courseScheduleRepository;
    private final CourseSyncJobRepository courseSyncJobRepository;

    @Transactional(readOnly = true)
    public CourseSummaryResponse getSummary() {
        final SemesterRefDto semester = findLoadedSemester();

        final LastJobInfo lastJob = courseSyncJobRepository.findFirstByOrderByStartedAtDesc()
                .map(LastJobInfo::from)
                .orElse(null);

        final Long runningJobId = courseSyncJobRepository.findFirstByStatus(RUNNING)
                .map(CourseSyncJob::getId)
                .orElse(null);

        return CourseSummaryResponse.of(
                semester,
                courseRepository.count(),
                courseScheduleRepository.count(),
                lastJob,
                runningJobId
        );
    }

    private SemesterRefDto findLoadedSemester() {
        final List<CourseTermInfoDto> loadedSemesters = courseRepository.findTerms();

        if (loadedSemesters.isEmpty()) {
            return null;
        }

        final CourseTermInfoDto loaded = loadedSemesters.get(0);

        return SemesterRefDto.of(loaded.academicYear(), loaded.term());
    }
}
