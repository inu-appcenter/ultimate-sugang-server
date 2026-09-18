package uss.code.admin.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uss.code.admin.dto.request.SystemSemesterRequest
import uss.code.admin.dto.response.SystemSemesterResponse
import uss.code.admin.service.SystemSemesterService

@RestController
@RequestMapping("/api/v1/admin/semesters")
class AdminSemesterController(
    private val systemSemesterService: SystemSemesterService,
) : AdminSemesterControllerDocs {

    @GetMapping
    override fun getSystemSemester(): ResponseEntity<SystemSemesterResponse> {
        val response = systemSemesterService.getSystemSemester()
        return ResponseEntity.status(OK).body(response)
    }

    @PutMapping
    override fun changeSystemSemester(
        @Valid @RequestBody request: SystemSemesterRequest,
    ): ResponseEntity<SystemSemesterResponse> {
        val response = systemSemesterService.changeSystemSemester(request)
        return ResponseEntity.status(OK).body(response)
    }
}
