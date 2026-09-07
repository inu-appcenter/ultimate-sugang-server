package uss.code.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import static lombok.AccessLevel.PRIVATE;

@Builder(access = PRIVATE)
public record StudentIdAvailabilityResponse(
        @Schema(
                description = "학번 사용 가능 여부",
                example = "true"
        )
        boolean available
) {
    public static StudentIdAvailabilityResponse of(final boolean available) {
        return StudentIdAvailabilityResponse.builder()
                .available(available)
                .build();
    }
}
