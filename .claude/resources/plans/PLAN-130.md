# [PLAN-130] test: 빈 목록에서도 통과하는 테스트 단언 보강

> 이슈: #130
> 브랜치: test/130-vacuous-assertions

## 목표
`doesNotContain`, `allSatisfy`는 대상 목록이 비면 아무것도 검사하지 않고 통과한다. 조회 결과가 버그로 비어도 실패하도록 11개 테스트의 단언을 보강한다.
테스트 이름이 말하는 의도(무엇이 없어야 한다, 모든 항목이 무엇을 담는다)는 그대로 둔다.

## 영향 범위
### 신규 파일
- 없음

### 수정 파일
- `src/test/kotlin/uss/code/course/service/CourseServiceTest.kt` — `doesNotContain` 10건 앞에 비어 있지 않음 단언 추가
- `src/test/kotlin/uss/code/registration/service/RegistrationServiceTest.kt` — `allSatisfy` 1건 앞에 비어 있지 않음 단언 추가
- `.claude/spec/test-convention.md` — `## 검증`에 규칙 한 줄 추가 (결정 2)
- `src/test/kotlin/uss/code/course/domain/CourseDepartmentTest.kt` — 이슈에 없던 같은 결함 1건 (결정 3)

main 코드, 픽스처, 서비스 정책 변화 없음.

## 구현 계획

1. **보강 방식**: 부정 단언 바로 앞에 `.isNotEmpty()`를 넣는다. 추출(`extracting`) 뒤, 부정 단언 앞이다

   ```kotlin
   assertThat(response.courseResponses)
       .extracting<String> { it.courseCode }
       .isNotEmpty()
       .doesNotContain("MATH101")
   ```

2. **`CourseServiceTest`** (10건, 이름은 `{중첩 클래스}.{메서드}`)
   - `전공_과목_조회_테스트.다른_학과_과목은_조회되지_않는다` (`doesNotContain("MATH101")`)
   - `학부_소속_전공_과목_조회_테스트.다른_학부의_과목은_조회되지_않는다` (`doesNotContain("LIF101")`)
   - `교양_과목_조회_테스트.전공_과목은_교양_조회시_포함되지_않는다` (`doesNotContain("COM101")`)
   - `타학과_전공과목_조회_테스트.다른_학과_과목은_조회되지_않는다` (`doesNotContain("CSE101")`)
   - `학제간융합전공_과목_조회_테스트.다른_연계전공_과목은_조회되지_않는다` (`doesNotContain("FA101")`)
   - `학제간융합전공_과목_조회_테스트.일반_학과_과목은_조회되지_않는다` (`doesNotContain("CSE101")`)
   - `연계전공_조회_테스트.연계전공이_아닌_학과는_목록에_섞이지_않는다` (`doesNotContain("수학과", "교양", "HUSS(타대학)")`)
   - `학과_목록_조회_테스트.교양과_교직과_일선과_군사학은_학과_목록에_없다`
   - `학과_목록_조회_테스트.연계전공은_학과_목록에_없다`
   - `학과_목록_조회_테스트.폐지된_학과는_학과_목록에_없다`
   - 손대지 않는 2건: `타학과로_학부를_조회하면_학부가_개설한_과목만_조회된다`, `학부를_넘겨도_하위_전공_과목은_함께_조회되지_않는다`는 앞에 `containsExactly("ELE101")`가 있어 빈 목록에서 이미 실패한다
3. **`RegistrationServiceTest`** (1건)
   - `학생_기준_이수구분_테스트.학번과_재수강_구분과_신청_시각이_함께_담긴다`: `.allSatisfy { ... }` 앞에 `.isNotEmpty()`
4. **컨벤션**: `test-convention.md`의 `## 검증`에 추가
   - `doesNotContain`, `allSatisfy`처럼 대상이 비면 그대로 통과하는 단언은 앞에 `isNotEmpty()`(또는 `hasSize`, `contains`)를 둔다. Kotlin 분석기에는 이를 잡는 SonarQube 규칙(`java:S5841`)이 없다
5. **이슈에 없던 1건**: `CourseDepartmentTest`의 `폐지된_학과는_목록에_없지만_소속_매핑에는_남는다`
   - `assertThat(CourseDepartment.departmentValues()).doesNotContain(CourseDepartment.TRADE)`에 `.isNotEmpty()`를 넣는다. Java 버전(`0d71376`)에도 같은 단언이 있었지만 SonarQube가 잡지 않았다

## 결정 필요 (Decisions needed)
- [x] 1. 보강 방식 — **A: `isNotEmpty()`** / B: setUp이 넣은 기대 항목을 `contains(...)`로 함께 단언 / C: `hasSize(n)`
  - 근거: 테스트가 여전히 "없어야 한다" 하나만 주장해 이름과 맞는다. 내용은 같은 중첩 클래스의 긍정 테스트(`hasSize`, `containsExactlyInAnyOrder`, `findByCourseCode`)가 이미 검증해 중복이 없다. B와 C는 긍정 테스트와 겹치고 setUp 데이터에 묶인다
- [x] 2. 규칙을 `test-convention.md`에 남길지 — **남김** / 남기지 않음
  - 근거: Kotlin 분석기에는 `java:S5841`이 없어 새 테스트에서 같은 결함을 막을 장치가 컨벤션뿐이다
- [x] 3. 이슈에 없는 `CourseDepartmentTest` 1건 — **포함** / 제외
  - 근거: 같은 결함, 같은 수정이다. 규칙을 남기면서 이 건을 두면 규칙을 어긴 채 남는다

## 검증
- 빈 목록에서 실패하는지: 스크래치 복사본에서 목록 응답의 팩토리(`CoursesResponse.of`, `DepartmentsResponse.of`, `InterdisciplinaryMajorsResponse.of`, `RegistrationCoursesResponse.of`)가 빈 목록을 돌려주게 바꾸고, 대상 테스트를 보강 전(`origin/dev`)과 보강 후로 각각 돌린다. 보강 전에는 통과, 보강 후에는 실패해야 한다 (`CourseDepartment.departmentValues()`도 빈 목록을 돌려주게 해 `CourseDepartmentTest` 1건을 같은 방식으로 본다)
- 테스트 개수와 이름: 전체 실행 결과 XML의 (클래스, 테스트 이름) 집합이 `origin/dev`와 같다
- 전체 테스트: `./gradlew test`를 백그라운드로, Docker를 켠 상태에서. 기준 304개, 실패 0 (#132가 머지되기 전의 `dev` 기준)

## Deviation Log
