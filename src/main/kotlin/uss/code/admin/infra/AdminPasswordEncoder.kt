package uss.code.admin.infra

import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Component

@Component
class AdminPasswordEncoder {
    fun encode(rawPassword: String): String {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(LOG_ROUNDS))
    }

    fun matches(
        rawPassword: String,
        encodedPassword: String,
    ): Boolean {
        return try {
            BCrypt.checkpw(rawPassword, encodedPassword)
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    companion object {
        private const val LOG_ROUNDS = 10
    }
}
