package uss.code.cart.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uss.code.cart.domain.Cart
import uss.code.course.domain.CourseTerm
import java.util.Optional

interface CartRepository : JpaRepository<Cart, Long> {
    @Query("""
        SELECT c
        FROM Cart c
        JOIN FETCH c.course course
        LEFT JOIN FETCH course.schedules
        WHERE c.member.id = :memberId
        ORDER BY c.createdAt
    """)
    fun findByMemberId(@Param("memberId") memberId: Long): List<Cart>

    @Query("""
        SELECT c
        FROM Cart c
        WHERE c.member.id = :memberId
        AND c.course.id = :courseId
    """)
    fun findByMemberIdAndCourseId(
        @Param("memberId") memberId: Long,
        @Param("courseId") courseId: Long,
    ): Optional<Cart>

    @Query("""
        SELECT COUNT(c)
        FROM Cart c
        WHERE c.course.academicYear = :academicYear
          AND c.course.term = :term
    """)
    fun countBySemester(
        @Param("academicYear") academicYear: Int,
        @Param("term") term: CourseTerm,
    ): Long

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        DELETE FROM Cart c
        WHERE c.course IN (
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
