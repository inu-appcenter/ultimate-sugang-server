package uss.code.admin.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uss.code.admin.dto.request.AdminLoginRequest
import uss.code.admin.dto.response.AdminTokenResponse
import uss.code.admin.service.AdminAuthService

@RestController
@RequestMapping("/api/v1/admin/auth")
class AdminAuthController(
    private val adminAuthService: AdminAuthService,
) : AdminAuthControllerDocs {

    @PostMapping("/login")
    override fun login(
        @Valid @RequestBody request: AdminLoginRequest,
    ): ResponseEntity<AdminTokenResponse> {
        val response = adminAuthService.login(request)
        return ResponseEntity.status(OK).body(response)
    }

    @PostMapping("/refresh")
    override fun refresh(
        @RequestHeader(value = ACCESS_TOKEN_HEADER, required = false) accessToken: String?,
    ): ResponseEntity<AdminTokenResponse> {
        val response = adminAuthService.reissue(accessToken)
        return ResponseEntity.status(OK).body(response)
    }

    companion object {
        private const val ACCESS_TOKEN_HEADER = "access-token"
    }
}
