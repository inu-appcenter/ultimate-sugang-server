package uss.code.registration.controller

import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uss.code.auth.annotation.Auth
import uss.code.registration.dto.response.RegistrationCoursesResponse
import uss.code.registration.dto.response.RegistrationResponse
import uss.code.registration.service.RegistrationService

@RestController
@RequestMapping("/api/v1/registration")
class RegistrationController(
    private val registrationService: RegistrationService,
) : RegistrationControllerDocs {

    @GetMapping
    override fun getRegistrationCourse(
        @Auth memberId: Long,
    ): ResponseEntity<RegistrationCoursesResponse> {
        val response = registrationService.getRegistrationCourse(memberId)
        return ResponseEntity.status(OK).body(response)
    }

    @PostMapping("/{course-id}")
    override fun registerCourse(
        @Auth memberId: Long,
        @PathVariable("course-id") courseId: Long,
    ): ResponseEntity<RegistrationResponse> {
        val response = registrationService.registerCourse(memberId, courseId)
        return ResponseEntity.status(OK).body(response)
    }

    @DeleteMapping("/{course-id}")
    override fun deleteRegisteredCourse(
        @Auth memberId: Long,
        @PathVariable("course-id") courseId: Long,
    ): ResponseEntity<Void> {
        registrationService.deleteRegisteredCourse(memberId, courseId)
        return ResponseEntity.status(OK).build()
    }
}
