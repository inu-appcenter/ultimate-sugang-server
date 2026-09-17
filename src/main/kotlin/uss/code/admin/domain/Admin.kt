package uss.code.admin.domain

import jakarta.persistence.*
import uss.code.admin.domain.AdminRole.ADMIN
import java.time.LocalDateTime

@Entity
@Table(
    name = "admins",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["login_id"]),
    ],
)
class Admin private constructor(
    loginId: String,
    password: String,
    name: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L
        protected set

    @Column(nullable = false, name = "login_id")
    var loginId: String = loginId
        protected set

    @Column(nullable = false, name = "password")
    var password: String = password
        protected set

    @Column(nullable = false, name = "name")
    var name: String = name
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "role")
    var role: AdminRole = ADMIN
        protected set

    @Column(nullable = false, name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now()
        protected set

    fun isAdmin(): Boolean {
        return role == ADMIN
    }

    companion object {
        @JvmStatic
        fun create(
            loginId: String,
            encodedPassword: String,
            name: String,
        ): Admin {
            return Admin(
                loginId = loginId,
                password = encodedPassword,
                name = name,
            )
        }
    }
}
