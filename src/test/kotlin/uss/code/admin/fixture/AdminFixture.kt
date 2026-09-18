package uss.code.admin.fixture

import org.mindrot.jbcrypt.BCrypt
import uss.code.admin.domain.Admin

object AdminFixture {
    const val DEFAULT_LOGIN_ID = "test-admin"
    const val DEFAULT_PASSWORD = "test-admin-password"
    const val DEFAULT_NAME = "김학사"

    private const val LOG_ROUNDS = 4

    fun createAdmin(): Admin {
        return createAdmin(DEFAULT_LOGIN_ID, DEFAULT_PASSWORD, DEFAULT_NAME)
    }

    fun createAdmin(
        loginId: String,
        rawPassword: String,
        name: String,
    ): Admin {
        return Admin.create(
            loginId = loginId,
            encodedPassword = encode(rawPassword),
            name = name,
        )
    }

    private fun encode(rawPassword: String): String {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(LOG_ROUNDS))
    }
}
