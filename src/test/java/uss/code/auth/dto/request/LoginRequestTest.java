package uss.code.auth.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestTest {

    private static final String VALID_STUDENT_ID = "202012345";
    private static final String VALID_PASSWORD = "password123";

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private boolean isValid(
            final String studentId,
            final String password
    ) {
        return validator.validate(new LoginRequest(studentId, password)).isEmpty();
    }

    @Nested
    class 학번_검증_테스트 {

        @Test
        void 학번이_있으면_통과한다() {
            assertThat(isValid(VALID_STUDENT_ID, VALID_PASSWORD)).isTrue();
        }

        @Test
        void 비어있으면_실패한다() {
            assertThat(isValid("", VALID_PASSWORD)).isFalse();
        }

        @Test
        void 공백만_있으면_실패한다() {
            assertThat(isValid("   ", VALID_PASSWORD)).isFalse();
        }

        @Test
        void 형식은_검사하지_않는다() {
            //given
            // 로그인은 조회일 뿐이라 학번 형식을 막지 않는다. 형식이 틀리면 회원을 찾지 못해 실패한다

            //when & then
            assertThat(isValid("stu dent@inu.ac.kr", VALID_PASSWORD)).isTrue();
        }
    }

    @Nested
    class 비밀번호_검증_테스트 {

        @Test
        void 여덟_자_미만이면_실패한다() {
            assertThat(isValid(VALID_STUDENT_ID, "pass12")).isFalse();
        }

        @Test
        void 비어있으면_실패한다() {
            assertThat(isValid(VALID_STUDENT_ID, "")).isFalse();
        }
    }
}
