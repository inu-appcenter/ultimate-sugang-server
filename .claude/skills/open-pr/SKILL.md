---
name: open-pr
description: |
  PR을 생성한다. 작업 성격에 맞는 PR 템플릿(단순, 문제 > 개선 > 결과)을 골라 본문을 쓰고 담당자, 라벨, 리뷰어를 지정한다.
  Trigger: "PR 날려줘", "PR 만들어줘", "PR 생성해줘", "PR 올려줘"
  Do NOT use for: 커밋(→ commit-push), 이슈, 브랜치 생성(→ open-issue), 리뷰 피드백 반영(→ review-feedback)
  Boundary: PR 생성과 담당자, 라벨, 리뷰어 지정까지만 한다. 머지는 하지 않는다.
argument-hint: "[base 브랜치]"
allowed-tools: Read, Write, Bash(git *), Bash(gh *)
model: sonnet
effort: medium
---

## PR 생성

PR 템플릿은 두 개다. 작업 성격을 보고 하나를 고른다(Phase 2).

| 템플릿 | 파일 | 담는 것 |
|---|---|---|
| 단순 | `.github/PULL_REQUEST_TEMPLATE.md` | 닫을 이슈(`## Related Issue`), 이슈의 Plan을 어떻게 구현했는가(`## Implementation`) |
| 문제 > 개선 > 결과 | `.github/PULL_REQUEST_TEMPLATE/problem-solving.md` | 요약, 무엇이 왜 문제였나(`## Problem`), 어떤 방법을 왜 골랐고 결과가 어땠나(`## Solution`), 닫을 이슈 |

### 시작 조건

1. 현재 브랜치 `{type}/{번호}-{slug}`에서 type과 이슈 번호를 얻는다. 번호가 없으면 묻는다
2. `gh pr view --json url,state`로 이 브랜치에 열린 PR이 있으면 그 URL을 알리고 끝낸다
3. 원격 브랜치가 없거나 `git rev-list --count @{u}..HEAD`가 0보다 크면 push되지 않은 커밋이 있다고 알리고 `commit-push`를 권한 뒤 멈춘다
4. base는 $ARGUMENTS가 있으면 그것, 없으면 git-convention 브랜치 전략의 base(지금 `dev`)

### Phase 1: 재료 모으기

1. `gh issue view {번호} --json title,body,labels`로 이슈를 읽는다. 이슈의 Issue와 Plan이 본문의 기준이다
2. `git log --oneline {base}..HEAD`, `git diff --stat {base}...HEAD`로 실제로 한 작업을 확인한다. 내용이 필요한 파일만 diff로 본다
3. 로컬 계획서 `.claude/resources/plans/PLAN-{번호}.md`(git 추적 안 함)가 있으면 Deviation Log를 읽는다
4. 성능, 동시성 작업이면 로컬 기록 `.claude/resources/{perf|concurrency}/{번호}/{슬러그}/record.md`(git 추적 안 함)의 최종 요약을 읽는다
5. 대화에서 정한 결정 중 이슈에 없는 것이 있으면 메모한다

### Phase 2: 템플릿 선택, 제목, 본문 작성

1. 템플릿을 고른다
   - **문제 > 개선 > 결과**: 아래 중 하나라도 해당하면
     - 성능, 동시성 작업처럼 측정 기록(`record.md`)이 있다
     - 버그 수정인데 원인이 코드만 봐서는 드러나지 않아 설명이 필요하다
     - 캐시, 락, 인덱스, 구조 변경처럼 여러 방법 중 하나를 골랐고, 왜 그것을 골랐는지가 남아야 한다
   - **단순**: 그 밖(기능 추가, 문서, 설정, 테스트 보강, 작은 수정)
   - 기준으로 갈리지 않으면 사용자에게 묻는다
2. 제목: `{type}: {설명}(#{번호})`. git-convention 제목 규칙을 따른다
3. 고른 템플릿의 구획을 그대로 쓴다. HTML 주석은 넣지 않는다. 템플릿에 없는 구획은 더하지 않는다
4. 단순 템플릿이면 구조, 분량, 말투는 `template/example.md`를 따른다
   - Related Issue: `- close #{번호}` 한 줄만
   - Implementation: 작업 전 상황 한두 문장, 이어서 Plan을 실제로 어떻게 구현했는지 순서대로 3~6줄. Plan과 달라진 점이 있으면 마지막에 `> [!NOTE]` 콜아웃으로 이유와 함께 한 줄
   - 문단, 목록, 콜아웃, 구획 사이에는 `<br>`을 한 줄 둔다. 목록 항목 사이에는 두지 않는다
   - 쓰지 않는 것: 변경 파일과 메서드 나열(diff가 보여준다), 커밋 메시지 복사, 문제와 해결을 부풀린 서술
5. 문제 > 개선 > 결과 템플릿이면 `template/problem-solving-guide.md`를 Read해 그 규칙대로 쓴다. 결과(수치, Before와 After)는 Solution 안에 쓴다
6. 수치는 record.md에서 잰 값만 쓴다. 재지 않은 값을 추정해 채우지 않는다
7. 문체: 존댓말(`~했습니다`). git-convention 표기 규칙(가운데점 대신 콤마, 긴 대시 대신 짧은 대시)
8. 본문은 세션 스크래치패드에 파일로 저장한다

### Phase 3: 생성

1. 리뷰어: git-convention 팀 로스터에서 호출자(`gh api user --jq .login`)를 뺀 나머지. 남는 사람이 없으면 지정하지 않는다
2. 라벨: 이슈의 라벨을 그대로 쓴다

   ```bash
   gh pr create --title "{제목}" --body-file {본문 파일} --base {base} \
     --assignee @me --label "{라벨}" [--reviewer {리뷰어1},{리뷰어2}]
   ```

   - 리뷰어 지정이 실패해도 PR은 되돌리지 않는다. `gh pr edit {번호} --add-reviewer {리뷰어}`로 한 번 다시 시도하고, 그래도 실패하면 보고에 남긴다

### Phase 4: 결과 보고

`template/output.md`를 Read해 채워 보고한다. 주석은 출력하지 않는다.
