# [PLAN-120] 프론트엔드 명세 기준 API 계약 정합

> 이슈: #120
> 브랜치: feat/120-api-contract-alignment

## 목표
프론트엔드가 화면에서 역으로 도출한 API 명세에 서버 응답을 맞춘다. 조회 종류마다 갈라진 강의 응답 DTO 5종을 하나로 통합하고, 신청 검증 순서를 재정의하며, 교양 조회를 이수구분 코드 기반으로 바꾸고 학과 목록 API를 신설한다.

## 사전 확인 (코드 대조 결과)

- 프론트가 요구한 `Course` 17필드 중 서버에 원천이 없는 것은 **교강사 하나뿐**이다. `courses` 테이블에도 연계 API 원천에도 없어 빈 문자열로 고정한다
- `professor`를 뺀 나머지는 전부 기존 데이터로 만들 수 있다. `capacity`/`enrolled`는 `max_capacity`/`current_enrollment`, `tags`는 `is75MinLesson` + `type_name`, `isNight`는 학과명 `(야)` 접미, `isClosed`는 `isActive() && isRegisterable()`의 부정이다
- 프론트가 보내는 교양 이수구분 코드(`11`, `21`, `23`, `50`, `70`, `80`)와 이수영역 코드(`161`~`186`)는 **서버 `CourseClassification`, `CourseArea`의 코드와 이미 일치**한다. 코드 체계를 새로 만들 필요가 없고 파라미터만 코드 기반으로 바꾸면 된다
- 시간표 교시 명칭(`1-2A`, `2B-3`, `5B-6`, `야1`)은 프론트 문법과 그대로 맞는다. 바꿀 것은 조립 표기뿐이다
- 반교시 충돌 판정은 서버가 이미 `start_time`/`end_time` 실시각으로 비교하므로 프론트 요구(`1-2A`와 `2B-3`은 비충돌)를 이미 만족한다. **손대지 않는다**
- `RedisCacheConfig.cacheFlusher`가 기동 시 전 캐시를 비우므로, 캐시 값 record 구조가 바뀌어도 역직렬화 충돌은 나지 않는다

## 영향 범위

### 신규 파일
- `src/main/java/uss/code/course/dto/response/CourseResponse.java` — 통합 강의 응답. 모든 강의 목록과 신청 내역, 신청 성공 응답이 이 하나를 쓴다
- `src/main/java/uss/code/course/dto/common/CachedCourse.java` — 캐시에 담는 강의 record. `CachedMajorCourse`와 `CachedGeneralEducationCourse`를 대체
- `src/main/java/uss/code/course/dto/common/CachedCourses.java` — 위의 목록 래퍼
- `src/main/java/uss/code/course/dto/response/CoursesResponse.java` — 통합 강의 목록 래퍼(`courseResponses`). 강의 목록을 반환하는 모든 조회가 이 하나를 쓴다
- `src/main/java/uss/code/course/dto/response/DepartmentResponse.java` — 학과 코드와 이름
- `src/main/java/uss/code/course/dto/response/DepartmentsResponse.java` — 학과 목록 래퍼
- `src/main/java/uss/code/course/infra/SearchKeywordSanitizer.java` — FULLTEXT 불리언 연산자 이스케이프
- `src/main/java/uss/code/registration/infra/RegistrationTypeResolver.java` — 학생 기준 이수구분 산출
- `src/main/java/uss/code/registration/dto/response/RegistrationResponse.java` — 신청 성공 응답(`courseResponse` 하나)
- `src/main/resources/database/migration/V1_14__add_classification_index_to_courses.sql` — 이수구분 조회 인덱스

### 수정 파일
- `src/main/java/uss/code/course/controller/CourseController.java` — 교양 조회 파라미터 교체, 학과 목록 엔드포인트 추가, 반환 타입 교체
- `src/main/java/uss/code/course/controller/CourseControllerDocs.java` — 위 변경에 맞춘 Swagger 문서
- `src/main/java/uss/code/course/service/CourseService.java` — 전 조회를 `CourseResponse`로 조립, 교양 조회 재작성, `getDepartments()` 추가
- `src/main/java/uss/code/course/infra/CourseCacheLoader.java` — 캐시 값을 `CachedCourses`로 단일화, 교양 캐시 키를 이수구분으로 교체
- `src/main/java/uss/code/course/infra/CourseCacheWarmer.java` — 교양 워밍 대상을 이수구분으로 교체
- `src/main/java/uss/code/course/infra/CourseScheduleFormatter.java` — 표기를 프론트 문법으로 교체, 시간표 없으면 빈 문자열
- `src/main/java/uss/code/course/repository/CourseRepository.java` — 이수구분 조회 추가, 정원 projection에 담긴 수 추가
- `src/main/java/uss/code/course/dto/common/CourseCapacity.java` — `cartCount` 추가
- `src/main/java/uss/code/course/domain/CourseClassification.java` — 교양 화면 대상 판별과 소속 이수영역 목록
- `src/main/java/uss/code/course/domain/CourseDepartment.java` — `isNight()` 추가
- `src/main/java/uss/code/course/domain/CourseType.java` — 태그 대상 판별
- `src/main/java/uss/code/cart/controller/CartController.java`, `CartControllerDocs.java`, `service/CartService.java` — 응답을 `CourseResponse`로 교체
- `src/main/java/uss/code/registration/controller/RegistrationController.java`, `RegistrationControllerDocs.java`, `service/RegistrationService.java` — 신청 내역 응답 재구성, 검증 순서 재정의, 신청 성공 응답 반환
- `src/main/java/uss/code/registration/dto/response/RegistrationCourseResponse.java` — 신청 내역 한 행(강의 객체 내장)
- `src/main/java/uss/code/member/dto/response/MemberProfileResponse.java` — `creditLimit`, `gpa` 추가
- `src/main/java/uss/code/global/exception/domain/ExceptionCode.java` — `CRS-007`, `REG-006` 추가
- `src/main/java/uss/code/global/config/CorsConfig.java` — `exposedHeaders`에 `Date`
- `src/main/java/uss/code/global/config/RedisCacheConfig.java` — 캐시 직렬화 타입 교체

### 삭제 파일
- `course/dto/response/MajorCourseResponse.java`, `GeneralEducationCourseResponse.java`, `InterdisciplinaryMajorCourseResponse.java`, `SearchedCourseResponse.java`
- `course/dto/common/CachedMajorCourse.java`, `CachedMajorCourses.java`, `CachedGeneralEducationCourse.java`, `CachedGeneralEducationCourses.java`
- `cart/dto/response/CartedCourseResponse.java`
- 목록 래퍼 5종 `course/dto/response/MajorCoursesResponse.java`, `GeneralEducationCoursesResponse.java`, `InterdisciplinaryMajorCoursesResponse.java`, `SearchedCoursesResponse.java`, `cart/dto/response/CartedCoursesResponse.java` — `CoursesResponse` 하나로 대체

### 정책 문서
- `.claude/spec/service-policy/course.md` — 시간표 표기, 교양 조회 범위 확대, 필터 목록에 학과 추가, 응답 필드
- `.claude/spec/service-policy/registration.md` — 검증 순서 정정과 재정의, 학생 기준 이수구분 신설
- `.claude/spec/service-policy/cart.md` — 조회 응답 변경
- `.claude/spec/service-policy/member.md` — 프로필 노출 항목 추가

## 구현 계획

### 1. Flyway
`V1_14__add_classification_index_to_courses.sql`
```sql
ALTER TABLE courses ADD INDEX idx_classification_sort (classification_code, grade_code, haksu_code);
```
교양 조회가 `area`가 아니라 `classification_code`로 바뀌면 기존 `idx_area_sort`가 안 먹는다. 등호 필터 `classification_code`를 선두에 두고, 뒤 두 컬럼이 `ORDER BY grade_code, classification_code, haksu_code`의 나머지와 순서가 같아 정렬을 인덱스로 대신한다.

### 2. Domain
- `CourseClassification`
  - `private static final List<String> LIBERAL_ARTS_SCREEN_CODES = List.of("11", "21", "23", "50", "70", "80")`
  - `public boolean isLiberalArtsScreen()` — 교양 화면에서 조회할 수 있는 이수구분인지
  - `public static CourseClassification fromLiberalArtsScreen(final String code)` — `fromCode` 후 위 검사, 실패 시 `INVALID_GENERAL_EDUCATION_CLASSIFICATION`
  - `public boolean hasArea(final CourseArea area)` — 이수영역이 이 이수구분 소속인지. 코드 접두(`11`→`16x`, `21`→`17x`, `23`→`18x`)가 아니라 명시 매핑으로 둔다
- `CourseDepartment.isNight()` — `name.endsWith("(야)")`. 상수 `NIGHT_NAME_SUFFIX`
- `CourseType`
  - `private static final Set<CourseType> TAG_TYPES = Set.of(E_LEARNING, E_LEARNING_HUSS, ONLINE_BLENDED, ONLINE_BLENDED_HUSS)`
  - `public static boolean isTagType(final String typeCode)`

### 3. Repository
- `CourseRepository.findByClassificationCode(@Param("classificationCode") final String classificationCode)` — `status = ACTIVE`, `ORDER BY c.gradeCode, c.classificationCode, c.haksuCode`
- `CourseRepository.findCapacitiesByClassificationCode(...)` — 위와 같은 조건의 `CourseCapacity` projection
- `findCapacitiesByDepartmentIn`, `findCapacitiesByClassificationCode`의 projection에 `c.cartCount` 추가
- `CourseCapacity(long id, int currentEnrollment, int maxCapacity, int cartCount)`
- 기존 `findByArea`, `findCapacitiesByArea`는 교양 조회가 이수구분 기준으로 바뀌면서 쓰이지 않으므로 제거

### 4. infra
- `CourseScheduleFormatter.format(final List<CourseSchedule> schedules)`
  - 시간표가 없으면 `""`
  - 요일, 시작 시각 순으로 정렬한 뒤 **(요일, 강의실) 연속 구간**으로 묶는다
  - 묶음 하나를 `{요일} {교시} {교시} ... ({강의실})`로 쓰고, 묶음 사이는 공백으로 잇는다
  - 예: 월 3교시(08-201), 월 4교시(08-201) → `월 3 4 (08-201)` / 화 1-2A(07-407), 목 2B-3(07-407) → `화 1-2A (07-407) 목 2B-3 (07-407)`
  - 상수: `DAY_PERIOD_DELIMITER = " "`, `CLASSROOM_PREFIX = " ("`, `CLASSROOM_SUFFIX = ")"`, `NO_SCHEDULE = ""`
- `SearchKeywordSanitizer.sanitize(final String keyword)`
  - FULLTEXT BOOLEAN MODE 연산자(`+ - > < ( ) ~ * " @`)를 제거한다. 제거 후 공백을 하나로 접고 trim
  - 제거가 아니라 이스케이프(따옴표 감싸기)를 택하지 않는 이유는, 따옴표로 감싸면 구문 검색이 되어 부분 일치가 깨지기 때문이다
- `RegistrationTypeResolver.resolve(final Member member, final Course course)`
  - `CourseDepartment.ownedBy(member.getDepartment())`에 `course.getDepartment()`가 없고, 강의 이수구분이 전공 계열(`전공기초`, `전공핵심`, `전공심화`)이면 `일반선택`
  - 그 외에는 `course.getClassificationName()` 그대로
  - 상수: `GENERAL_ELECTIVE_NAME`, 전공 계열 코드 집합

### 5. DTO
`CourseResponse` — 프론트 계약 필드 + 서버 고유 필드
```java
public record CourseResponse(
        String id,            // Course.id 문자열
        String code,          // haksuCode
        String courseCode,
        String name,          // titleKr
        String nameEn,        // titleEn
        String englishCourseName,  // 원어강의명, 아니면 ""
        String professor,     // "" 고정
        int credits,
        int capacity,
        int enrolled,
        int cartCount,        // 장바구니에 담긴 수
        String courseType,    // classificationName
        String courseArea,    // 교양 이수영역이면 areaName, 아니면 ""
        String department,
        String grade,         // gradeName
        String schedule,
        List<String> tags,
        boolean isEnglish,
        boolean isNight,
        boolean isClosed
) {
    public static CourseResponse from(final Course course);
    public static CourseResponse of(final CachedCourse course, final CourseCapacity capacity);
}
```
- `tags`: `is75MinLesson()`이면 `75분수업`을 먼저 넣고, `CourseType.isTagType(typeCode)`이면 `typeName`을 잇는다. 해당 없으면 빈 목록
- `courseArea`: `CourseArea.isGeneralEducationArea()`일 때만 `areaName`, 아니면 `""`
- `isClosed`: `!(course.isActive() && course.isRegisterable())`. 캐시 경로는 `!capacity.isRegisterable()`(조회가 이미 ACTIVE만 담는다)
- `englishCourseName`: 원어강의가 아니면 `null`이 아니라 `""`

`CachedCourse` — `CourseResponse`에서 정원, 현재 수강인원, 담긴 수, 마감 여부를 뺀 나머지. 이 넷은 캐시하지 않고 매 요청 `CourseCapacity`로 읽는다

`RegistrationCourseResponse` — 신청 내역 한 행
```java
public record RegistrationCourseResponse(
        String studentId,
        String resolvedType,
        String reAttendance,   // "" 고정
        String createdAt,      // ISO 8601
        CourseResponse courseResponse
)
```
`RegistrationResponse(CourseResponse courseResponse)` — 신청 성공 응답

`DepartmentResponse(String code, String name)` — `code`는 `MemberDepartment.name()`, `name`은 한글 학과명. `InterdisciplinaryMajorResponse`와 같은 형태로 맞춘다

`MemberProfileResponse`에 `int creditLimit`(`member.getMaxCredit()`), `double gpa`(`member.getLastSemesterGpa()`) 추가

### 6. Service
- `CourseService`
  - `getMajorCourses(long memberId)`, `getOtherDepartmentCourses(String department)`, `getInterdisciplinaryMajorCourses(String department)`, `searchCourses(String keyword)`, `getHussCourses()` — 조립만 `CourseResponse`로 교체
  - `getGeneralEducationCourses(final String classificationCode, final String areaCode)` 재작성
    1. `CourseClassification.fromLiberalArtsScreen(classificationCode)`
    2. `areaCode`가 있으면 `CourseArea.fromCode(areaCode)` 후 `classification.hasArea(area)` 검사, 실패 시 `INVALID_GENERAL_EDUCATION_AREA`
    3. `courseCacheLoader.loadGeneralEducationCourses(classification)`으로 이수구분 전건을 받는다
    4. `areaCode`가 있으면 메모리에서 `areaCode` 일치만 남긴다. 정렬 순서는 유지된다
    5. `findCapacitiesByClassificationCode`로 정원을 채운다
    - 하위 영역 전체 조회와 영역 지정 조회가 같은 캐시 한 벌을 쓰게 되어, 캐시 키가 이수영역 13개에서 이수구분 6개로 줄어든다
  - `getDepartments()` 신설 — `MemberDepartment.values()` 중 `CourseDepartment.ownedBy(...)`가 비어 있지 않고, 그 강의 학과가 실제로 적재된 것만. `getInterdisciplinaryMajors()`가 `findDepartmentsIn`으로 거르는 방식을 그대로 따른다
  - `searchCourses`는 `SearchKeywordSanitizer.sanitize` 결과로 조회한다. 정제 후 빈 문자열이면 조회하지 않고 빈 목록을 반환한다
- `CartService.getCartedCourse(long memberId)` — `CourseResponse.from(cart.getCourse())`로 교체
- `RegistrationService`
  - `getRegistrationCourse(long memberId)` — 회원을 한 번 조회해 `studentId`와 `resolvedType` 산출에 쓴다. 각 행을 `RegistrationCourseResponse`로 조립
  - `registerCourse(long memberId, long courseId)` — 검증 순서를 아래로 재정의하고 `RegistrationResponse`를 반환
    1. 회원 존재 (`MEMBER_NOT_FOUND`)
    2. 강의 존재 (`COURSE_NOT_FOUND`)
    3. 폐강 (`COURSE_CLOSED`)
    4. 동일 강의 이미 신청 (`COURSE_ALREADY_REGISTERED`)
    5. 시간표 중복 (`COURSE_SCHEDULE_CONFLICT`)
    6. 동일 과목명 이미 신청 (`DUPLICATE_SUBJECT_REGISTERED`) — 신규
    7. 학점 상한 (`CREDIT_LIMIT_EXCEEDED`)
    8. 과목 유형 제한 (`COURSE_TYPE_LIMIT_EXCEEDED`)
    9. 정원 (`COURSE_MAX_CAPACITY_EXCEEDED`, `increaseEnrollmentWithinCapacity`)
  - `validateDuplicateSubject(final List<Registration> registrations, final Course course)` 신규 — `registration.getCourse().getTitleKr().equals(course.getTitleKr())`이면 실패. 분반이 달라도 막는다
  - 정원 검사가 마지막에 있다는 사실을 `registration.md`에 반영한다. 현재 문서는 정원을 4번으로 적어 두어 코드와 어긋나 있다
- `MemberService.getProfile` — 응답 조립만 바뀐다

### 7. Controller
- `GET /api/v1/courses/general-education?classification-code={code}&area-code={code}` — `area-code`는 선택. `@ParamValidation(maxLength = 3)`
- `GET /api/v1/courses/departments` → `getDepartments()`
- `POST /api/v1/registration/{courseId}` → `ResponseEntity<RegistrationResponse>`
- 나머지는 반환 타입만 교체. `{Controller}Docs` 인터페이스도 같이 고친다

### 8. 전역
- `ExceptionCode`
  - `INVALID_GENERAL_EDUCATION_CLASSIFICATION(BAD_REQUEST, "CRS-007", "유효하지 않은 교양 이수구분이에요.")`
  - `DUPLICATE_SUBJECT_REGISTERED(BAD_REQUEST, "REG-006", "이미 같은 과목명을 신청했어요.")`
- `CorsConfig` — `configuration.setExposedHeaders(List.of("Date"))`. `Date`는 CORS 안전목록 응답 헤더가 아니라 명시하지 않으면 브라우저가 읽지 못한다
- `RedisCacheConfig` — 두 커스터마이저의 직렬화 타입을 `CachedCourses`로 교체

## 결정 필요 (Decisions needed)
- [x] 목록 응답 래핑 키 — **`CoursesResponse{courseResponses}` 하나로 통일한다.** 원소 타입이 `CourseResponse` 하나가 되면서 래퍼 5종을 유지할 이유가 사라졌다. 회신 결정 9(오퍼레이션별 키 유지)는 DTO 통합을 전제하지 않은 판단이었으므로 이 결정으로 대체하고, 최종 API 명세에 반영해 프론트에 전달한다
- [x] 담긴 수(`cartCount`)의 위치 — **`CourseResponse`에 넣어 모든 강의 조회에서 제공한다.** 캐시 경로는 `CourseCapacity` projection에 `cart_count`를 얹어 실시간으로 채운다. `cart.md`의 "장바구니 조회는 담긴 수를 함께 제공한다"가 "모든 강의 조회에서 제공한다"로 넓어지므로 정책 문서를 고친다
- [x] 동일 강의 재신청 검사(`REG-003`)의 위치 — **시간표 중복보다 앞에 유지한다.** `(member_id, course_id)` unique 제약이 이미 있고, 같은 강의를 두 번 신청한 것과 다른 강의와 시간이 겹치는 것은 사용자가 취할 행동이 다르다. 프론트는 `REG-003`을 에러 카탈로그에 추가한다

## 검증
- `CourseScheduleFormatterTest` — 표기 전면 교체. 시간표 없음은 하이픈이 아니라 빈 문자열, 같은 요일 다중 교시 묶음, 요일별 강의실이 다른 경우
- `CourseResponseTest` — `CourseResponse` 단일 스키마 기준으로 재작성. 식별자 문자열화, 원어강의명 빈 문자열, 태그 조립, 야간 학과, 마감 여부, 이수영역 빈 문자열
- `CourseServiceTest` — 교양 조회를 이수구분 코드 기준으로 재작성. 이수구분 단독 조회, 이수영역 지정 조회, 이수구분과 이수영역이 어긋나면 실패, 교직/군사학/일반선택 조회 성공, 전공 이수구분이면 실패. 학과 목록 조회 신규
- `CourseServiceSearchTest` — 검색어에 불리언 연산자가 섞여도 결과가 나오는지, 정제 후 빈 문자열이면 빈 목록인지
- `RegistrationServiceTest` — 검증 순서 신규 시나리오(시간표 중복이 학점 상한보다 먼저, 동일 과목명이 학점 상한보다 먼저), 동일 과목명 차단(분반이 다른 경우), 신청 성공 응답에 강의가 담기는지, 신청 내역의 학생 기준 이수구분(타 학과 전공과목이 일반선택으로 바뀌는지, 소속 학과 과목은 그대로인지)
- `CartServiceTest` — 응답 타입 교체분
- `MemberServiceTest` — 프로필에 최대 이수 학점과 직전 학기 성적이 담기는지
- 전체: `./gradlew test`

## Deviation Log
- `CourseClassification`: 교양 화면 대상을 코드 문자열 목록이 아니라 enum 상수 `EnumSet`으로 선언 — 이유: 코드 문자열을 enum 밖에서 한 번 더 적지 않아도 되고, 이수영역 매핑(`AREAS`)도 같은 방식으로 상수끼리 묶인다
- `CourseService.resolveArea()`: `fromCode` 후 `hasArea` 검사 대신 `tryFromCode`에 `filter(classification::hasArea)`를 걸어 없는 코드와 소속 아닌 코드를 모두 `CRS-001` 하나로 — 이유: 사용자 관점에서 둘 다 "유효하지 않은 교양 영역"이며, 없는 코드가 `GLB-002`로 새는 것보다 한 코드가 낫다
- `SearchKeywordSanitizer.sanitize()`: 연산자를 제거하지 않고 공백으로 치환 — 이유: 제거하면 `실습(Ⅴ-1)`이 `실습Ⅴ1`로 붙어 원문에 없는 ngram 토큰이 생긴다
- `RegistrationCourseResponse.createdAt`: `LocalDateTime.toString()` 대신 `yyyy-MM-dd'T'HH:mm:ss.SSS` 고정 패턴 — 이유: `toString()`은 초와 밀리초가 0이면 생략해 행마다 표기가 달라지고, 프론트의 사전순 정렬이 깨진다
- `RegistrationService.registerCourse()`: 성공 응답을 벌크 UPDATE 전에 읽어둔 엔티티로 조립하므로 `enrolled`는 증가 반영 전 값이고 `isClosed`도 그 시점 기준이다 — 이유: 반영값을 주려면 신청 핫패스에 SELECT 한 번이 추가돼 측정 지표가 바뀐다. 화면은 이 응답에서 강의명만 쓰고 내역을 다시 조회한다
