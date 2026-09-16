package uss.code.course.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uss.code.course.domain.CourseSchedule
import uss.code.course.domain.CourseTerm

interface CourseScheduleRepository : JpaRepository<CourseSchedule, Long> {
    @Query("""
        SELECT COUNT(s)
        FROM CourseSchedule s
        WHERE s.course.academicYear = :academicYear
          AND s.course.term = :term
    """)
    fun countBySemester(
        @Param("academicYear") academicYear: Int,
        @Param("term") term: CourseTerm,
    ): Long

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        DELETE FROM CourseSchedule s
        WHERE s.course IN (
            SELECT c
            FROM Course c
            WHERE c.academicYear = :academicYear
              AND c.term = :term
        )
    """)
    fun deleteBySemester(
        @Param("academicYear") academicYear: Int,
        @Param("term") term: CourseTerm,
    )
}
