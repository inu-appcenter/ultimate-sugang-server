package uss.code.auth.dto.request

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LoginRequestTest {
    private fun isValid(
        studentId: String,
        password: String,
    ): Boolean {
        val request = LoginRequest(
            studentId = studentId,
            password = password,
        )
        return validator.validate(request).isEmpty()
    }

    @Nested
    inner class 학번_검증_테스트 {
        @Test
        fun 학번이_있으면_통과한다() {
            assertThat(isValid(VALID_STUDENT_ID, VALID_PASSWORD)).isTrue()
        }

        @Test
        fun 비어있으면_실패한다() {
            assertThat(isValid("", VALID_PASSWORD)).isFalse()
        }

        @Test
        fun 공백만_있으면_실패한다() {
            assertThat(isValid("   ", VALID_PASSWORD)).isFalse()
        }

        @Test
        fun 형식은_검사하지_않는다() {
            //given
            // 로그인은 조회일 뿐이라 학번 형식을 막지 않는다. 형식이 틀리면 회원을 찾지 못해 실패한다

            //when & then
            assertThat(isValid("stu dent@inu.ac.kr", VALID_PASSWORD)).isTrue()
        }
    }

    @Nested
    inner class 비밀번호_검증_테스트 {
        @Test
        fun 여덟_자_미만이면_실패한다() {
            assertThat(isValid(VALID_STUDENT_ID, "pass12")).isFalse()
        }

        @Test
        fun 비어있으면_실패한다() {
            assertThat(isValid(VALID_STUDENT_ID, "")).isFalse()
        }
    }

    companion object {
        private const val VALID_STUDENT_ID = "202012345"
        private const val VALID_PASSWORD = "password123"

        private val validator: Validator = Validation.buildDefaultValidatorFactory().use { it.validator }
    }
}
