package uss.code.admin.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import uss.code.admin.dto.response.CourseSummaryResponse

@Tag(name = "Admin Course API", description = "백오피스 강의 적재 현황 API")
interface AdminCourseControllerDocs {
    @Operation(
        summary = "강의 적재 현황 조회",
        description = "적재된 학기, 강의 수, 시간표 수를 조회합니다.<br>" +
            "강의 수는 폐강을 포함합니다.<br>" +
            "🔐 <strong>Jwt 필요</strong> (관리자)<br>"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "✅ 적재 현황 조회 성공")
    )
    @GetMapping("/summary")
    fun getSummary(): ResponseEntity<CourseSummaryResponse>
}
