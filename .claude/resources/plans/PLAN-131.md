# [PLAN-131] refactor: Kotlin 전환 뒤 남은 코드 스멜 정리

> 이슈: #131
> 브랜치: refactor/131-code-smells

## 목표
PR #129의 SonarQube 측정에서 나온 Kotlin Code Smell 7건(규칙 4가지)을 항목마다 고칠지, 이 프로젝트의 선택이라 둘지 정하고 반영한다.
동작은 바뀌지 않아야 한다.

## 영향 범위
### 신규 파일
- 없음

### 수정 파일
- `src/main/kotlin/uss/code/course/infra/CourseValidator.kt` — 지역 변수 2개를 `List<CourseSchedule>`로 받는다 (결정 2)
- `src/main/kotlin/uss/code/auth/infra/JwtProvider.kt` — claim 조회 2곳을 인덱스 접근으로 바꾼다 (결정 3)
- `.claude/rules/code-convention/kotlin/domain.md`, `.claude/rules/code-convention/kotlin/controller.md` — 두기로 한 항목의 근거를 규칙으로 남긴다 (결정 5)

그대로 두는 파일: `Course.kt`, `Member.kt`, `AdminCourseControllerDocs.kt` (결정 1, 4). 팩토리 시그니처가 바뀌지 않으므로 `CourseFixture`, `MemberFixture`, `AuthService`도 그대로다.
서비스 정책 변화 없음.

## 구현 계획

1. **`kotlin:S107` 파라미터 과다** (`Course.create` 25개, `Member.create` 8개): 그대로 둔다 (결정 1)
   - `Course.create`의 파라미터 25개는 영속 필드 25개와 1:1이다. 코드와 이름 쌍(`classificationCode`/`classificationName` 등)은 원천 데이터 그대로 비정규화해 저장한다
   - 호출은 컨벤션상 이름 붙인 인자로 한 줄에 하나씩이라, 규칙이 막으려는 위치 인자 혼동이 생기지 않는다
   - #127 이후 main에는 호출부가 없고 `CourseFixture`만 부른다
   - `Member.create`는 기본값(7개)을 하나 넘는다. 호출부는 `AuthService.signUp` 하나이고 `SignUpRequest` 필드를 이름 붙인 인자로 옮긴다. 요청 DTO를 넘기면 도메인이 DTO에 의존하게 된다
2. **`kotlin:S6524` 바꾸지 않는 컬렉션을 변경 가능 타입으로 받음**: `CourseValidator.validateCourseScheduleNotConflict(existingCourses: List<Course>, newCourse: Course): Boolean` (결정 2)
   - `val newCourseSchedules = newCourse.schedules` → `val newCourseSchedules: List<CourseSchedule> = newCourse.schedules`
   - `val existingCourseSchedules = existingCourse.schedules` → `val existingCourseSchedules: List<CourseSchedule> = existingCourse.schedules`
   - `import uss.code.course.domain.CourseSchedule` 추가
   - 엔티티의 `schedules`를 읽기 전용으로 노출하는 안은 `domain.md`의 "컬렉션 연관관계는 `MutableList`로 선언" 규칙과 맞지 않아 택하지 않는다
3. **`kotlin:S6518` `get` 대신 인덱스 접근**: `JwtProvider.extractRole(accessToken: String?): String?` (결정 3)
   - `parseJwt(accessToken).payload.get(ROLE_CLAIM, String::class.java)` → `parseJwt(accessToken).payload[ROLE_CLAIM, String::class.java]`
   - `e.claims.get(ROLE_CLAIM, String::class.java)` → `e.claims[ROLE_CLAIM, String::class.java]`
   - 같은 Java 메서드 `Claims.get(String, Class<T>)`를 연산자로 부른다. `payload[ROLE_CLAIM]`(`Map.get`)은 타입 검사가 빠져 다른 동작이라 쓰지 않는다
4. **`kotlin:S6517` 함수형 인터페이스 권장**: `AdminCourseControllerDocs` 그대로 둔다 (결정 4)
   - `fun interface`는 람다로 구현하라는 선언인데, Docs 인터페이스는 컨트롤러 클래스가 구현하고 springdoc이 어노테이션을 읽는 용도다
   - Docs 인터페이스 8개 중 함수가 하나라는 이유로 이것만 달라지고, 엔드포인트가 늘면 다시 `fun`을 지워야 한다
5. **컨벤션** (결정 5)
   - `domain.md`의 `## Entity 클래스`: "`create()`는 영속 필드를 개별 파라미터로 받는다. 파라미터 수가 많아도 파라미터 객체로 묶지 않는다 (호출은 이름 붙인 인자)"
   - `controller.md`의 `## Docs 인터페이스`: "Docs 인터페이스는 함수가 하나여도 `fun interface`로 선언하지 않는다"
6. **SonarQube 재측정**: 이 PR에서는 하지 않는다. 사용자가 원할 때 #132, #133과 이 PR이 머지된 `dev` 기준으로 잰다

## 결정 필요 (Decisions needed)
- [x] 1. `S107` `Course.create`, `Member.create` — **그대로 둠** / 값 객체(`@Embeddable`)로 묶음 / 파라미터 객체 도입
  - 근거: 구현 계획 1. 값 객체는 엔티티 모델과 JPQL 경로를 바꾸는 모델 변경이고 #127 결정을 뒤집는다. 파라미터 객체는 같은 25개를 다른 클래스로 옮길 뿐이다
- [x] 2. `S6524` `CourseValidator` — **지역 변수 타입을 `List`로 고침** / 둠
- [x] 3. `S6518` `JwtProvider` — **인덱스 접근으로 고침** / 둠
- [x] 4. `S6517` `AdminCourseControllerDocs` — **둠** / `fun interface`로 고침
  - 근거: 구현 계획 4
- [x] 5. 두기로 한 항목을 컨벤션에 남길지 — **남김** / PR 본문에만 남김
  - 근거: SonarQube를 다시 재면 같은 경고가 다시 뜬다. 결정을 컨벤션에 두어 고칠 대상으로 오해하지 않게 한다

## 검증
- 동작 변화 없음: 변경 전(`origin/dev`)과 후의 `CourseValidator`, `JwtProvider` 클래스를 `javap -c -p`로 풀어 바이트코드 명령이 같은지 비교한다
- 전체 테스트: `./gradlew test`를 백그라운드로, Docker를 켠 상태에서. 기준 304개, 실패 0 (#132, #133이 머지되기 전의 `dev` 기준). iCloud 때문에 클래스 파일이 잘리면 `cleanCompileTestKotlin test --rerun`

## Deviation Log
- `.claude/rules/code-convention/kotlin/domain.md`: "영속 필드를 개별 파라미터로"를 "값을 개별 파라미터로"로 쓰고, 두 규칙 끝에 SonarQube 규칙 번호를 괄호로 달았다 — 이유: `Member.create`는 `college`를 받지 않고 `department`에서 끌어와 "영속 필드"가 사실과 다르다. 규칙 번호는 다음 측정에서 같은 경고를 볼 때 이 결정을 찾게 하려는 것
