 ㅕ # Kotlin 마이그레이션 TODO

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
- **다음: U9 참고 파일 준비 (클로드)**
- 커밋: U1~U7(엔티티·enum·DTO)과 U8(레포지토리)을 나눠서 한다
  - `.claude/skills/implement/SKILL.md`의 한 줄 수정은 마이그레이션과 무관하다. 커밋에서 뺀다

## 역할

### 사용자

- `kotlin-reference/`의 참고 파일을 원본 `.java`와 두 탭으로 띄워 놓고, `src/main/kotlin/`의 같은 경로에 따라 친다
- 원본 `.java`는 지우지 않는다 (둘 다 있는 동안 IDE의 중복 선언 에러는 정상)
- 개념 정리는 노션에서 직접 한다
- 다 치면 "UN 다 했어"라고 알리고, 모르는 건 그때그때 묻는다

### 클로드

- **단위 준비**
  1. 원본 Java와 호출부를 분석한다. 누가 부르는지, null이 들어올 수 있는지, `Optional`이나 record 접근자를 쓰는지 본다
  2. `kotlin-reference/`(gitignore, 패키지 경로 그대로)에 참고 파일을 만든다. 컨벤션과 `interop.md`를 따른다
  3. 문자열 값과 enum 항목을 원본과 대조한다. 도구 입력의 `\uXXXX`는 실제 문자로 바뀌어 저장될 수 있으니 바이트로 확인한다
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

- [ ] U9: CourseService, MemberService, CartService, RegistrationService
  - 레포지토리 반환 타입 `Optional<T>` → `T?` 전환은 호출부가 모두 Kotlin이 되는 시점에 한다
    - U9에서 바꿀 것: `CartRepository.findByMemberIdAndCourseId`, `RegistrationRepository.findByMemberIdAndCourseId`, `CourseRepository.findByIdWithSchedules` (호출부가 U9 서비스뿐이다)
    - U10에서 바꿀 것: `MemberRepository.findByStudentId`(AuthService), `AdminRepository.findByLoginId`(AdminAuthService)
    - `MemberRepository.findByEmail`은 메인 호출부가 없다. 테스트만 쓰는지 확인하고 정한다
  - 상속받은 `findById`는 인터페이스를 고칠 필요 없이 Kotlin 확장 함수 `findByIdOrNull`로 부른다
  - `CartService.validateCartLimit`의 매직넘버 10은 `MAX_CART_COUNT` 상수로 뺀다 (`kotlin/common.md` 예시와 같다)
  - `CartService`, `RegistrationService`의 `import static ExceptionCode.*`는 Kotlin에서 개별 import로 바꾼다
- [ ] U10: AuthService, AdminAuthService, AdminCourseService, SystemSemesterService

### 4. 컨트롤러 (끝나면 전체 테스트)

- [ ] U11: AuthController, CartController, MemberController + 각 Docs
- [ ] U12: CourseController, RegistrationController + 각 Docs
- [ ] U13: AdminAuthController, AdminCourseController, AdminSemesterController + 각 Docs

### 5. 그 외 (끝나면 전체 테스트)

- [ ] U14: global/exception (ExceptionCode, RestApiException, JwtAuthenticationException, JwtTokenExpiredException, JwtTokenInvalidException, JwtTokenMissingException, GlobalExceptionHandler, AsyncExceptionHandler, CacheExceptionHandler)
- [ ] U15: EnumValidation, ParamValidation, EnumValidator, ParamValidator, AdminEndpoint, WhitelistEndpoint, ApiPerformanceInterceptor, HttpLoggingFilter
- [ ] U16: global/config 11 (ArgumentResolverConfig, AsyncConfig, CorsConfig, DataSourceConfig, FilterChainConfig, InterceptorConfig, P6SpyConfig, P6SpySqlFormatter, RedisCacheConfig, SchedulingConfig, SwaggerConfig)
- [ ] U17: Auth, AdminAuth, AdminAuthenticationFilter, JwtAuthenticationFilter, JwtExceptionFilter, JwtProvider, MemberPasswordEncoder, AdminAuthArgumentResolver, AuthArgumentResolver
- [ ] U18: CourseCacheLoader, CourseCacheWarmer, CourseScheduleFormatter, CourseValidator, SearchKeywordSanitizer, RegistrationTypeResolver, AdminPasswordEncoder, UssServerApplication

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
- U6, U7 작성본에서 `@JvmRecord` 5개 누락, 와일드카드 import(`EnumType.*`, `GenerationType.*`), 파일 끝 개행 없음을 정리했다. `SystemSemesterRequest`의 `@Min`/`@Max`를 2000/2100으로 바꾼 것은 의미가 있는 변경이라 그대로 두고 사용자에게 확인을 요청했다 (2026-09-16)
- U4는 사용자 요청으로 `@JvmRecord` 누락 3개, trailing comma 4곳, `) {`·`companion object {` 공백만 고쳤다. `CourseResponse`의 `from`/`of` 순서는 작성본 그대로 뒀다
- DTO 패키지는 `dto/common` 대신 `dto/internal`, 클래스 이름은 `~Dto`로 한다 (2026-09-16, 사용자 결정). Kotlin으로 옮기는 단위에서 바꾸고 Java에 남은 파일은 그대로 둔다. 문서는 `kotlin/dto.md`, `java/dto.md`, `kotlin/repository.md`, `project-structure.md`에 반영했다
- 사용자가 물어본 Kotlin 문법은 루트 `NOTE.md`(gitignore)에 모은다 (2026-09-15)
