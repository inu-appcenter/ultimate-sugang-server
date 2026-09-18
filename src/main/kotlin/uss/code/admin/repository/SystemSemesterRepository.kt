package uss.code.admin.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uss.code.admin.domain.SystemSemester

interface SystemSemesterRepository : JpaRepository<SystemSemester, Long> {
    @Query("""
        SELECT s
        FROM SystemSemester s
        ORDER BY s.id
    """)
    fun findAllOrdered(): List<SystemSemester>
}
