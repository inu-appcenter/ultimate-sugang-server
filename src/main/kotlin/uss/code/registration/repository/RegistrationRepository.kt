package uss.code.registration.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uss.code.course.domain.CourseTerm
import uss.code.registration.domain.Registration
import java.util.Optional

interface RegistrationRepository : JpaRepository<Registration, Long> {
    @Query("""
        SELECT r
        FROM Registration r
        JOIN FETCH r.course
        WHERE r.member.id = :memberId
    """)
    fun findByMemberId(@Param("memberId") memberId: Long): List<Registration>

    @Query("""
        SELECT r
        FROM Registration r
        WHERE r.member.id = :memberId AND r.course.id = :courseId
    """)
    fun findByMemberIdAndCourseId(
        @Param("memberId") memberId: Long,
        @Param("courseId") courseId: Long,
    ): Optional<Registration>

    @Query("""
        SELECT COUNT(r)
        FROM Registration r
        WHERE r.course.academicYear = :academicYear
          AND r.course.term = :term
    """)
    fun countBySemester(
        @Param("academicYear") academicYear: Int,
        @Param("term") term: CourseTerm,
    ): Long

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        DELETE FROM Registration r
        WHERE r.course IN (
            SELECT course
            FROM Course course
            WHERE course.academicYear = :academicYear
              AND course.term = :term
        )
    """)
    fun deleteBySemester(
        @Param("academicYear") academicYear: Int,
        @Param("term") term: CourseTerm,
    )
}
