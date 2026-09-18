package uss.code.admin.controller

import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uss.code.admin.dto.response.CourseSummaryResponse
import uss.code.admin.service.AdminCourseService

@RestController
@RequestMapping("/api/v1/admin/courses")
class AdminCourseController(
    private val adminCourseService: AdminCourseService,
) : AdminCourseControllerDocs {

    @GetMapping("/summary")
    override fun getSummary(): ResponseEntity<CourseSummaryResponse> {
        val response = adminCourseService.getSummary()
        return ResponseEntity.status(OK).body(response)
    }
}
