---
name: write-test
description: |
  테스트 코드를 작성하고 실행한다. 대상 없이 부르거나 테스트 클래스를 주면 실행만 한다.
  Trigger: "테스트 작성해줘", "테스트 코드 만들어줘", "XXXService 테스트", "테스트 추가해줘", "테스트 돌려줘", "테스트 실행해줘", "이 테스트 통과하는지 확인해줘", "빌드 돌려줘"
  Do NOT use for: 기존 테스트의 단순 수정(직접 Edit), 빌드 설정 변경
  Boundary: 테스트 대상(프로덕션) 코드의 버그는 고치지 않고 보고만 한다.
argument-hint: "[대상 클래스명 | 테스트 클래스명 | 비움]"
allowed-tools: Read, Grep, Glob, Edit, Write, Bash(./gradlew *), Bash(docker info*)
model: opus
effort: high
---

## 테스트 작성과 실행

대상: $ARGUMENTS

- 비어 있으면 전체 테스트를 실행만 한다(Phase 4로 바로 간다)
- 이미 있는 테스트 클래스명(`...Test`)이나 `클래스#메서드`면 그 테스트를 실행만 한다(Phase 4로 바로 간다)
- 그 밖에는 대상 클래스의 테스트를 작성하고 실행한다(Phase 1~5)

테스트는 통합 테스트(`@IntegrationTest`, H2)로 작성한다. MySQL 고유 동작(FULLTEXT, 락)을 봐야 할 때만 `@MySqlIntegrationTest`를 쓴다.

### Phase 1: 대상 분석

1. 대상 클래스를 Grep, Glob으로 찾아 Read한다
2. public 메서드와 각 메서드의 분기(조건, 예외)를 목록으로 만든다
3. 생성자 의존성을 확인한다

### Phase 2: Fixture 준비

1. `src/test/kotlin/uss/code/{domain}/fixture/`의 기존 Fixture를 Glob으로 찾아 재사용한다
2. 없으면 [template/test-code-template.md](template/test-code-template.md)의 Fixture 절을 따라 만든다

> Skip 조건: 필요한 Fixture가 모두 있거나 대상이 Entity를 쓰지 않을 때

### Phase 3: 테스트 작성

1. `.claude/spec/test-convention.md`와 [template/test-code-template.md](template/test-code-template.md)를 Read한다
2. Phase 1의 메서드마다 아래 케이스를 쓴다
   - 정상 동작
   - 실패(존재하지 않음, 권한 없음, 중복 등). `RestApiException`은 `exceptionCode`까지 검증한다(예: `.hasFieldOrPropertyWithValue("exceptionCode", {CODE})`). 타입만 보면 다른 코드로 회귀해도 통과한다. `ExceptionCode` 항목은 개별 import로 쓴다(`ExceptionCode.X` 표기 금지)
   - 경계값, 빈 목록, null (해당할 때만)
3. Phase 1의 분기 목록과 대조해 빠진 케이스가 없는지 확인한다

### Phase 4: 실행

1. 실행 환경: 기본은 H2라 따로 준비할 것이 없다. `@MySqlIntegrationTest`는 Docker가 꺼져 있으면 조용히 건너뛰므로, 대상에 포함되는데 `docker info`가 실패하면 Docker를 켜 달라고 알리고 멈춘다
2. 명령. 출력은 파일로 리다이렉트하고, 실패하면 필요한 구간만 읽는다
   - 클래스, 메서드: `./gradlew test --tests "{패키지}.{테스트 클래스}"` (`#메서드`는 `.{메서드}`로)
   - 전체: `./gradlew test`. 오래 걸리므로 Bash `run_in_background`로 돌린다
3. 결과를 보고하기 전에 건너뜀 수를 확인한다. 이번에 작성한 테스트가 건너뛰어졌으면 원인(Docker 등)을 해소하고 다시 실행한다
4. 실패 처리
   - 컴파일 오류: 오류 줄을 보고 고친다
   - 테스트 실패: 실패한 `클래스#메서드`와 첫 메시지로 원인을 판단한다. 스택이 더 필요하면 그 테스트 구간만 본다(`build/reports/tests/test`)
   - 원인이 이번에 작성한 테스트면 고쳐 다시 실행한다. 수정과 재실행은 최대 3회
   - 원인이 프로덕션 코드 버그면 원인과 수정 방안만 보고한다. 사용자 확인 없이 고치지 않는다
   - 실행만 하는 경우(이번에 작성한 테스트가 없음)에는 아무것도 고치지 않고 실패 목록, 원인, 수정 방안을 보고한다

> 다음 Phase 조건: 전부 통과했거나 재실행 상한에 도달했을 때

### Phase 5: 결과 보고

`template/output.md`를 Read해 채워 보고한다. 주석은 출력하지 않는다.
