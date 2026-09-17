package uss.code.admin.repository

import org.springframework.data.jpa.repository.JpaRepository
import uss.code.admin.domain.Admin

interface AdminRepository : JpaRepository<Admin, Long> {
    fun findByLoginId(loginId: String): Admin?
}
