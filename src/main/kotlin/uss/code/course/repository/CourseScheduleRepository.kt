package uss.code.course.repository

import org.springframework.data.jpa.repository.JpaRepository
import uss.code.course.domain.CourseSchedule

interface CourseScheduleRepository : JpaRepository<CourseSchedule, Long>
