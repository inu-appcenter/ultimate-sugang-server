package uss.code.member.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uss.code.auth.annotation.Auth
import uss.code.member.dto.request.DepartmentUpdateRequest
import uss.code.member.dto.response.MemberProfileResponse
import uss.code.member.service.MemberService

@RestController
@RequestMapping("/api/v1/members")
class MemberController(
    private val memberService: MemberService,
) : MemberControllerDocs {

    @GetMapping("/profile")
    override fun getProfile(
        @Auth memberId: Long,
    ): ResponseEntity<MemberProfileResponse> {
        val response = memberService.getProfile(memberId)
        return ResponseEntity.status(OK).body(response)
    }

    @PatchMapping("/department")
    override fun updateDepartment(
        @Auth memberId: Long,
        @Valid @RequestBody request: DepartmentUpdateRequest,
    ): ResponseEntity<Void> {
        memberService.updateDepartment(memberId, request)
        return ResponseEntity.status(OK).build()
    }
}
