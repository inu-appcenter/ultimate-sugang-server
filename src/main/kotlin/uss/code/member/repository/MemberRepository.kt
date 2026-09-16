package uss.code.member.repository

import org.springframework.data.jpa.repository.JpaRepository
import uss.code.member.domain.Member
import java.util.Optional

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByEmail(email: String): Optional<Member>

    fun existsByEmail(email: String): Boolean

    fun findByStudentId(studentId: String): Optional<Member>

    fun existsByStudentId(studentId: String): Boolean
}
