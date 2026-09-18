package uss.code.auth.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*

data class SignUpRequest(
    @field:Schema(
        description = "이메일",
        example = "student@inu.ac.kr"
    )
    @field:NotBlank(message = "이메일이 비어있습니다.")
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    val email: String?,

    @field:Schema(
        description = "비밀번호",
        example = "password1234"
    )
    @field:NotBlank(message = "비밀번호가 비어있습니다.")
    @field:Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
    val password: String?,

    @field:Schema(
        description = "학번",
        example = "202012345"
    )
    @field:NotBlank(message = "학번이 비어있습니다.")
    @field:Pattern(regexp = "^[A-Za-z0-9]{1,20}$", message = "학번은 20자 이하의 영문자 또는 숫자여야 합니다.")
    val studentId: String?,

    @field:Schema(
        description = "이름",
        example = "홍길동"
    )
    @field:NotBlank(message = "이름이 비어있습니다.")
    val name: String?,

    @field:Schema(
        description = "단과대학",
        example = "INFORMATION_TECHNOLOGY"
    )
    @field:NotBlank(message = "단과대학이 비어있습니다.")
    val college: String?,

    @field:Schema(
        description = "학과(부)",
        example = "COMPUTER_ENGINEERING"
    )
    @field:NotBlank(message = "학과가 비어있습니다.")
    val department: String?,

    @field:Schema(
        description = "학년",
        example = "JUNIOR"
    )
    @field:NotBlank(message = "학년이 비어있습니다.")
    val grade: String?,

    @field:Schema(
        description = "학적 상태",
        example = "ENROLLED"
    )
    @field:NotBlank(message = "학적 상태가 비어있습니다.")
    val academicStatus: String?,

    @field:Schema(
        description = "직전 학기 성적",
        example = "3.5"
    )
    @field:NotNull(message = "직전 학기 성적이 비어있습니다.")
    @field:DecimalMin(value = "0.0", message = "직전 학기 성적은 0.0 이상이어야 합니다.")
    @field:DecimalMax(value = "4.5", message = "직전 학기 성적은 4.5 이하여야 합니다.")
    val lastSemesterGpa: Double?,
)
