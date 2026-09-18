package uss.code.auth.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import uss.code.auth.dto.request.LoginRequest
import uss.code.auth.dto.request.SignUpRequest
import uss.code.auth.dto.response.AuthTokenResponse
import uss.code.auth.dto.response.EmailAvailabilityResponse
import uss.code.auth.dto.response.StudentIdAvailabilityResponse
import uss.code.auth.service.AuthService
import uss.code.global.annotation.ParamValidation

@Validated
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) : AuthControllerDocs {

    @PostMapping("/sign-up")
    override fun signUp(
        @Valid @RequestBody request: SignUpRequest,
    ): ResponseEntity<AuthTokenResponse> {
        val response = authService.signUp(request)
        return ResponseEntity.status(CREATED).body(response)
    }

    @PostMapping("/login")
    override fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<AuthTokenResponse> {
        val response = authService.login(request)
        return ResponseEntity.status(OK).body(response)
    }

    @PostMapping("/re-issue")
    override fun reissue(
        @RequestHeader(value = ACCESS_TOKEN_HEADER, required = false) accessToken: String?,
    ): ResponseEntity<AuthTokenResponse> {
        val response = authService.reissue(accessToken)
        return ResponseEntity.status(OK).body(response)
    }

    @GetMapping("/email-availability")
    override fun checkEmailAvailability(
        @ParamValidation(maxLength = 255) @RequestParam("email") email: String,
    ): ResponseEntity<EmailAvailabilityResponse> {
        val response = authService.checkEmailAvailability(email)
        return ResponseEntity.status(OK).body(response)
    }

    @GetMapping("/student-id-availability")
    override fun checkStudentIdAvailability(
        @ParamValidation(maxLength = 20) @RequestParam("student-id") studentId: String,
    ): ResponseEntity<StudentIdAvailabilityResponse> {
        val response = authService.checkStudentIdAvailability(studentId)
        return ResponseEntity.status(OK).body(response)
    }

    companion object {
        private const val ACCESS_TOKEN_HEADER = "access-token"
    }
}
