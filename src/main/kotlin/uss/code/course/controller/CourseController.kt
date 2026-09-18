package uss.code.course.controller

import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uss.code.auth.annotation.Auth
import uss.code.course.dto.response.*
import uss.code.course.service.CourseService
import uss.code.global.annotation.ParamValidation

@Validated
@RestController
@RequestMapping("/api/v1/courses")
class CourseController(
    private val courseService: CourseService,
) : CourseControllerDocs {

    @GetMapping("/major")
    override fun getMajorCourses(
        @Auth memberId: Long,
    ): ResponseEntity<CoursesResponse> {
        val response = courseService.getMajorCourses(memberId)
        return ResponseEntity.status(OK).body(response)
    }

    @GetMapping("/general-education")
    override fun getGeneralEducationCourses(
        @ParamValidation(maxLength = 3) @RequestParam("classification-code") classificationCode: String,
        @ParamValidation(maxLength = 3) @RequestParam(value = "area-code", required = false) areaCode: String?,
    ): ResponseEntity<CoursesResponse> {
        val response = courseService.getGeneralEducationCourses(classificationCode, areaCode)
        return ResponseEntity.status(OK).body(response)
    }

    @GetMapping("/other-department")
    override fun getOtherDepartmentCourses(
        @ParamValidation(maxLength = 40) @RequestParam("department") department: String,
    ): ResponseEntity<CoursesResponse> {
        val response = courseService.getOtherDepartmentCourses(department)
        return ResponseEntity.status(OK).body(response)
    }

    @GetMapping("/interdisciplinary-major")
    override fun getInterdisciplinaryMajorCourses(
        @ParamValidation(maxLength = 40) @RequestParam("department") department: String,
    ): ResponseEntity<CoursesResponse> {
        val response = courseService.getInterdisciplinaryMajorCourses(department)
        return ResponseEntity.status(OK).body(response)
    }

    @GetMapping("/search")
    override fun searchCourses(
        @ParamValidation(maxLength = 70) @RequestParam("keyword") keyword: String,
    ): ResponseEntity<CoursesResponse> {
        val response = courseService.searchCourses(keyword)
        return ResponseEntity.status(OK).body(response)
    }

    @GetMapping("/huss")
    override fun getHussCourses(): ResponseEntity<CoursesResponse> {
        val response = courseService.getHussCourses()
        return ResponseEntity.status(OK).body(response)
    }

    @GetMapping("/categories")
    override fun getCategories(): ResponseEntity<CourseCategoriesResponse> {
        val response = courseService.getCategories()
        return ResponseEntity.status(OK).body(response)
    }

    @GetMapping("/terms")
    override fun getTerms(): ResponseEntity<CourseTermsResponse> {
        val response = courseService.getTerms()
        return ResponseEntity.status(OK).body(response)
    }

    @GetMapping("/interdisciplinary-majors")
    override fun getInterdisciplinaryMajors(): ResponseEntity<InterdisciplinaryMajorsResponse> {
        val response = courseService.getInterdisciplinaryMajors()
        return ResponseEntity.status(OK).body(response)
    }

    @GetMapping("/departments")
    override fun getDepartments(): ResponseEntity<DepartmentsResponse> {
        val response = courseService.getDepartments()
        return ResponseEntity.status(OK).body(response)
    }
}
