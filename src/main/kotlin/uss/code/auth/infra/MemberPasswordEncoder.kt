package uss.code.auth.infra

import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Component

@Component
class MemberPasswordEncoder {
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
