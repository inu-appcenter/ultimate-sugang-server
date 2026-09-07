package uss.code.course.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uss.code.auth.annotation.Auth;
import uss.code.course.dto.response.CourseCategoriesResponse;
import uss.code.course.dto.response.CourseTermsResponse;
import uss.code.course.dto.response.CoursesResponse;
import uss.code.course.dto.response.DepartmentsResponse;
import uss.code.course.dto.response.InterdisciplinaryMajorsResponse;
import uss.code.course.service.CourseService;
import uss.code.global.annotation.ParamValidation;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/courses")
public class CourseController implements CourseControllerDocs {

    private final CourseService courseService;

    @GetMapping("/major")
    public ResponseEntity<CoursesResponse> getMajorCourses(@Auth final long memberId){
        return ResponseEntity.ok(courseService.getMajorCourses(memberId));
    }

    @GetMapping("/general-education")
    public ResponseEntity<CoursesResponse> getGeneralEducationCourses(
            @ParamValidation(maxLength = 3)
            @RequestParam("classification-code") final String classificationCode,
            @ParamValidation(maxLength = 3)
            @RequestParam(value = "area-code", required = false) final String areaCode
    ){
        return ResponseEntity.ok(courseService.getGeneralEducationCourses(classificationCode, areaCode));
    }

    @GetMapping("/other-department")
    public ResponseEntity<CoursesResponse> getOtherDepartmentCourses(
            @ParamValidation(maxLength = 40)
            @RequestParam("department") final String department
    ){
        return ResponseEntity.ok(courseService.getOtherDepartmentCourses(department));
    }

    @GetMapping("/interdisciplinary-major")
    public ResponseEntity<CoursesResponse> getInterdisciplinaryMajorCourses(
            @ParamValidation(maxLength = 40)
            @RequestParam("department") final String department
    ){
        return ResponseEntity.ok(courseService.getInterdisciplinaryMajorCourses(department));
    }

    @GetMapping("/search")
    public ResponseEntity<CoursesResponse> searchCourses(
            @ParamValidation(maxLength = 70)
            @RequestParam("keyword") final String keyword
    ){
        return ResponseEntity.ok(courseService.searchCourses(keyword));
    }

    @GetMapping("/huss")
    public ResponseEntity<CoursesResponse> getHussCourses(){
        return ResponseEntity.ok(courseService.getHussCourses());
    }

    @GetMapping("/categories")
    public ResponseEntity<CourseCategoriesResponse> getCategories(){
        return ResponseEntity.ok(courseService.getCategories());
    }

    @GetMapping("/terms")
    public ResponseEntity<CourseTermsResponse> getTerms(){
        return ResponseEntity.ok(courseService.getTerms());
    }

    @GetMapping("/interdisciplinary-majors")
    public ResponseEntity<InterdisciplinaryMajorsResponse> getInterdisciplinaryMajors(){
        return ResponseEntity.ok(courseService.getInterdisciplinaryMajors());
    }

    @GetMapping("/departments")
    public ResponseEntity<DepartmentsResponse> getDepartments(){
        return ResponseEntity.ok(courseService.getDepartments());
    }
}
