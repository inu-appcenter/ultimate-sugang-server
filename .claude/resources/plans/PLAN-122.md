# [PLAN-122] 원본 실측 기준 학과 조회와 신청 검증 반영

> 이슈: #122
> 브랜치: feat/122-course-department-lookup

## 목표
타학과 조회를 학생 소속 단위에서 개설학과 단위로 바꾸고, 학과와 연계전공 목록을 적재 여부와 무관한 전건으로 만들며, 실측 목록에 있으나 서버에 없는 24건을 등록한다. 함께 신청 검증에서 같은 강의 재신청 검사를 원본 순서에 맞게 뒤로 옮긴다.

## 사전 확인 (코드 대조 결과)

- 실측 76건 중 **75건이 이미 `CourseDepartment`에 코드까지 등록**돼 있다. 없는 것은 `HUSS(교류대학)` 하나다
- 숫자가 정확히 맞는다. 89 − 연계전공 11 − 비학과 4 − `무역학부` 1 + HUSS 2 + `HUSS(교류대학)` 1 = **76**
- 연계전공은 11 − HUSS 2 + 신설 23 = **32**
- **연계전공 판정이 `this == A || this == B ...` 11개 체인이다.** 23건을 더하면 34개가 되고, `HUSS(타대학)`, `HUSS포용사회이니셔티브학부`를 학과로 옮기는 것도 이 체인을 손대는 일이다. 판정 방식을 먼저 바꾼다
- 단과대학으로 갈래를 파생하는 방법은 쓸 수 없다. HUSS 두 건은 학과로 옮겨야 하는데 단과대학이 `ETC`이고, 이 값은 연계 API 원문이라 바꾸면 동기화가 깨진다
- 프론트는 목록을 **이름에서 코드로 바꾸는 매핑표로만** 쓰고 표시 순서는 자체 목록을 따른다. 따라서 **enum 선언 순서는 손대지 않아도 되고, `name`이 실측 표기와 글자 단위로 같아야 한다**
- `무역학부`(`TRADE`)는 코드가 비어 강의가 붙을 수 없고 실측 드롭다운에도 없다. 다만 `CourseDepartmentTest`가 폐지 학과 예시로 쓰고 있어 지우지 않고 목록에서만 뺀다
- `ownedBy`는 전공 조회, 학생 기준 이수구분, 캐시 로더와 워머가 함께 쓴다. 이번에 바뀌는 것은 **타학과 조회 한 곳뿐**이다

## 영향 범위

### 신규 파일
- `src/main/java/uss/code/course/domain/CourseDepartmentKind.java` — 개설학과가 어느 목록에 속하는지 나타내는 갈래

### 수정 파일
- `src/main/java/uss/code/course/domain/CourseDepartment.java` — `kind` 필드 추가, 연계전공 23건과 `HUSS(교류대학)` 신설, HUSS 2건을 학과로 이동, 판정 메서드 교체
- `src/main/java/uss/code/course/service/CourseService.java` — 타학과 조회를 개설학과 기준으로, 학과와 연계전공 목록을 전건으로
- `src/main/java/uss/code/course/dto/response/DepartmentResponse.java` — `MemberDepartment` 대신 `CourseDepartment`를 받는다
- `src/main/java/uss/code/course/controller/CourseControllerDocs.java` — 파라미터 의미와 실패 코드 갱신
- `src/main/java/uss/code/global/exception/domain/ExceptionCode.java` — `CRS-008` 추가
- `src/main/java/uss/code/registration/service/RegistrationService.java` — 같은 강의 재신청 검사를 동일 과목명 뒤로 이동

### 정책 문서
- `.claude/spec/service-policy/course.md` — 타학과 조회 기준, 필터 목록 전건화, 개설학과 갈래
- `.claude/spec/service-policy/registration.md` — 검증 순서와 근거 문단 정정

### 테스트
- `src/test/java/uss/code/course/domain/CourseDepartmentTest.java` — 갈래 판정 테스트 추가, 연계전공 판정 테스트 갱신
- `src/test/java/uss/code/course/service/CourseServiceTest.java` — 타학과 조회와 목록 조회 재작성
- `src/test/java/uss/code/registration/service/RegistrationServiceTest.java` — 검증 순서 테스트 갱신

## 구현 계획

### 1. Domain

`CourseDepartmentKind` 신규
```java
public enum CourseDepartmentKind {
    DEPARTMENT,         // 타학과 조회 목록에 나오는 개설학과
    INTERDISCIPLINARY,  // 연계전공 조회 목록
    NON_MAJOR,          // 교양, 교직, 일선, 군사학
    LEGACY              // 폐지 단위. 목록에는 없지만 소속 매핑에는 남는다
}
```

`CourseDepartment`
- 필드 `private final CourseDepartmentKind kind` 추가
- 생성자 셋으로 나눈다. 학생 소속이 있는 학과는 지금 형태를 그대로 두고 `DEPARTMENT`로 채운다
  - `(code, college, name, MemberDepartment owner)` → `kind = DEPARTMENT`
  - `(code, college, name, CourseDepartmentKind kind)` → `owner = null`
  - `(code, college, name)` → `owner = null`, `kind = DEPARTMENT` (소속 없는 개설학과, HUSS 3건)
  - 두 4-인자 생성자는 네 번째 인자 타입이 달라 오버로드로 공존한다. **소속이 있는 74줄은 손대지 않는다**
- `TRADE`만 `(code, college, name, LEGACY)` 형태로 바꾼다. 소속(`GLOBAL_TRADE_SERVICE`)이 사라지면 `ownedBy` 테스트가 깨지므로 소속을 유지하는 5-인자 생성자를 하나 더 둔다
- 교양, 교직, 일선, 군사학 4건 → `NON_MAJOR`
- 기존 연계전공 11건 중 9건 → `INTERDISCIPLINARY`
- `HUSS_INCLUSIVE_SOCIETY_INITIATIVE`, `HUSS_OTHER_UNIVERSITY` → `DEPARTMENT`
- `HUSS_EXCHANGE_UNIVERSITY("", CourseCollege.ETC, "HUSS(교류대학)")` 신설
- 연계전공 23건 신설. 코드는 공백, 단과대학은 `ETC`, `kind = INTERDISCIPLINARY`
- 메서드
  - `public static List<CourseDepartment> departmentValues()` — `kind == DEPARTMENT` 전건
  - `public static List<CourseDepartment> interdisciplinaryValues()` — `kind == INTERDISCIPLINARY` 전건으로 교체
  - `public static CourseDepartment fromDepartment(final String department)` — `from` 후 `kind != DEPARTMENT`면 `INVALID_DEPARTMENT`
  - `fromInterdisciplinary`는 유지하되 `kind` 기준으로 판정
  - `isInterdisciplinary()`의 `==` 체인 제거

신설 23건의 상수명은 아래로 한다. 이름은 실측 표기를 그대로 옮긴다 (가운뎃점 U+00B7, 쉼표 포함 항목 2건).

| 상수 | 이름 |
|---|---|
| `INU_LIBERAL_ARTS` | INU리버럴아츠연계전공 |
| `MICE_SPORTS_TOURISM` | MICE,스포츠및관광연계전공 |
| `PERFORMING_VISUAL_ARTS` | 공연예술과시각예술연계전공 |
| `PUBLIC_HEALTH` | 공중보건연계전공 |
| `INTERNATIONAL_BUSINESS_TAX` | 국제비즈니스및세무연계전공 |
| `GLOBAL_ENTREPRENEURSHIP` | 글로벌기업가정신연계전공 |
| `CLIMATE_ENERGY_ENVIRONMENT` | 기후,에너지및환경연계전공 |
| `GREEN_CLIMATE` | 녹색기후연계전공 |
| `GREEN_CITY` | 녹색도시연계전공 |
| `NORTHEAST_ASIAN_STUDIES` | 동북아지역학전공(연계) |
| `FUTURE_CITY` | 미래도시연계전공 |
| `BIO_CONVERGENCE_STARTUP` | 바이오융합·창업연계전공 |
| `BEAUTY_INDUSTRY` | 뷰티산업연계전공 |
| `RENEWABLE_ENERGY` | 신재생에너지연계전공 |
| `EUROPEAN_TRADE` | 유럽통상학전공(연계) |
| `GENOMICS` | 유전체학연계전공 |
| `AI_STARTUP` | 인공지능·창업연계전공 |
| `AI_SOFTWARE` | 인공지능소프트웨어연계전공 |
| `STEM_CELL_TISSUE_ENGINEERING` | 줄기세포및조직공학연계전공 |
| `CHINA_STUDIES` | 중국연구연계전공 |
| `CHINESE_REGIONAL_STUDIES` | 중국지역학전공(연계) |
| `INTELLIGENT_ROBOT` | 지능로봇연계전공 |
| `ANTIBODY_ENGINEERING` | 항체공학연계전공 |

### 2. 전역
`ExceptionCode`에 `INVALID_DEPARTMENT(BAD_REQUEST, "CRS-008", "유효하지 않은 학과예요.")` 추가

### 3. DTO
`DepartmentResponse.from(final CourseDepartment department)` — `code`는 `department.name()`, `name`은 `department.getName()`. `InterdisciplinaryMajorResponse`와 같은 형태를 유지한다

### 4. Service
- `CourseService.getOtherDepartmentCourses(final String department)`
  1. `CourseDepartment.fromDepartment(department)`
  2. `courseRepository.findByDepartment(courseDepartment)`
  3. `CourseResponse`로 조립
  - `ownedBy` 확장이 사라진다. 학부를 넘겨도 하위 전공 강의가 딸려오지 않는다
  - 소속이 없어 빈 목록을 돌려주던 분기도 사라진다. 없는 학과는 `CRS-008`로 실패한다
- `CourseService.getDepartments()` — `CourseDepartment.departmentValues()`를 그대로 응답으로. 적재 여부 필터와 `findDepartmentsIn` 호출을 제거한다
- `CourseService.getInterdisciplinaryMajors()` — `interdisciplinaryValues()`를 그대로 응답으로. 적재 여부 필터를 제거한다
- `CourseRepository.findDepartmentsIn`은 두 곳에서만 쓰이므로 함께 제거한다

### 5. Registration 검증 순서
`RegistrationService.registerCourse`의 호출 순서를 아래로 바꾼다. 옮기는 것은 `validateDuplicateCourse` 한 줄이다.

```
1. 회원 존재            MEM-001
2. 강의 존재            CRS-003
3. 폐강                CRS-004
4. 시간표 중복          CRS-005
5. 동일 과목명          REG-006
6. 같은 강의 재신청      REG-003   <- 4, 5 뒤로 이동
7. 학점 상한            REG-002
8. 과목 유형 제한        CRS-006
9. 정원                REG-001
```

`REG-003`은 사실상 도달 불가가 된다. 같은 강의는 시간표가 있으면 4에서, 없으면 국문 강의명이 같아 5에서 걸린다. 그래도 검사는 남긴다. 지우면 `(member_id, course_id)` unique 제약 위반이 500으로 새어 나간다.

### 6. Controller
경로와 파라미터 이름은 그대로다. `CourseControllerDocs`의 타학과 조회 설명을 개설학과 기준으로 고치고 실패 코드를 `GLB-002`에서 `CRS-008`로 바꾼다. 학과 목록 조회 설명에서 "과목이 적재된"을 뺀다.

## 결정 필요 (Decisions needed)
- [x] 연계전공 판정을 `kind` 필드로 바꾼다 — `==` 체인은 34개가 되고 HUSS 이동도 같은 자리를 손대야 한다. 단과대학 파생은 HUSS의 단과대학이 연계 API 원문이라 쓸 수 없다
- [x] `무역학부`는 지우지 않고 `LEGACY`로 둔다 — 실측 목록에 없어 조회 목록에서는 빼되, 폐지 학과 매핑 테스트가 참조하고 있다
- [x] 신설 24건의 학사 코드는 공백으로 둔다 — 코드는 연계 API 원문이어야 하고 서버가 지어낼 수 없다. 목록과 조회는 상수명으로 이뤄지므로 공백이어도 동작한다

## 검증
- `CourseDepartmentTest` — 갈래별 전건 수(학과 76, 연계전공 32), `fromDepartment`가 연계전공과 비학과를 거부하는지, `HUSS(타대학)`과 `HUSS포용사회이니셔티브학부`가 학과로 잡히는지, 신설 항목이 이름으로 찾아지는지, 실측 표기와 글자 단위로 같은지(쉼표 2건, 가운뎃점 U+00B7 2건, `(야)` 2건)
- `CourseServiceTest` — 타학과 조회가 학부 하위 전공을 포함하지 않는지, 연계전공과 교양을 넘기면 `CRS-008`인지, 학과 목록이 강의 없는 학과까지 전건인지, 연계전공 목록이 32건인지
- `RegistrationServiceTest` — 같은 강의 재신청이 시간표 중복(`CRS-005`)으로 잡히는지, 시간표 없는 같은 강의는 동일 과목명(`REG-006`)으로 잡히는지, 기존 순서 테스트 갱신
- 전체: `.claude/CLAUDE.md`의 macOS 전체 실행 명령

## Deviation Log
- `CourseDepartment`: `@RequiredArgsConstructor`를 걷어내고 생성자 넷을 직접 선언 — 이유: 롬복이 final 필드 전체로 생성자를 만들어서 `kind`를 더하면 5-인자가 되고 89줄을 전부 고쳐야 했다. 직접 선언하면 소속이 있는 74줄을 그대로 둘 수 있다
- `CourseDepartment`: 연계전공 블록을 실측 목록과 같은 순서로 다시 나열 — 이유: 프론트는 순서를 쓰지 않지만, 32건을 실측 목록과 같은 차례로 두면 빠진 항목을 눈으로 대조할 수 있다
- `CourseServiceTest`: `타학과로_학부를_조회해도_하위_전공_과목까지_함께_조회된다`를 반대 동작 검증으로 바꾸고, 전공 조회는 여전히 하위 전공을 포함한다는 테스트를 옆에 추가 — 이유: 두 조회의 기준이 달라졌다는 사실 자체가 회귀 대상이라 한 자리에서 대비되게 뒀다
- `CourseRepository.findDepartmentsIn` 제거 — 이유: 목록 전건화로 호출부 두 곳이 모두 사라져 쓰이지 않는 쿼리가 됐다
