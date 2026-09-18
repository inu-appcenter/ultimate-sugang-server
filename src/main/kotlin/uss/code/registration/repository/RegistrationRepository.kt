package uss.code.registration.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uss.code.registration.domain.Registration

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
    ): Registration?
}
