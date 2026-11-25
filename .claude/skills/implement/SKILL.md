---
name: implement
description: |
  이슈의 Plan대로 기능을 구현한다. 로컬 계획서(PLAN-{번호})가 있으면 계획서를 기준으로 삼는다.
  Trigger: "구현해줘", "#{번호} 구현해줘", "PLAN-{번호} 구현해줘", "계획서대로 만들어줘", "이 작업 구현 시작"
  Do NOT use for: 계획 수립(→ write-plan), 이슈 발의(→ open-issue), 테스트 작성과 실행(→ write-test), PR 생성(→ open-pr)
  Boundary: 정해진 기준대로 코드를 구현한다. 테스트 코드는 쓰지 않는다.
argument-hint: "[이슈 번호]"
allowed-tools: Read, Grep, Glob, Edit, Write, AskUserQuestion, Bash
model: sonnet
effort: high
---

## 기능 구현

### 시작 조건

1. 이슈 번호: $ARGUMENTS, 없으면 현재 브랜치명 `{type}/{번호}-{slug}`에서 얻는다
2. 구현 기준을 정한다
   - 로컬 계획서 `.claude/resources/plans/PLAN-{번호}.md`(git 추적 안 함)가 있으면 그 계획서가 기준이다. "결정 필요"에 체크하지 않은 항목이 있으면 사용자에게 확정받아 계획서에 반영하기 전까지 시작하지 않는다
   - 계획서가 없으면 `gh issue view {번호}`의 Plan 체크리스트가 기준이다. 체크리스트만으로 정해지지 않는 사항(패키지 위치, 응답 형식, 정책 변경)은 시작 전에 사용자에게 묻는다

### 기준 이탈 처리

| 심각도 | 경우 | 처리 |
|---|---|---|
| critical | 기준에 논리 오류가 있다, 정해지지 않은 사항이 나왔다 | 멈추고 사용자에게 확정받은 뒤 진행 |
| trivial | 로직은 맞지만 더 나은 방법이 있다 | 계획서가 있으면 `Deviation Log`에 `- {파일}: {바꾼 것} - 이유: {이유}`, 없으면 결과 보고의 "이탈"에 적고 진행 |

### Phase 1: 구현

1. 레이어 순서대로 구현한다: Entity, Flyway 마이그레이션 → Repository → Service → DTO → Controller
2. 파일을 만들기 전에 Glob으로 있는지 확인하고, 있으면 Edit으로 고친다
3. 패키지 위치와 코드 규칙은 `.claude/rules/`(project-structure, code-convention, migration)를 따른다
4. 인증 없이 열어야 하는 경로를 추가하거나 바꾸면 인증 화이트리스트(`global/http/WhitelistEndpoint`)와 `auth/filter/JwtAuthenticationFilter`를 함께 확인한다. 보호할 경로가 열리거나 열어야 할 경로가 401이 되지 않게 한다
5. 정책(성공과 실패 조건, 제한 수치, 산정 기준, 검증 순서, 유효 기간, 권한 범위)이 바뀌면 `.claude/spec/service-policy/`의 해당 도메인 파일을 같은 작업에서 고친다

### Phase 2: 기준 대비 확인

`git diff --stat`을 기준(계획서의 영향 범위, 또는 이슈의 Plan 체크리스트)과 대조한다. 빠진 항목은 구현하고, 의도한 이탈이면 이탈로 적는다.

> 다음 Phase 조건: 빠진 항목이 없거나 모두 이탈로 설명됐을 때

### Phase 3: 결과 보고

`template/output.md`를 Read해 채워 보고한다. 주석은 출력하지 않는다.
