package uss.code.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Schema(
                description = "학번",
                example = "202012345"
        )
        @NotBlank(message = "학번이 비어있습니다.")
        String studentId,

        @Schema(
                description = "비밀번호",
                example = "password1234"
        )
        @NotBlank(message = "비밀번호가 비어있습니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
        String password
) {}
