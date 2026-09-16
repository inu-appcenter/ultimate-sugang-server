package uss.code.admin.repository

import org.springframework.data.jpa.repository.JpaRepository
import uss.code.admin.domain.Admin
import java.util.Optional

interface AdminRepository : JpaRepository<Admin, Long> {
    fun findByLoginId(loginId: String): Optional<Admin>
}
