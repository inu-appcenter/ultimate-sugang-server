package uss.code.cart.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uss.code.cart.domain.Cart

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
    ): Cart?
}
