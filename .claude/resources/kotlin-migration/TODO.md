# Kotlin 마이그레이션 TODO

- 이슈: #126 `refactor: 서버 코드 Kotlin 전환`
- 브랜치: `refactor/126-kotlin-migration`
- 컨벤션: `.claude/rules/code-convention/kotlin/` (전환 기간 규칙은 `interop.md`)
- 학습 노트: 루트 `NOTE.md` (gitignore). 사용자가 Kotlin 문법을 물으면 답한 내용을 여기에 주제별로 정리한다
- 동기화 관련 코드는 이 마이그레이션 대상이 아니다. 제거는 #127에서 한다
- 새 세션은 이 파일을 먼저 읽고 "현재 상태"부터 이어간다. 단위를 끝낼 때마다 이 파일을 갱신한다.

## 현재 상태

- 0단계(빌드 설정) 완료
- U1 완료 (2026-09-15, 전체 테스트 363개 통과)
- U2 완료 (2026-09-15, 컴파일 통과. 테스트는 레이어가 끝나는 U7 뒤에 돌린다)
- U3 완료 (2026-09-16, 컴파일 통과)
- U4 완료 (2026-09-16, 컴파일 통과. `dto/common` → `dto/internal` 이름 변경과 Java 호출부 8개 수정 포함)
- U5 완료 (2026-09-16, 컴파일 통과)
- U6, U7 완료 (2026-09-16). **레이어 1이 끝나 전체 테스트를 돌렸고 363개 전부 통과했다**
- U8 완료 (2026-09-16). **레이어 2가 끝나 전체 테스트를 돌렸고 363개 전부 통과했다**
- U9 완료 (2026-09-16, 컴파일 통과). 레포지토리 3개를 nullable로 바꾸고 테스트 단언 4줄을 고쳤다
- U10 완료 (2026-09-17). **레이어 3이 끝나 전체 테스트를 돌렸고 363개 전부 통과했다**
  - 첫 실행은 `MemberServiceTest` 7개가 `NoSuchMethodError: getProfile(java.lang.Long)`로 실패했다. 증분 컴파일이 Java 시절 시그니처로 컴파일된 테스트 클래스를 다시 만들지 않았다. `./gradlew clean` 뒤 다시 돌려 통과했다
- U11~U13 완료 (2026-09-17). **레이어 4가 끝나 `clean` 뒤 전체 테스트를 돌렸고 363개 전부 통과했다**
  - 사용자와 합의한 컨벤션(함수 본문 블록 통일, 응답 형식, 경로 변수 케밥 케이스)을 컨트롤러와 기존 Kotlin 코드 전체에 반영했다. 결정 기록 참고
- U14 완료 (2026-09-17, 컴파일 통과)
- U15 완료 (2026-09-17, 컴파일 통과)
- U16 완료 (2026-09-17, 컴파일 통과). `src/main/java/uss/code/global`이 모두 옮겨져 비었다
- U17 완료 (2026-09-18, `clean` 뒤 컴파일 통과). `src/main/java/uss/code/auth`가 모두 옮겨져 비었다
- U18 완료 (2026-09-18). **레이어 5가 끝나 `clean` 뒤 전체 테스트를 돌렸고 363개 중 실패 0이다** (스킵 6개는 Docker가 필요한 `CourseServiceSearchTest`)
- **단위 U1~U18이 모두 끝났다.** `src/main/java`에 남은 Java 39개는 모두 동기화 코드(#127 대상)다: `admin`의 controller·domain·dto·event·infra·repository·service, `course/domain`의 `CourseFieldChange`, `CourseSnapshot`
- **다음: 6단계 마무리.** 조건이 "Java 코드가 남아 있지 않을 때"라 #127(동기화 코드 제거)이 먼저다. 순서는 사용자와 정한다
- 커밋: U1~U7(c4220b8), U8(019bd9a), U9~U10(9392706), U11~U13(6928c5a), 컨벤션 정리(38157d8)까지 커밋했다. 이 파일은 c4220b8 뒤로 커밋하지 않았다
  - 컨벤션 정리 커밋은 `=` 본문을 블록으로 바꾼 것과 의존성 그룹 순서 변경이다. 두 커밋 모두 스냅샷을 따로 꺼내 전체 테스트 363개 통과를 확인했다
  - `.claude/skills/implement/SKILL.md`의 한 줄 수정은 마이그레이션과 무관하다. 커밋에서 뺀다

## 역할

### 사용자

- `kotlin-reference/`의 참고 파일을 원본 `.java`와 두 탭으로 띄워 놓고, `src/main/kotlin/`의 같은 경로에 따라 친다
- 원본 `.java`는 지우지 않는다 (둘 다 있는 동안 IDE의 중복 선언 에러는 정상)
- 개념 정리는 노션에서 직접 한다
- 다 치면 "UN 다 했어"라고 알리고, 모르는 건 그때그때 묻는다
- 컨트롤러 단위(U11~U13)의 `{Controller}Docs`는 따라 치지 않는다. 클로드가 옮긴다 (2026-09-17 사용자 결정)

### 클로드

- **단위 준비**
  1. 원본 Java와 호출부를 분석한다. 누가 부르는지, null이 들어올 수 있는지, `Optional`이나 record 접근자를 쓰는지 본다
  2. `kotlin-reference/`(gitignore, 패키지 경로 그대로)에 참고 파일을 만든다. 컨벤션과 `interop.md`를 따른다
  3. 문자열 값과 enum 항목을 원본과 대조한다. 도구 입력의 `\uXXXX`는 실제 문자로 바뀌어 저장될 수 있으니 바이트로 확인한다
  4. Docs처럼 사용자가 따라 치지 않는 파일은 참고 파일을 거치지 않고 `src/main/kotlin`에 바로 넣고, 원본 `.java`를 지운 뒤 컴파일을 확인한다
- **사용자가 끝냈다고 하면**
  1. 작성본을 참고 파일과 비교한다 (공백 무시 diff, 문자열 값 diff)
  2. 표기 차이(어노테이션 누락, trailing comma, 공백)는 보고하면서 바로 참고 파일 기준으로 맞춘다. 작성본은 스크래치패드에 백업한다
     - 함수 순서나 구조처럼 의미가 있는 차이는 손대지 말고 따로 보고한다 (2026-09-16 사용자 결정)
  3. 원본 `.java`를 지운다
  4. 컴파일이 깨진 Java 호출부를 최소한으로 고친다
  5. 컴파일을 확인한다 (`./gradlew compileKotlin compileJava compileTestJava`, 2분 넘게 걸릴 수 있어 백그라운드로)
  6. 참고 파일을 지우고 이 파일을 갱신한다
- **테스트는 레이어가 끝날 때 한 번** 돌린다. 단위마다는 컴파일만 한다
  - 명령은 `.claude/CLAUDE.md`의 "macOS에서 테스트 전체 실행", 백그라운드로 실행
  - 먼저 `./gradlew clean`을 돌린다. Java에서 Kotlin으로 바뀐 시그니처(`Long` → `long`)를 증분 컴파일이 테스트 클래스에 반영하지 않은 적이 있다 (U10)
- 셸은 zsh다. `$var`는 단어로 나뉘지 않고 `$(...)`의 결과는 나뉜다. 경로 목록은 변수에 담지 말고 `for f in a/B c/D; do`처럼 직접 나열한다
  - 변수에 담은 목록으로 `mkdir -p .../$(dirname $f)`를 돌렸다가 프로젝트 루트에 빈 디렉터리가 생긴 적이 있다 (2026-09-17 `uss/`, 2026-09-18 `annotation/` 등. 모두 지웠다)
- 사용자 질문에는 묻는 것만 짧게 답하고, Kotlin 문법 질문이면 그 내용을 루트 `NOTE.md`의 해당 주제에 추가한다
- 커밋, 푸시는 사용자가 요청할 때만 한다

## 단위

### 1. 엔티티, enum, DTO (끝나면 전체 테스트)

- [x] U1: AcademicStatus, MemberCollege, MemberDepartment, MemberGrade, CourseStatus, CourseTerm, CourseDay, CourseGrade, CourseType, CourseCollege
- [x] U2: CourseArea, CourseClassification, CourseDepartment, CourseDepartmentKind, Member
  - `MemberFixture`의 `new Member()`를 `BeanUtils.instantiateClass(Member.class)`로 바꿨고, 이 규칙을 `interop.md`에 추가했다
- [x] U3: CourseSchedule, Course, Cart, Registration
  - 픽스처 `new Entity()` 수정: CourseFixture, CourseScheduleFixture, CartFixture, RegistrationFixture
  - `CartService`, `RegistrationService`의 `getCourse().getId().equals(courseId)`는 id가 `long`이 되면 깨진다. `==`로 고친다
  - `CourseSnapshot`, `CourseFieldChange`는 동기화 코드라 Java로 둔다. Kotlin `Course`는 record 컴포넌트를 프로퍼티로 읽고(`snapshot.titleKr`), Java 함수인 `CourseFieldChange.of`에는 이름 붙인 인자를 쓸 수 없다
  - `CourseSchedule.course`는 `create()` 뒤 `addCourse()`로 채워지므로 `lateinit var`로 선언했다
  - `Course.applyUpdate`는 `CourseSyncApplier`가 반환 리스트에 `add`하므로 `MutableList`를 반환한다
  - Java `Cart` 생성자는 public이었지만 직접 호출부가 없어 `private constructor`로 맞췄다
- [x] U4: course/dto/internal 5 (CachedCourseDto, CachedCoursesDto, CourseCapacityDto, CourseCategoryDto, CourseTermInfoDto) + CourseAreaResponse, CourseCategoriesResponse, CourseCategoryResponse, CourseResponse, CourseTermResponse
  - 10개 모두 `@JvmRecord data class`. Java 호출부가 record 접근자(`course.id()`)와 메서드 참조(`CourseCapacityDto::id`)를 쓴다
  - 팩토리는 `companion object` + `@JvmStatic`. `CachedCourseDto::from`, `CourseAreaResponse::from`, `CourseTermResponse::from`이 메서드 참조로 쓰인다
  - `CourseCapacityDto`, `CourseCategoryDto`, `CourseTermInfoDto`는 JPQL 생성자 표현식(`SELECT new ...`)으로 만들어진다. 파라미터 순서와 타입을 select 절과 맞춘다
  - `dto/common` → `dto/internal`, 이름에 `Dto` 접미사를 붙이는 규칙 변경을 이 단위부터 적용한다 (2026-09-16)
  - 완료할 때 Java 호출부의 패키지·클래스명을 함께 고친다 (8개 파일)
    - `CourseRepository`(JPQL 문자열의 `SELECT new ...` 포함), `CourseService`, `CourseCacheLoader`, `RedisCacheConfig`
    - `CourseSyncApplier`, `AdminCourseService`, `CourseSyncService`, 테스트 `CourseServiceTest`
    - 스크래치 복사본에서 일괄 치환으로 컴파일 통과를 확인했다
- [x] U5: CourseTermsResponse, CoursesResponse, DepartmentResponse, DepartmentsResponse, InterdisciplinaryMajorResponse, InterdisciplinaryMajorsResponse + DepartmentUpdateRequest, MemberProfileResponse + RegistrationCourseResponse, RegistrationCoursesResponse, RegistrationResponse
  - 11개 모두 `@JvmRecord data class` + 팩토리에 `@JvmStatic`. `DepartmentResponse::from`, `InterdisciplinaryMajorResponse::from`이 메서드 참조로 쓰이고, 테스트가 `response.courseResponses()` 같은 record 접근자를 쓴다
  - `DepartmentUpdateRequest`가 첫 Request DTO다. 프로퍼티는 nullable + `@field:Schema`, `@field:NotBlank`
    - 스크래치에서 확인: null·공백이면 위반 1개(메시지 그대로), 정상 값이면 0개, `{}` 역직렬화는 예외 없이 null
  - `department.name()`(enum 이름)은 Kotlin에서 `department.name`, `department.getName()`은 `department.displayName`이다
  - `RegistrationCourseResponse`의 `DateTimeFormatter`는 `const`가 안 되니 companion의 `private val`로 둔다
  - Java 호출부 수정은 없다
- [x] U6: LoginRequest, SignUpRequest, AuthTokenResponse, EmailAvailabilityResponse, StudentIdAvailabilityResponse + PageResponse, ErrorResponse
  - `LoginRequest`, `SignUpRequest`는 nullable + `@field:` 검증. 스크래치 확인: 전부 null이면 위반 2개/9개, 정상 값이면 0개, GPA 5.0이면 1개
  - `PageResponse<T>`는 제네릭 record다. `Page<?>` → `Page<*>`, `page.getNumber()` → `page.number`, 팩토리는 `fun <T> of(...)`
  - `ErrorResponse`는 `global/exception/dto/response`, `PageResponse`는 `global/dto/response`에 그대로 둔다
  - Java 호출부 수정은 없다
- [x] U7: Admin, AdminRole, SystemSemester + SemesterRef, AdminLoginRequest, SystemSemesterRequest, AdminTokenResponse, CourseSummaryResponse, SystemSemesterResponse
  - `admin/dto/common/SemesterRef`는 `admin/dto/internal/SemesterRefDto`로 옮긴다. 같은 패키지의 동기화용 DTO는 #127 대상이라 그대로 둔다
    - 완료할 때 Java 호출부 3개를 함께 고친다: `SyncPreflightResponse`, `CourseSyncService`, `AdminCourseService`
    - `internal`인데 `@Schema`가 붙어 있어 프로퍼티 사이 빈 줄을 유지했다 (규칙의 근거가 이 파일엔 맞지 않는다)
  - 픽스처 `new Entity()` 수정: AdminFixture, SystemSemesterFixture
  - `Admin`, `SystemSemester`는 U3와 같은 엔티티 형태(`private constructor` + `var` + `protected set`)
  - `CourseSummaryResponse`는 `@Schema` 설명대로 `semester`, `lastJob`, `runningJobId`를 nullable로 둔다. `LastJobInfo`는 동기화 코드라 Java로 남는다

### 2. 레포지토리 (끝나면 전체 테스트)

- [x] U8: AdminRepository, SystemSemesterRepository, CartRepository, CourseRepository, CourseScheduleRepository, MemberRepository, RegistrationRepository
  - 단건 조회는 `Optional<T>`을 그대로 둔다. Java 서비스가 `.orElseThrow(...)`로 받는다 (`interop.md`). 서비스가 Kotlin이 되는 U9, U10에서 `T?`로 바꾸고 호출부를 `?: throw`로 고친다
  - 쿼리는 raw string(`"""`)으로 두고 `trimIndent()`를 붙이지 않는다. 어노테이션 인자는 컴파일 타임 상수여야 한다
  - `@Modifying` 수정 쿼리 중 Java에서 `void`였던 것은 반환 타입을 생략하고, `int`였던 것은 `Int`로 둔다
  - `@Param("x") x: Long` 형태로 쓴다. Kotlin 파라미터는 기본이 불변이라 `final`이 없다
  - 쿼리는 실행 시점에 파싱되므로 스크래치 복사본에서 전체 테스트까지 돌려 확인했다

### 3. 서비스 (끝나면 전체 테스트)

- [x] U9: CourseService, MemberService, CartService, RegistrationService
  - 레포지토리 반환 타입 `Optional<T>` → `T?` 전환은 호출부가 모두 Kotlin이 되는 시점에 한다
    - U9에서 바꿀 것: `CartRepository.findByMemberIdAndCourseId`, `RegistrationRepository.findByMemberIdAndCourseId`, `CourseRepository.findByIdWithSchedules` (호출부가 U9 서비스뿐이다)
    - U10에서 바꿀 것: `MemberRepository.findByStudentId`(AuthService), `AdminRepository.findByLoginId`(AdminAuthService)
    - `MemberRepository.findByEmail`은 메인 호출부가 없고 `AuthServiceTest`만 `orElseThrow()`로 쓴다. U10에서 함께 정한다
    - 이 전환으로 테스트 4줄이 깨진다. `CartServiceTest`(787 `isEmpty()`→`isNull()`, 882·909 `isPresent()`→`isNotNull()`), `RegistrationServiceTest`(910 `isEmpty()`→`isNull()`)
  - 상속받은 `findById`는 인터페이스를 고칠 필요 없이 Kotlin 확장 함수 `findByIdOrNull`로 부른다
  - `CartService.validateCartLimit`의 매직넘버 10은 `MAX_CART_COUNT` 상수로 뺀다 (`kotlin/common.md` 예시와 같다)
  - `CartService`, `RegistrationService`의 `import static ExceptionCode.*`는 Kotlin에서 개별 import로 바꾼다
  - `DepartmentUpdateRequest.department`는 nullable이라 `MemberDepartment.from(request.department!!)`로 꺼낸다 (검증 통과 필드라 `!!` 허용)
  - Java Stream은 Kotlin 컬렉션 함수로 바꾼다. `toMap(...::id, identity())` → `associateBy { it.id }`, `groupingBy(..., LinkedHashMap::new, ...)` → `groupBy`(기본이 LinkedHashMap이라 순서가 유지된다), `filter(containsKey) + map(get)` → `mapNotNull { capacities[it.id]?.let { ... } }`
  - `CourseService.resolveArea`는 `Optional<CourseArea>` 대신 `CourseArea?`를 반환한다. 내부 함수라 Java 호출부가 없다
- [x] U10: AuthService, AdminAuthService, AdminCourseService, SystemSemesterService
  - `reissue(accessToken: String?)`는 nullable이어야 한다. 컨트롤러가 `@RequestHeader(required = false)`로 받아 헤더가 없으면 null이 오고, `JwtProvider`가 그걸 `JwtTokenMissingException`으로 바꾼다
    - non-null로 두면 Kotlin의 파라미터 null 검사가 먼저 터져 NPE가 난다. 스크래치 전체 테스트에서 `토큰이_없으면_예외가_발생한다` 2개가 실패해 발견했다
  - 완료할 때 레포지토리 3개를 nullable로 바꾼다: `MemberRepository.findByStudentId`, `findByEmail`, `AdminRepository.findByLoginId`
    - `AuthServiceTest` 3줄(132, 146, 280)의 `.orElseThrow()`를 뺀다
  - `SignUpRequest`의 9개 필드는 검증 통과 필드라 `!!`로 꺼낸다. `Member.create`는 이름 붙인 인자로 부른다
  - `instanceof` 패턴 매칭은 `is` + 스마트 캐스트로 옮긴다 (`AuthService.toDuplicateCode`)
  - `CourseSyncJobRepository`는 동기화 코드라 Java로 남는다. `Optional`을 그대로 받아 `.map { }.orElse(null)`로 쓴다

### 4. 컨트롤러 (끝나면 전체 테스트)

- [x] U11: AuthController, CartController, MemberController + 각 Docs
- [x] U12: CourseController, RegistrationController + 각 Docs
- [x] U13: AdminAuthController, AdminCourseController, AdminSemesterController + 각 Docs
  - Docs 8개는 클로드가 변환 스크립트로 옮겼다. 문자열 리터럴 384개가 원본과 바이트 단위로 같고, `@ApiResponse`·`@ExampleObject`·`@Operation` 개수도 같다
    - `AdminSyncControllerDocs`는 동기화 코드라 Java로 남는다
  - Docs 함수는 `override fun`으로 구현한다. 매핑·검증 어노테이션(`@GetMapping`, `@Valid`, `@ParamValidation`, `@RequestParam`)은 원본처럼 Docs와 컨트롤러 양쪽에 둔다
  - `required = false`인 `@RequestParam("area-code")`, `@RequestHeader("access-token")`은 `String?`로 받는다. 나머지 `String`은 Spring이 누락을 먼저 막아 non-null이다
  - `ACCESS_TOKEN_HEADER`는 companion의 `private const val`로 둔다. `const`라 어노테이션 인자로 쓸 수 있다
  - Java 호출부 수정은 없었다. 컨트롤러를 부르는 코드가 없다
  - 작성본에서 버그 3개를 찾았다 (스크래치 복사본에서 컴파일, 전체 테스트, api-docs 비교로 확인)
    - `AdminSemesterController.changeSystemSemester`에 `return`이 없어 컴파일 에러
    - `AdminAuthController`의 `@RequestMapping`이 `/api/v1/auth`여서 `AuthController.login`과 매핑이 겹쳐 컨텍스트가 뜨지 않았다 (테스트 364개 중 356개 실패)
    - `RegistrationController.registerCourse`의 `@PostMapping("/courseId")`에 중괄호가 빠졌다. 컴파일과 테스트는 통과하고 api-docs 비교에서만 드러났다
    - 그 밖에 `AdminAuthController`에 원본에 없던 `@Validated`, 안 쓰는 import 6개(`AuthController`에서 복사)가 있었다
  - 작성본은 스크래치패드 `controllers-user-backup/`에 백업하고, 합의한 컨벤션으로 클로드가 다시 정리했다. `AuthController`의 함수 순서(signUp, login, reissue, check 둘)는 작성본을 따랐다
  - 완료 확인: `clean` 뒤 전체 테스트 363개 통과. api-docs는 의도한 차이만 있다
    - 경로 `{courseId}` → `{course-id}` 4개 (장바구니 담기와 삭제, 수강신청과 취소)와 경로 파라미터 이름
    - `operationId` `reIssue` → `reissue`
    - 컨트롤러 테스트가 없어서 api-docs 비교를 따로 한다. 덤프용 임시 테스트는 스크래치에만 두었다

### 5. 그 외 (끝나면 전체 테스트)

- [x] U14: global/exception (ExceptionCode, RestApiException, JwtAuthenticationException, JwtTokenExpiredException, JwtTokenInvalidException, JwtTokenMissingException, GlobalExceptionHandler, AsyncExceptionHandler, CacheExceptionHandler)
  - 작성본은 `global/domain`, `global/handler`에 만들어져 import가 Java 원본을 가리켰다 (`e.message`가 `String?`로 보인 원인). 사용자 요청으로 클로드가 `global/exception/{domain,handler}`로 옮겼다
  - 완료할 때 표기 차이를 참고 파일 기준으로 맞췄다: 와일드카드 import 2곳(`HttpStatus.*`, `ExceptionCode.*`), trailing comma, `kotlin.RuntimeException` 표기, 120자 넘는 로그 호출, 파일 끝 닫는 괄호 하나 더, 파일 끝 개행. 의미 있는 차이는 없었다. 작성본은 스크래치패드 `u14-user-backup/`
  - Java 호출부 수정은 없었다. `src/main/java/uss/code/global/exception`은 비어서 지웠다
  - `ExceptionCode`는 `enum class` + `val status`, `code`, `message`다. Java 호출부의 `getStatus()`, `getCode()`, `getMessage()`가 그대로 산다
    - 항목 38개와 주석은 원본에서 스크립트로 복사했다 (문자열 76개 바이트 일치). 뒤에 멤버가 없어 마지막 `;`를 뺐다 (`CourseStatus`, `AdminRole`과 같은 표기)
  - `RestApiException`은 `val exceptionCode`다. 테스트의 `hasFieldOrPropertyWithValue("exceptionCode", ...)`가 그대로 통한다. 원본처럼 `RuntimeException()`에 메시지를 넘기지 않아 `message`는 null이다
  - `JwtAuthenticationException`은 하위 예외 3개가 상속하므로 `open class`다
    - `val code`와 `override val message: String`을 둔다. Java의 Lombok `@Getter`가 `getMessage()`를 덮어쓰던 것과 같다
    - `message`를 non-null로 덮어써서 Kotlin 핸들러가 `ErrorResponse.of(message = e.message)`로 바로 넘길 수 있다 (`Throwable.message`는 `String?`)
    - 하위 예외 3개는 본문이 없다. 원본의 `@Getter`는 붙일 필드가 없어 의미가 없었다
  - Spring 7 인터페이스는 JSpecify로 nullability를 표시한다 (패키지 `@NullMarked`). `@Nullable`인 곳만 nullable로 받는다
    - `AsyncExceptionHandler`: `vararg params: Any?`
    - `CacheExceptionHandler`: `handleCachePutError`의 `value: Any?`. `key`는 `Any`
  - `GlobalExceptionHandler`
    - `StringBuilder` 루프는 `joinToString("\n")` + 문자열 템플릿으로 옮겼다. 원본의 줄마다 `\n` + `trim()`과 결과가 같다 (null 값은 둘 다 `"null"`)
    - `instanceof` 패턴 매칭은 `is` 스마트 캐스트다. 지역 `var cause`도 람다에 잡히지 않으면 스마트 캐스트된다
    - `ErrorResponse.of` 호출은 이름 붙인 인자로 쓴다. `handleJwtAuthenticationException`은 `val response`에 담아 반환한다
    - 핸들러는 컨트롤러가 아니라 파라미터 1개를 한 줄에 쓴다
  - `CacheExceptionHandler.handleCacheGetError`의 로그 호출은 120자를 넘어 인자마다 줄바꿈했다
  - 로거는 모두 companion의 `LogManager.getLogger(...)`다
  - 완료할 때 Java 호출부 수정은 없을 것으로 본다. 스크래치 복사본이 수정 없이 컴파일됐다
  - 스크래치 확인: 컴파일, 전체 테스트 363개 통과
    - 컨트롤러 테스트가 없어 핸들러 응답을 검증하는 테스트가 없다. 스크래치 전용 덤프 테스트(스크래치패드 `ExceptionParityDumpTest.java`)로 변환 전후 출력 63줄이 같음을 확인했다
    - 덤프 대상: 에러 코드 38개, 예외의 `getMessage()`·`toString()`, 핸들러 응답 14가지(검증 오류 0개와 3개, enum 변환 실패의 직접·중첩 원인, null 대상 타입 등), 비동기·캐시 핸들러의 null 인자
- [x] U15: EnumValidation, ParamValidation, EnumValidator, ParamValidator, AdminEndpoint, WhitelistEndpoint, ApiPerformanceInterceptor, HttpLoggingFilter
  - 완료할 때 작성본의 버그 2개를 참고 파일 기준으로 고쳤다. 작성본은 스크래치패드 `u15-user-backup/`
    - `annotation/ParamValidator.kt`에 어노테이션이 `ParamValidator`라는 이름으로 선언돼 있었다. Java 원본을 지우면 `@ParamValidation`을 쓰는 컨트롤러와 Docs 4개, 검증기가 컴파일되지 않는다. 파일과 이름을 `ParamValidation`으로 바꿨다
    - `ApiPerformanceInterceptor`에 `@Component`가 빠져 있었다. `InterceptorConfig`가 생성자로 주입받으므로 빈이 없으면 컨텍스트가 뜨지 않는다
  - 표기 차이도 맞췄다: `:` 앞 공백(한 곳은 두 칸, 세 곳은 없음), 파일 끝 개행 5곳
  - Java 호출부 수정은 없었다. `src/main/java/uss/code/global`에는 `config`만 남았다
  - `EnumValidation`, `ParamValidation`은 `annotation class`다
    - Bean Validation 필수 속성 `message`, `groups`(`Array<KClass<*>>`), `payload`(`Array<KClass<out Payload>>`)를 둔다. Java 쪽에서 보면 `Class<?>[]`, `Class<? extends Payload>[]`로 원본과 같다
    - `@Target`은 Kotlin 것(`AnnotationTarget.FUNCTION, FIELD, VALUE_PARAMETER`)을 쓴다. 생성되는 Java `@Target`은 METHOD, FIELD, PARAMETER로 원본과 같고 순서만 다르다
    - 동기화 컨트롤러(Java)의 `@EnumValidation(target = SyncChangeType.class)`도 그대로 컴파일된다
  - 검증기의 `isValid(value: String?, context: ConstraintValidatorContext)`: 값은 null이 올 수 있어 nullable이다
  - `EnumValidator`의 `validValues`는 `initialize`에서 채우므로 `lateinit var`다. `toUpperCase()`는 `uppercase()`로 옮겼다 (U1~U3의 enum `from`과 같은 선택, 로캘에 따라 달라지지 않는다)
  - **공백 판정은 Java 기준을 유지한다 (2026-09-17 사용자 결정)**
    - Kotlin의 `isBlank()`/`trim()`은 Java의 `String.isBlank()`/`trim()`과 공백 기준이 다르다. Kotlin은 NBSP(` `, ` `, ` `)와 전각 공백(`　`)을 공백으로 보고, Java `trim()`은 `' '` 이하 문자(제어 문자 포함)만 자른다
    - 그대로 옮기면 `ParamValidator`에서 NBSP만 있는 값이 통과에서 실패로, 제어 문자만 있는 값이 실패에서 통과로 바뀌고, 앞뒤 전각 공백·제어 문자가 있는 값의 길이 계산이 달라진다
    - 그래서 `value.all(Character::isWhitespace)`(Java `isBlank`와 같다)와 `value.trim { it <= ' ' }`(Java `trim`과 같다)로 썼다. `HttpLoggingFilter.buildUri`의 빈 쿼리 판정도 같은 방식이다
  - `AdminEndpoint`, `WhitelistEndpoint`는 `object` + `@JvmStatic`이다. Java 필터가 `AdminEndpoint.isAdminPath(uri)`로 부른다
    - 내부 record는 `private data class EndPoint`다. `HttpMethod`는 enum이 아닌 클래스라 `name()`을 함수로 부른다
    - `equalsIgnoreCase`는 `equals(method, ignoreCase = true)`, 경로 연결은 문자열 템플릿이다
    - `EndPoint(...)` 호출은 인자 2개라 이름 붙인 인자로 한 줄에 하나씩 썼다. 목록이 길어진다 (`WhitelistEndpoint` 9개)
    - `substring(0, length - suffix.length)`는 `removeSuffix(...)`로 바꿨다. 앞에서 `endsWith`를 확인하므로 결과가 같다
  - `ApiPerformanceInterceptor`
    - `@Log4j2(topic = "API_PERF")`는 `LogManager.getLogger("API_PERF")`다. 로거 이름이 로그 설정의 라우팅 기준이라 바꾸지 않는다
    - `afterCompletion`의 네 번째 파라미터는 Spring 선언대로 `ex: Exception?`다 (`@Nullable`, 이름이 다르면 Kotlin이 경고한다)
    - `(Long) request.getAttribute(...)`는 `as Long?`다. 다른 타입이면 원본처럼 `ClassCastException`이 난다 (`as?`를 쓰면 조용히 null이 된다)
  - `HttpLoggingFilter`
    - `OncePerRequestFilter()`는 클래스라 괄호로 생성자를 부른다. `throws ServletException, IOException`은 Kotlin에 없다
    - Java `split("&")`는 끝의 빈 문자열을 버리고 Kotlin `split`은 남긴다. 결과를 맞추려고 `.dropLastWhile { it.isEmpty() }`를 붙였다 (`a=1&` → `a=1`)
    - `StringBuilder` 루프는 `joinToString(QUERY_DELIMITER) { ... }`, `substring(0, 16)`은 `take(16)`이다
  - 스크래치 확인: 컴파일(경고 없음), 전체 테스트 363개 통과
    - 스크래치 전용 덤프 테스트(스크래치패드 `U15ParityDumpTest.java`)로 변환 전후 378줄을 비교했다. `@Target` 순서 외에 같다
    - 덤프 대상: 어노테이션 속성과 기본값, Hibernate Validator로 실제 검증한 결과(텍스트 34개 x 3가지 제약), 경로 25개 x 메서드 7개의 화이트리스트 판정, 인터셉터(시작 시각 없음, 3초 초과, 잘못된 타입), 쿼리 23개의 마스킹 결과, 필터 제외 경로와 MDC 정리
- [x] U16: global/config 11 (ArgumentResolverConfig, AsyncConfig, CorsConfig, DataSourceConfig, FilterChainConfig, InterceptorConfig, P6SpyConfig, P6SpySqlFormatter, RedisCacheConfig, SchedulingConfig, SwaggerConfig)
  - 작성본은 `src/main/kotlin/uss/code/config`(패키지 `uss.code.config`)에 만들어져 있었다. 참고 파일 내용으로 `global/config`에 두고 작성본 폴더를 지웠다. 작성본은 스크래치패드 `u16-user-backup/`
    - 제자리에 둔 11개는 스크래치에서 전체 테스트, 덤프, api-docs 비교를 거친 파일과 바이트 단위로 같다
  - 작성본에서 고친 버그
    - `FilterChainConfig`에 `@Configuration`이 없었다. 필터 5개(CORS, 로깅, JWT 예외, JWT 인증, 관리자 인증)가 등록되지 않아 토큰 없이 요청이 통과한다. 컨트롤러 테스트가 없어 전체 테스트로는 드러나지 않는다
    - `RedisCacheConfig`에 `@Configuration`, `@EnableCaching`이 없었다. 캐시, 에러 핸들러, 커스터마이저, 기동 시 캐시 비우기가 모두 빠진다
    - `SwaggerConfig` 끝에 클래스를 닫는 `}`가 없었다 (컴파일 에러)
    - `P6SPYConfig`(파일명, 클래스명)를 `P6SpyConfig`로 바꿨다. Java 원본 `uss.code.global.config.P6SpySqlFormatter`를 가리키던 import도 뺐다
  - 참고 파일 기준으로 맞춘 표기 차이
    - `AsyncConfig.getAsyncExecutor()`의 반환 타입 `Executor?`를 `Executor`로 (IDE가 Spring의 `@Nullable` 선언을 그대로 옮긴 것으로 보인다. 항상 값을 돌려주므로 non-null이 컨벤션에 맞다)
    - `RedisCacheConfig`의 `REJECT_COMMANDS` 항목 직접 import를 `DisconnectedBehavior.REJECT_COMMANDS`로
    - `:`, `{` 앞 공백(4개 파일), trailing comma(`ArgumentResolverConfig`, `FilterChainConfig`, `RedisCacheConfig` 2곳), 클래스 선언 뒤 빈 줄(`CorsConfig`, `DataSourceConfig`), `FilterChainConfig`의 의존성 그룹 빈 줄과 companion 앞 빈 줄, 파일 끝 개행 11개
  - Java 호출부 수정은 없었다
  - 참고 파일: `kotlin-reference/uss/code/global/config/` (완료 후 지웠다)
  - 설정 클래스는 `@Configuration`만 붙인다. `kotlin-spring`이 `open`으로 만들어 CGLIB 프록시가 원본처럼 생긴다 (덤프로 확인)
  - `@RequiredArgsConstructor` + `private final` 필드는 주 생성자다. `FilterChainConfig`의 빈 줄 그룹은 원본대로 뒀다
  - Java 인터페이스 구현
    - `addArgumentResolvers(resolvers: MutableList<...>)`: 받은 리스트에 `add`하므로 `MutableList`다
    - `AsyncConfigurer.getAsyncExecutor()`는 Kotlin에서도 `override fun`이다. 게터처럼 생겼지만 인터페이스 함수라 프로퍼티로 구현하지 않는다
    - `getAsyncExecutor`, `getAsyncUncaughtExceptionHandler`, `errorHandler`는 Spring 선언이 `@Nullable`이지만 항상 값을 돌려주므로 반환 타입을 non-null로 좁혔다
  - Java 세터 호출은 프로퍼티 대입으로 옮긴다 (`configuration.allowedOrigins = ...`, `executor.corePoolSize = ...`, `bean.order = ...`, `P6SpyOptions.getActiveInstance().logMessageFormat = ...`)
    - 예외: getter와 setter의 nullability가 다르면 Kotlin은 읽기 전용 프로퍼티로만 본다. 대입하면 `'val' cannot be reassigned` 컴파일 에러가 난다
      - `FilterRegistrationBean.setFilter(...)`: getter는 `T?`, setter는 `T`
      - `ThreadPoolTaskExecutor.setThreadNamePrefix(...)`: getter는 `String`, setter는 `String?`
    - `P6SpyOptions.getActiveInstance()`는 static 함수라 프로퍼티가 되지 않는다. 반환 타입(`P6SpyLoadableOptions`)이 상속한 MBean 인터페이스에 `logMessageFormat` getter와 setter가 있다
  - `AsyncConfig`의 `10`, `"asyncThread-"`는 `CORE_POOL_SIZE`, `MAX_POOL_SIZE`, `THREAD_NAME_PREFIX` 상수로 뺐다 (U9의 `MAX_CART_COUNT`와 같다). Swagger 문서 문구와 P6Spy의 SQL 키워드는 Docs 문자열처럼 원본대로 뒀다
  - `List.of(...)`, `String[]` 상수는 companion의 `private val ... = listOf(...)`다. `excludePathPatterns`는 원본의 가변 인자 대신 `List<String>` 오버로드로 불린다
  - `RedisCacheConfig`
    - Java 람다는 SAM 생성자(`RedisCacheManagerBuilderCustomizer { builder -> ... }`)로 옮겼다. 인터페이스 함수가 `void`라 마지막 식의 값은 버려진다
    - `ApplicationRunner`는 인자를 쓰지 않아 파라미터 선언을 생략했다
    - `Objects.requireNonNull(cache).clear()`는 `checkNotNull(cache).clear()`다. `!!`를 쓰지 않는 규칙 때문이고, 예외 타입만 `NullPointerException`에서 `IllegalStateException`으로 바뀐다
      - 캐시 이름을 같은 `CacheManager`에서 받아 실제로는 null이 오지 않는다. 기동 시점 코드라 `require`/`check` 금지 규칙의 근거(요청 처리 중 500 응답)와도 관계없다
    - `new JacksonJsonRedisSerializer<>(CachedCoursesDto.class)`는 `JacksonJsonRedisSerializer(CachedCoursesDto::class.java)`다. 타입 인자는 추론된다
  - `SwaggerConfig`
    - `static { }` 블록은 companion의 `init { }`이다. 클래스가 로드될 때 한 번 실행된다. api-docs에서 `@Auth` 파라미터가 계속 숨겨지는 것을 확인했다
    - `in`은 Kotlin 키워드라 `` .`in`(SecurityScheme.In.HEADER) ``처럼 백틱으로 감싼다
    - `servers()`는 `ArrayList` 대신 `listOf`로 만든다. springdoc은 받은 서버 목록을 고치지 않고 복사하거나 새 목록으로 바꾼다 (바이트코드 확인)
    - 빈 이름이 함수 이름에서 나오므로 `openAPI`를 그대로 둔다
  - `P6SpySqlFormatter`
    - P6Spy 인터페이스에는 nullability 표시가 없다. P6Spy가 직접 만드는 `now`(날짜 문자열), `category`(`Category.toString()`)는 non-null로, 문장과 연결 정보를 그대로 넘기는 `prepared`, `sql`, `url`은 nullable로 받는다 (바이트코드 확인)
      - non-null로 선언한 파라미터에 null이 오면 Kotlin의 파라미터 검사가 NPE를 던져 쿼리 로깅 경로가 깨진다
    - `sql.trim()`은 `sql.trim { it <= ' ' }`다 (U15와 같은 공백 기준). `toLowerCase(Locale.ROOT)`는 `lowercase()`다
    - `String.format(...)`은 문자열 템플릿으로 옮겼다. `%d`는 기본 로캘의 숫자를 쓰고 템플릿은 항상 ASCII 숫자라, 아랍어처럼 다른 숫자를 쓰는 로캘에서만 결과가 다르다
    - `Category`는 enum이 아닌 클래스이고 `getName()` getter가 있어 `Category.STATEMENT.name`으로 읽는다. `FormatStyle.DDL.getFormatter()`도 `formatter`다
    - 여러 줄 `if` 조건은 Kotlin 공식 형식이다. 연산자 `||`는 줄 끝에 두고, 닫는 괄호는 `{`와 함께 다음 줄에 둔다
  - `P6SpySqlFormatter.class.getName()`은 `P6SpySqlFormatter::class.java.name`이다. P6Spy가 이 이름으로 리플렉션 생성한다
  - `SchedulingConfig`는 본문이 없어 중괄호를 생략했다
  - 완료할 때 Java 호출부 수정은 없을 것으로 본다. 설정 클래스를 부르는 코드가 없고(테스트 주석 한 줄뿐), 스크래치 복사본이 수정 없이 컴파일됐다
  - 스크래치 확인: 컴파일(경고 없음), `clean` 뒤 전체 테스트 363개 통과
    - 덤프 테스트(스크래치패드 `U16ParityDumpTest.java`)로 변환 전후 262줄을 비교했다. 차이는 위에 적은 두 가지뿐이다: 없는 캐시의 예외 타입, 서버 목록 구현 클래스(`ArrayList` → `Arrays$ArrayList`)
    - api-docs(47KB)는 바이트 단위로 같다
    - 덤프 대상: 설정 빈과 `@Bean` 함수(이름, 타입, `@Primary`, CGLIB), CORS 설정과 실제 preflight 4가지, 필터 등록 5개의 순서, 인터셉터 제외 경로 판정 9개, 인자 리졸버 순서, 비동기 실행기 설정과 실제 스레드 이름, 캐시 에러 핸들러와 캐시별 직렬화 설정, Lettuce 연결 끊김 동작, 캐시 비우기, P6Spy 포맷 95가지, 데이터소스 설정, OpenAPI 빈, 인증 필터 응답 4가지, `@EnableAsync`·`@EnableScheduling` 적용
- [x] U17: Auth, AdminAuth, AdminAuthenticationFilter, JwtAuthenticationFilter, JwtExceptionFilter, JwtProvider, MemberPasswordEncoder, AdminAuthArgumentResolver, AuthArgumentResolver
  - 작성본은 `global/{annotation,filter,infra,resolver}`(패키지 `uss.code.global.*`)에 만들어져 있었다. 참고 파일 경로와 `project-structure.md`(`auth/`에 JWT 필터, 리졸버, 비밀번호 인코딩)에 맞춰 `auth/{annotation,filter,infra,resolver}`로 옮겼다. 작성본은 스크래치패드 `u17-user-backup/`
    - 그대로 두고 Java 원본을 지우면 `uss.code.auth.*`를 import하는 Kotlin 코드(`FilterChainConfig`, `ArgumentResolverConfig`, `SwaggerConfig`, 컨트롤러의 `@Auth`, 서비스의 `JwtProvider`·`MemberPasswordEncoder`)와 Java `AdminSyncController`가 모두 깨진다
    - 작성본 `JwtProvider`가 이름이 같은 Java 원본 `uss.code.auth.infra.JwtProvider`를 import하고 있었다. `AuthArgumentResolver`도 Java 원본 `uss.code.auth.annotation.Auth`를 가리켰다 (IDE 자동 import로 보인다)
  - 참고 파일 기준으로 맞춘 표기 차이: 와일드카드 import 2곳(`AnnotationTarget.*`, `AnnotationRetention.*`), 안 쓰는 import 2개(`JwtExceptionFilter`의 `HttpServlet`, `JwtProvider`의 `ExceptionCode`), `ExceptionCode.INVALID_ACCESS_TOKEN` → 항목 import, trailing comma 10곳, `{`·`(` 앞 공백(`Long{`, `?){`, `if(`, `try{`, `}catch` 등 4개 파일), `AdminAuthenticationFilter`의 빈 줄 하나, 파일 끝 개행 7개
  - 의미 있는 차이는 작성본대로 뒀다
    - `JwtProvider` 함수 순서: `validateToken`, `validateAdminToken`, `getMemberIdAllowingExpiration`이 `parseJwt` 뒤에 있다
    - `extractRole`, `MemberPasswordEncoder.matches`의 `return try { ... }` 식 형태. 기존 코드(`AuthService`, `HttpLoggingFilter`, `CourseArea`)에도 있는 형태라 U18 `AdminPasswordEncoder` 참고 파일도 이 형태로 썼다
  - Java 호출부 수정은 없었다
  - 참고 파일: `kotlin-reference/uss/code/auth/{annotation,filter,infra,resolver}/` (완료 후 지웠다)
  - **완료할 때 컴파일 전에 `./gradlew clean`을 먼저 돌린다.** `getMemberId` 등 4개 함수의 반환 타입이 `Long`에서 `long`으로 바뀐다 (U10과 같은 상황)
  - `Auth`, `AdminAuth`는 본문 없는 `annotation class`다. `@Target(AnnotationTarget.VALUE_PARAMETER)`가 Java의 `PARAMETER`가 된다
    - Java `AdminSyncController`의 `@AdminAuth`, `SwaggerConfig`의 `Auth::class.java`가 그대로 쓰인다
  - `JwtProvider`
    - 토큰을 받는 public 함수 7개는 모두 `String?`다. 필터가 헤더(`getHeader`, 없으면 null)를 그대로 넘기고, 테스트가 `validateAdminToken(null)`을 부른다
      - `validateToken`은 null이면 예외를 던지므로, 뒤의 `validateAccessToken(accessToken)`에서는 스마트 캐스트로 `String`이 된다
    - 생성자: `secretKey` 파라미터는 프로퍼티가 아니고, 같은 이름의 `SecretKey` 프로퍼티 초기화에만 쓴다 (`Member`의 `var department = department`와 같은 형태)
    - `@Value`는 어노테이션을 파라미터 윗줄에 둔다. 같은 줄에 두면 한 줄이 120자를 넘는다
      - 생성자 프로퍼티(`private val`)에는 `@param:Value`로 적용 대상을 밝힌다. 안 밝히면 Kotlin 2.2가 "지금은 파라미터에만 적용되지만 앞으로 필드에도 적용된다"고 경고한다
      - 일반 파라미터 `secretKey`는 대상이 파라미터뿐이라 `@param:`을 붙이면 오히려 "불필요한 대상" 경고가 난다
      - 설정 묶음 규칙(`@ConfigurationProperties` + data class)으로 바꾸는 것은 구조 변경이라 이번에 하지 않았다
    - Java의 다중 catch(`JwtException | IllegalArgumentException`)는 Kotlin에 없어 catch를 두 개로 나눴다. 순서는 원본과 같다
    - `Long.valueOf(claims.getSubject())` 세 곳은 `extractId(claims)`로 모았다 (`extractRole`과 짝). 원본에 없던 private 함수다
      - `subject`는 Java 플랫폼 타입이라 컨벤션대로 `String`으로 받고 null이면 `?: throw JwtTokenInvalidException(INVALID_ACCESS_TOKEN)`
      - 처음에는 `.subject.toLong()`으로 썼는데, Kotlin이 플랫폼 타입 수신 객체에 null 검사를 넣어 subject가 없으면 NPE가 났다 (덤프에서 발견)
      - 그 결과 subject 없는 토큰의 응답이 원본과 달라진다: 원본은 `getMemberId`에서 `NumberFormatException`(500), Kotlin은 401 AUTH-002. `getMemberIdAllowingExpiration`의 정상 경로는 원본도 AUTH-002라 같다
      - 우리 비밀키로 서명했는데 subject가 없는 토큰이어야 생기는 경우라 실제로는 도달하지 않는다
    - `String.valueOf(id)`는 `id.toString()`, `get(ROLE_CLAIM, String.class)`는 `get(ROLE_CLAIM, String::class.java)`다. 역할 클레임은 없을 수 있어 `extractRole`은 `String?`를 반환한다
  - 필터 3개
    - 헤더 값은 `val accessToken: String? = request.getHeader(...)`로 nullability를 밝힌다. `request.requestURI`는 U15의 `HttpLoggingFilter`처럼 `String`으로 받는다
    - 중괄호 없는 `if (...) throw`는 블록으로 쓴다. 여러 줄 `||`는 줄 끝에 둔다
    - `JwtAuthenticationFilter`, `AuthArgumentResolver`의 문자열 `"member-id"`는 관리자 쪽처럼 `MEMBER_ID_ATTRIBUTE` 상수로 뺐다. `JwtExceptionFilter`의 content type도 `JSON_CONTENT_TYPE` 상수다
    - `import static ...HttpServletResponse.SC_UNAUTHORIZED`는 Kotlin에서 `import jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED`로 바로 가져온다
    - `response.setContentType(...)`, `setStatus(...)`는 프로퍼티 대입이다 (getter와 setter 타입이 같다)
    - `e.message`는 U14에서 `JwtAuthenticationException.message`를 non-null로 덮어써서 `String`으로 바로 넘어간다
  - 리졸버 2개: Spring 7 선언대로 `mavContainer: ModelAndViewContainer?`, `binderFactory: WebDataBinderFactory?`, 반환 `Any?`다. 속성이 없으면 null을 돌려주는 원본 동작과 같다
    - `(HttpServletRequest) webRequest.getNativeRequest()`는 `webRequest.nativeRequest as HttpServletRequest`다 (다른 타입이면 원본처럼 `ClassCastException`)
  - `MemberPasswordEncoder`의 파라미터는 non-null이다. Kotlin 호출부(`AuthService`)가 `request.password!!`로 넘긴다
    - 원본은 null을 받으면 문자열 `"null"`을 해시했다. Kotlin은 NPE다. 호출부가 모두 Kotlin이라 null이 올 수 없다
  - 완료할 때 Java 호출부 수정은 없을 것으로 본다. 스크래치 복사본이 수정 없이 컴파일됐다 (테스트의 `new JwtProvider(...)`, `getMemberId(...)` 포함)
  - 스크래치 확인: 컴파일(경고 없음), `clean` 뒤 전체 테스트 363개 통과
    - 덤프 테스트(스크래치패드 `U17ParityDumpTest.java`)로 변환 전후 624줄을 비교했다. 차이는 위에 적은 것뿐이다: 반환 타입 `Long` → `long`, `throws` 선언, null 비밀번호, subject 없는 토큰
      - 잡음은 정규화했다: bcrypt 해시(salt가 매번 다름), 로그인 검증 메시지의 필드 순서(원본끼리도 실행마다 다름)
    - 덤프 대상: 어노테이션 정의, 공개 시그니처, 컨트롤러 파라미터의 리졸버 지원 여부, 리졸버 반환값(없음, Long, 다른 타입, 서블릿이 아닌 요청), 비밀번호 해시와 비교 15가지, 토큰 28종 x `JwtProvider` 함수 7개, 발급 토큰의 헤더·클레임·유효 시간, 예외 필터 11가지(응답 커밋 후 포함), 필터 제외 경로 14개 x 메서드 3개, 토큰 28종 x 필터 2개 x 경로 2개, 실제 HTTP 요청 6개 경로 x 토큰 30종
    - 참고: 컨트롤러 파라미터 확인 중 `POST /api/v1/admin/sync/jobs`의 `@AdminAuth adminId`가 Swagger에 필수 쿼리 파라미터로 보이는 것을 봤다 (`SwaggerConfig`가 `Auth`만 숨긴다). 동기화 코드라 #127에서 사라진다
- [x] U18: CourseCacheLoader, CourseCacheWarmer, CourseScheduleFormatter, CourseValidator, SearchKeywordSanitizer, RegistrationTypeResolver, AdminPasswordEncoder, UssServerApplication
  - 작성본은 8개 모두 제자리에 있었고 의미 있는 차이가 없었다. 표기 차이만 참고 파일 기준으로 맞췄다. 작성본은 스크래치패드 `u18-user-backup/`
    - trailing comma 2곳, `(`·`{` 앞 공백(`CourseCacheLoader (`, `){`, `CachedCoursesDto{`, `String{`, `warmMajorCourses(){`, `main(...){`, `isNotEmpty()}`), 클래스 선언 뒤 빈 줄 2곳, `isSameGroup`의 연속 줄 들여쓰기(8칸 → 4칸), 파일 끝 개행 8개
    - 전공 쪽 `@Scheduled`도 인자마다 줄바꿈돼 있었다. 120자 안이라 참고 파일처럼 한 줄로 되돌렸다 (U14 로그 호출처럼 120자를 넘는 것만 줄바꿈)
  - Java 원본 8개를 지웠고 Java 호출부 수정은 없었다. `src/main/java`의 `course/infra`, `registration/infra`는 비어서 지웠다
  - 참고 파일: `kotlin-reference/uss/code/{course,registration,admin}/infra/`, `kotlin-reference/uss/code/UssServerApplication.kt` (완료 후 지웠다. `kotlin-reference/`가 비었다)
  - `@UtilityClass` 4개는 `object`다. 상수는 `object` 본문에 두고, `const`가 안 되는 값(`Regex`, `setOf`, `GENERAL_ELECTIVE.displayName`)은 `private val`
    - `@JvmStatic`은 `CourseScheduleFormatter.format`에만 붙인다. Java 호출부가 있다 (동기화 코드 `CourseSyncApplier`, 테스트 `CourseScheduleFormatterTest`)
    - `CourseValidator`, `SearchKeywordSanitizer`, `RegistrationTypeResolver`는 호출부가 모두 Kotlin 서비스라 붙이지 않는다. Java에서 보면 `INSTANCE`의 인스턴스 함수가 된다
  - `CourseCacheLoader`
    - `MAJOR_COURSES`, `GENERAL_EDUCATION_COURSES`는 companion의 public `const val`이다. `RedisCacheConfig`가 `CourseCacheLoader.MAJOR_COURSES`로 읽고, 어노테이션 인자라 `const`여야 한다
    - `cacheNames`는 배열 속성이라 `[MAJOR_COURSES]`로 쓴다. `value`가 아닌 배열 속성은 vararg로 받지 않는다
    - 캐시 키 SpEL `#memberDepartment.name()`은 파라미터 이름으로 값을 찾는다. Kotlin 컴파일에 `-java-parameters`가 없어도 Spring이 `kotlin-reflect`로 이름을 읽는다. 덤프에서 키가 원본과 같음(`COMPUTER_ENGINEERING`)을 확인했다
      - 그래서 이 함수들의 파라미터 이름을 바꾸면 캐시 키가 깨진다
    - `.stream().map(CachedCourseDto::from).toList()`는 기존 서비스와 같은 `.map { CachedCourseDto.from(it) }`다. 목록 구현이 불변 리스트에서 `ArrayList`로 바뀌지만 DTO가 `List`로 노출하고 고치는 곳이 없다
    - `kotlin-spring`이 `@Component` 클래스를 `open`으로 만들어 캐시 프록시(CGLIB)가 원본처럼 생긴다 (덤프 확인)
  - `CourseCacheWarmer`
    - `@ConditionalOnProperty(name = ["spring.cache.type"], ...)`: `name`도 배열 속성이다. `@EventListener(ApplicationReadyEvent::class)`는 `value` 속성이라 하나만 넘긴다
    - `@Scheduled`의 `${...}`는 `\$`로 이스케이프한다 (`JwtProvider`의 `@Value`와 같다). 교양 쪽은 120자를 넘어 인자마다 줄바꿈했다
    - `Arrays.stream(Enum.values())`는 `Enum.entries`다. `forEach(courseCacheLoader::refreshMajorCourses)`는 반환값이 있는 함수 참조지만 Kotlin이 `Unit` 반환으로 맞춰 준다
  - `CourseScheduleFormatter`
    - `Comparator.comparing(...).thenComparing(...)`는 `compareBy(CourseSchedule::dayOfWeek).thenBy(CourseSchedule::startTime)`다. `sortedWith`는 새 리스트를 돌려주는 안정 정렬이라 입력 보존 테스트와 같은 요일·시각의 순서가 원본과 같다
    - `String.join`은 `joinToString`, `+` 연결은 문자열 템플릿이다. `formatGroup`은 기간 목록을 `joinedPeriods`로 먼저 만든다 (템플릿 한 줄이 너무 길어진다)
    - `sorted.get(0)`은 `sorted.first()`, 루프에서 바뀌는 `groupHead`는 `var`
  - `CourseValidator`
    - 중첩 `for` 루프는 Stream이 아니라 그대로 옮겼다. 120자를 넘는 `if` 조건은 `&&`를 줄 끝에 두고 줄바꿈했다
    - `filter().count()`(long)는 `count { }`(Int), `mapToInt().sum()`은 `sumOf { }`다. `member.getMaxCredit()`는 커스텀 getter 프로퍼티 `maxCredit`
    - `import static CourseType.OCU`는 `import uss.code.course.domain.CourseType.OCU`, `OCU.getCode().equals(typeCode)`는 `OCU.code == typeCode`
  - `SearchKeywordSanitizer`
    - `Pattern.compile`은 `Regex`다. 정규식은 raw string(`"""..."""`)이라 `\\`, `\"` 이스케이프가 없다. `Regex`는 `java.util.regex.Pattern`을 감싸므로 `\s`의 범위와 `replace`의 치환 규칙이 원본과 같다
    - `trim()`은 `trim { it <= ' ' }`다 (2026-09-17 결정). 덤프에서 `a` → `a`, ` a ` → 그대로를 원본과 확인했다. Kotlin `trim()`이면 둘 다 결과가 바뀐다
  - `RegistrationTypeResolver`: `Set.of(...)`는 `setOf(...)`, `.contains(x)`는 `x in ...`, `GENERAL_ELECTIVE.getName()`은 `displayName`
  - `AdminPasswordEncoder`는 U17 완료본 `MemberPasswordEncoder`와 같은 모양이다 (`return try`). 파라미터는 non-null이다. 호출부 `AdminAuthService`가 `request.password!!`로 넘긴다
    - 원본은 null을 받으면 해시를 만들거나 `false`를 돌려줬다. Kotlin은 NPE다 (U17과 같은 차이)
  - `UssServerApplication`
    - Spring 공식 Kotlin 형태로 옮겼다: 본문 없는 `class UssServerApplication` + 최상위 `fun main` + `runApplication<UssServerApplication>(*args)`. `*`는 배열을 vararg로 펼친다
    - `main`이 `UssServerApplicationKt` 클래스로 옮겨진다. bootJar의 `Start-Class`가 `uss.code.UssServerApplicationKt`로 자동으로 잡힌다 (확인). Dockerfile은 `java -jar`라 영향이 없다. IDE 실행 구성이 옛 클래스를 가리키면 다시 만든다
    - `kotlin-spring`이 `@SpringBootApplication` 클래스를 `open`으로 만든다 (`@Configuration` 메타 어노테이션)
  - 완료할 때 Java 호출부 수정은 없을 것으로 본다. 스크래치 복사본이 수정 없이 컴파일됐다
  - 스크래치 확인: 컴파일(Lombok 실험 기능 안내 외 경고 없음), `clean` 뒤 전체 테스트 364개(덤프 1개 포함) 실패 0
    - 스킵 6개는 `CourseServiceSearchTest`다. `@Testcontainers(disabledWithoutDocker = true)`라 로컬에 Docker가 없으면 스킵된다. 검색어 정제 결과는 덤프로 비교했다
    - 덤프 테스트(스크래치패드 `U18ParityDumpTest.java`)로 변환 전후 약 3500줄을 비교했다. 차이는 위에 적은 것뿐이다: `object`의 `INSTANCE`와 인스턴스 함수, `List<? extends ...>`, `Pattern` → `Regex` 필드, companion 필드, `main` 위치, `@Scheduled` 속성 출력 순서(값은 같다), null 비밀번호, 캐시 DTO의 목록 구현 클래스
    - 덤프 대상: 공개 시그니처와 어노테이션, 시간표 문자열 407가지(무작위 400 포함), 시간 충돌 500가지, 유형 제한 108가지, 학점 제한 252가지, 검색어 정제 315가지, 수강 구분 1840가지, 비밀번호 12가지, 캐시 적중·갱신과 키, 워머의 조건부 등록(redis/none/simple)과 기동 이벤트 호출, 스케줄 cron·zone

### 6. 마무리

`src/main/java`에 Java 코드가 남아 있지 않을 때 한다.

- [ ] `interop.md`의 전환용 장치 제거 (`@JvmStatic`, `@JvmRecord`, `@get:JvmName`, `Optional` 반환을 nullable로)
- [ ] Lombok 의존성, `kotlin-lombok` 플러그인 제거, `interop.md` 삭제
- [ ] (선택) `build.gradle` → `build.gradle.kts`, 테스트 코드 Kotlin 전환

## 결정 기록

- 컨벤션: enum `name` → `displayName`, 엔티티 `var id: Long = 0L`, Request DTO 필드 nullable(검증 통과 필드만 `!!`), 상수와 로거는 `companion object`에 둔다 (2026-09-15에 파일 최상단에서 바꿨다)
- dev에서 추가된 `CourseDepartmentKind`는 U2에 넣었다
- 테스트는 레이어가 끝날 때 한 번 돌린다. 단위마다는 컴파일만 한다 (2026-09-15)
- Java 테스트 픽스처의 `new Entity()`는 `BeanUtils.instantiateClass(Entity.class)`로 바꾼다. kotlin-jpa의 기본 생성자는 소스에서 호출할 수 없다
- U1, U2 모두 사용자 요청으로 클로드가 작성본을 참고 파일 내용으로 교체해 표기를 정리했다. U1은 `src/main/java`에 작성돼 `src/main/kotlin`으로 옮겼다
- U3도 사용자 요청으로 교체했다. 빠진 함수(`replaceType`, `is75MinLesson`, `isRegisterable`)와 `forEach { this::addCourseSchedule }`(컴파일은 되지만 아무것도 안 한다)가 있었다
- DTO는 `@JvmRecord data class`로 옮긴다. Java 호출부의 record 접근자와 메서드 참조가 그대로 살고, Jackson도 record로 다뤄 JSON 키(`isEnglish`, `isNight`, `isClosed`)가 유지된다. Redis 캐시 직렬화/역직렬화도 확인했다 (2026-09-16)
- DTO 프로퍼티 사이 빈 줄은 `request/`, `response/`만 넣는다. 어노테이션이 붙지 않는 `internal/`은 붙여 쓴다 (2026-09-16, 사용자 결정)
- 컨트롤러 단위에서 사용자와 컨벤션을 합의했다 (2026-09-17). 문서는 `kotlin/controller.md`, `kotlin/common.md`, 예시는 `domain.md`, `dto.md`에 반영했다
  - 함수 본문은 모든 Kotlin 코드에서 블록(`{ }`)으로 쓴다. `=` 표현식 본문과 `get() =`를 쓰지 않는다. 기존 코드의 `=` 본문 80개(도메인, DTO, 서비스)와 `Member.maxCredit` getter를 스크립트로 바꿨다
  - 컨트롤러 응답은 `ResponseEntity.status(상태).body(response)`, body가 없으면 `.status(상태).build()`로 통일한다. `ok()`, `noContent()`를 쓰지 않는다. 상태는 `HttpStatus` 항목을 하나씩 import한다
  - 컨트롤러 본문은 `val response = 서비스 호출` 다음 줄에 바로 `return`한다. 사이에 빈 줄을 넣지 않는다
  - 컨트롤러와 Docs의 파라미터는 개수와 상관없이 한 줄에 하나씩 쓰고, 파라미터에 붙는 어노테이션은 같은 줄에 쓴다
    - 이 줄바꿈 규칙의 적용 범위는 컨트롤러와 Docs다. 서비스를 포함한 다른 레이어는 기존 규칙(파라미터 2개 이상이면 줄바꿈)을 따른다
      - 서비스에도 적용했다가 사용자 결정으로 되돌렸다 (2026-09-17)
  - URL에 드러나는 이름은 케밥 케이스다. 쿼리 파라미터와 헤더는 이미 케밥이라 그대로 두고, 경로 변수만 `{courseId}` → `{course-id}`로 바꿨다. 실제 요청 URL은 달라지지 않는다
    - 지난 측정 기록(`.claude/resources/perf`, `concurrency`)과 `optimize-performance` 스킬 문서의 `{courseId}`는 라벨이라 그대로 뒀다
  - 생성자 주입 파라미터는 계층별로 묶는다 (사용자 결정). Repository끼리, Service끼리 한 그룹이고, 계층 밖 컴포넌트(프로바이더, 인코더, 리졸버, 로더)는 따로 묶어 빈 줄로 나눈다. Service는 Repository 그룹, Controller는 Service 그룹이 맨 위다
    - 기존 규칙(도메인별 그룹핑)을 대체한다. 그룹 안에서 자기 도메인을 먼저 쓰는 순서는 유지했다
    - `AdminAuthService`, `CartService`, `CourseService`, `RegistrationService` 4곳을 고쳤다. 테스트에 서비스를 직접 생성하는 코드가 없어 순서 변경의 영향이 없다
    - Java 컨벤션(`java/common.md`)은 남은 Java 코드용이라 그대로 뒀다
- U10 작성본에서 `AdminAuthService`의 닫는 중괄호 누락(컴파일 에러), `SystemSemesterService`의 파라미터 오타(`requst`), 와일드카드 import(`ExceptionCode.*`), 안 쓰는 `jakarta.validation.constraints.Email` import, trailing comma, `{` 앞 공백, 파일 끝 개행을 정리했다 (2026-09-17)
  - `AuthService`의 의미 있는 차이 3개는 그대로 뒀다: `reIssue` → `reissue` 이름 변경, 생성자 파라미터 순서(`memberRepository` 먼저), `checkEmailAvailability`·`checkStudentIdAvailability`를 `reissue` 뒤로 옮긴 순서
  - 토큰 재발급 함수 이름은 `reissue`로 통일한다 (2026-09-17, 사용자 결정). `AdminAuthService`, `AuthControllerDocs`, `AuthController`(Java와 참고 파일), `AdminAuthController`의 호출부, 테스트(`AuthServiceTest` 6줄, `AdminAuthServiceTest` 7줄)를 고쳤다
    - Swagger `operationId`가 `reIssue`에서 `reissue`로 바뀐다. 경로(`/re-issue`)는 그대로다
    - 지난 계획 문서(`.claude/resources/plans/PLAN-*.md`)의 `reIssue`는 기록이라 두었다
- U9 작성본에서 `CartService.deleteCartedCourse` 함수 누락, 와일드카드 import(`ExceptionCode.*`), companion object 위치(클래스 상단), 팩토리 대신 생성자 직접 호출(`CourseCategoriesResponse(...)`), 단수형 지역 변수 이름을 정리했다 (2026-09-16)
- U6, U7 작성본에서 `@JvmRecord` 5개 누락, 와일드카드 import(`EnumType.*`, `GenerationType.*`), 파일 끝 개행 없음을 정리했다. `SystemSemesterRequest`의 `@Min`/`@Max`를 2000/2100으로 바꾼 것은 의미가 있는 변경이라 그대로 두고 사용자에게 확인을 요청했다 (2026-09-16)
- U4는 사용자 요청으로 `@JvmRecord` 누락 3개, trailing comma 4곳, `) {`·`companion object {` 공백만 고쳤다. `CourseResponse`의 `from`/`of` 순서는 작성본 그대로 뒀다
- DTO 패키지는 `dto/common` 대신 `dto/internal`, 클래스 이름은 `~Dto`로 한다 (2026-09-16, 사용자 결정). Kotlin으로 옮기는 단위에서 바꾸고 Java에 남은 파일은 그대로 둔다. 문서는 `kotlin/dto.md`, `java/dto.md`, `kotlin/repository.md`, `project-structure.md`에 반영했다
- 사용자가 물어본 Kotlin 문법은 루트 `NOTE.md`(gitignore)에 모은다 (2026-09-15)
- 2026-09-17 사용자 결정 2건
  - IDE가 커밋된 Kotlin 파일 14개의 import를 와일드카드로 합쳐 놓은 변경은 그대로 둔다. 되돌리지 않고 `.editorconfig`도 추가하지 않는다
    - 새로 옮기는 파일은 지금처럼 참고 파일 기준(개별 import)으로 맞춘다
  - Java `trim()`/`isBlank()`의 공백 기준을 유지한다 (`trim { it <= ' ' }`, `all(Character::isWhitespace)`). U15 검증기·필터와 U16 `P6SpySqlFormatter`에 적용했다
