package uss.code.course.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uss.code.auth.annotation.Auth;
import uss.code.course.dto.response.CourseCategoriesResponse;
import uss.code.course.dto.response.CourseTermsResponse;
import uss.code.course.dto.response.CoursesResponse;
import uss.code.course.dto.response.DepartmentsResponse;
import uss.code.course.dto.response.InterdisciplinaryMajorsResponse;
import uss.code.global.annotation.ParamValidation;
import uss.code.global.exception.dto.response.ErrorResponse;

@Tag(name = "Course API", description = "과목 조회 관련 API")
public interface CourseControllerDocs {

    @Operation(summary = "전공 과목 조회", description = "사용자의 전공에 해당하는 과목 목록을 조회합니다.<br>" +
            "🔐 <strong>Jwt 필요</strong><br>")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 전공 과목 조회 성공"),
            @ApiResponse(responseCode = "404", description = "🚨 사용자 조회 실패",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "사용자 조회 실패",
                                            value = "{\"code\" : \"MEM-001\", \"message\" : \"사용자를 찾을 수 없어요.\"}"
                                    )
                            },
                            schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/major")
    ResponseEntity<CoursesResponse> getMajorCourses(@Auth final long memberId);

    @Operation(summary = "교양 과목 조회", description = "이수구분 코드로 교양 과목 목록을 조회합니다.<br>" +
            "이수구분 코드는 기초교양 11, 핵심교양 21, 심화교양 23, 교직 50, 군사학 70, 일반선택 80 입니다.<br>" +
            "이수영역 코드를 함께 넘기면 그 영역만, 넘기지 않으면 이수구분 전체를 조회합니다.<br>" +
            "🔐 <strong>Jwt 필요</strong><br>")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 교양 과목 조회 성공"),
            @ApiResponse(responseCode = "400", description = "🚨 유효하지 않은 교양 이수구분",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "유효하지 않은 교양 이수구분",
                                            value = "{\"code\" : \"CRS-007\", \"message\" : \"유효하지 않은 교양 이수구분이에요.\"}"
                                    )
                            },
                            schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "🚨 유효하지 않은 교양 영역",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "유효하지 않은 교양 영역",
                                            value = "{\"code\" : \"CRS-001\", \"message\" : \"유효하지 않은 교양 영역이에요.\"}"
                                    )
                            },
                            schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/general-education")
    ResponseEntity<CoursesResponse> getGeneralEducationCourses(
            @ParamValidation(maxLength = 3)
            @RequestParam("classification-code") final String classificationCode,
            @ParamValidation(maxLength = 3)
            @RequestParam(value = "area-code", required = false) final String areaCode
    );

    @Operation(summary = "타학과 과목 조회", description = "특정 학과의 과목 목록을 조회합니다.<br>" +
            "학과는 회원이 소속될 수 있는 학과, 학부 이름으로 넘깁니다. 교양, 교직, 일선, 군사학, 연계전공은 학과가 아니므로 넘길 수 없습니다.<br>" +
            "학부를 넘기면 그 학부의 하위 전공 과목까지 함께 조회됩니다.<br>" +
            "🔐 <strong>Jwt 필요</strong><br>")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 타학과 과목 조회 성공"),
            @ApiResponse(responseCode = "400", description = "🚨 유효하지 않은 학과",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "존재하지 않는 학과",
                                            value = "{\"code\" : \"GLB-002\", \"message\" : \"유효하지 않은 열거타입이에요.\"}"
                                    )
                            },
                            schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/other-department")
    ResponseEntity<CoursesResponse> getOtherDepartmentCourses(
            @ParamValidation(maxLength = 40)
            @RequestParam("department") final String department
    );

    @Operation(summary = "연계전공 과목 조회", description = "특정 연계전공의 과목 목록을 조회합니다.<br>" +
            "🔐 <strong>Jwt 필요</strong><br>")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 연계전공 과목 조회 성공"),
            @ApiResponse(responseCode = "400", description = "🚨 유효하지 않은 연계전공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "유효하지 않은 연계전공",
                                            value = "{\"code\" : \"CRS-002\", \"message\" : \"유효하지 않은 연계전공과목이에요.\"}"
                                    )
                            },
                            schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/interdisciplinary-major")
    ResponseEntity<CoursesResponse> getInterdisciplinaryMajorCourses(
            @ParamValidation(maxLength = 40)
            @RequestParam("department") final String department
    );

    @Operation(summary = "과목 검색", description = "키워드로 과목을 검색합니다.<br>" +
            "🔐 <strong>Jwt 필요</strong><br>")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 과목 검색 성공")
    })
    @GetMapping("/search")
    ResponseEntity<CoursesResponse> searchCourses(
            @ParamValidation(maxLength = 70)
            @RequestParam("keyword") final String keyword
    );

    @Operation(summary = "HUSS 과목 조회", description = "HUSS 교과목 목록을 조회합니다.<br>" +
            "🔐 <strong>Jwt 필요</strong><br>")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ HUSS 과목 조회 성공")
    })
    @GetMapping("/huss")
    ResponseEntity<CoursesResponse> getHussCourses();

    @Operation(summary = "과목 카테고리 조회", description = "적재된 과목의 이수구분과 그에 속한 이수영역 목록을 조회합니다.<br>" +
            "🔐 <strong>Jwt 필요</strong><br>")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 과목 카테고리 조회 성공")
    })
    @GetMapping("/categories")
    ResponseEntity<CourseCategoriesResponse> getCategories();

    @Operation(summary = "년도, 학기 조회", description = "적재된 과목의 년도와 학기 목록을 조회합니다.<br>" +
            "🔐 <strong>Jwt 필요</strong><br>")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 년도, 학기 조회 성공")
    })
    @GetMapping("/terms")
    ResponseEntity<CourseTermsResponse> getTerms();

    @Operation(summary = "연계전공 조회", description = "과목이 적재된 연계전공 목록을 조회합니다.<br>" +
            "🔐 <strong>Jwt 필요</strong><br>")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 연계전공 조회 성공")
    })
    @GetMapping("/interdisciplinary-majors")
    ResponseEntity<InterdisciplinaryMajorsResponse> getInterdisciplinaryMajors();

    @Operation(summary = "학과 조회", description = "과목이 적재된 학과 목록을 조회합니다.<br>" +
            "응답의 code를 타학과 과목 조회의 department 파라미터로 넘깁니다.<br>" +
            "🔐 <strong>Jwt 필요</strong><br>")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 학과 조회 성공")
    })
    @GetMapping("/departments")
    ResponseEntity<DepartmentsResponse> getDepartments();
}
