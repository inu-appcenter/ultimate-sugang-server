package uss.code.member.repository

import org.springframework.data.jpa.repository.JpaRepository
import uss.code.member.domain.Member

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByEmail(email: String): Member?

    fun existsByEmail(email: String): Boolean

    fun findByStudentId(studentId: String): Member?

    fun existsByStudentId(studentId: String): Boolean
}
