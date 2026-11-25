---
name: open-issue
description: |
  작업을 대화로 구체화해 GitHub 이슈를 열고, 연결된 작업 브랜치를 만들어 체크아웃한다.
  Trigger: "이슈 열어줘", "이거 작업 시작하자", "새 작업 시작하자", "기능/버그/리팩토링 이슈 만들어줘"
  Do NOT use for: 계획 수립(→ write-plan), 구현(→ implement), PR 생성(→ open-pr), 이미 열린 이슈에 브랜치만 만드는 경우(직접 git)
  Boundary: 이슈 생성과 브랜치 생성, 체크아웃까지만 한다.
argument-hint: "[작업 설명]"
allowed-tools: Read, Write, AskUserQuestion, Bash(git *), Bash(gh *)
model: sonnet
effort: medium
---

## 작업 발의 (이슈 + 브랜치)

이슈는 두 가지만 담는다. 어떤 이슈가 있었는가(`## Issue`), 어떻게 작업하려고 하는가(`## Plan`).

### Phase 1: 작업 구체화

$ARGUMENTS가 비었거나 모호하면 대화로 아래를 확정한다. 여러 작업이 섞여 있으면 이슈를 나누고 작업마다 Phase 1~3을 반복한다.

- **종류**: `.claude/spec/git-convention.md` 커밋 타입 표의 type 하나
- **제목**: git-convention 제목 규칙(40자 이내 명사형, 대상 나열 금지)
- **Issue**: 지금 무엇이 어떻게 되고 있고, 그래서 무엇이 잘못되거나 불편한지
- **Plan**: 무엇을 어떻게 바꿀지 2~5개, 이번에 하지 않을 것
- **slug**: 브랜치용 영문 kebab-case (예: `registration-queue`)

> 다음 Phase 조건: 다섯 요소가 정해졌을 때

### Phase 2: 본문 작성

1. `.github/ISSUE_TEMPLATE/issue.md`를 Read해 frontmatter 아래 두 구획을 그대로 쓴다. HTML 주석은 넣지 않는다
2. 구조, 분량, 말투는 `template/example.md`를 따른다
   - Issue: 관찰한 사실과 그 영향만 2~4문장. 해결책, 원인 추정, 배경 설명을 늘어놓지 않는다. 근거(경로, 링크)는 마지막에 `> [!NOTE]` 콜아웃
   - Plan: `- [ ]` 체크리스트. "무엇을 어떻게"를 쓰고 클래스, 메서드 이름을 나열하지 않는다. 하지 않을 것은 목록 뒤에 `> [!IMPORTANT]` 콜아웃(`하지 않는다: ...`)
   - 문단, 콜아웃, 구획 사이에는 `<br>`을 한 줄 둔다. 목록 항목 사이에는 두지 않는다
   - 두 구획 밖의 내용(관련 도메인 체크, 기대 효과, 일정)을 더하지 않는다
3. 제목과 본문은 git-convention 표기 규칙(가운데점 대신 콤마, 긴 대시 대신 짧은 대시)을 지킨다
4. 본문은 세션 스크래치패드에 파일로 저장한다(`--body-file`로 넘겨 셸 이스케이프 사고를 막는다)

### Phase 3: 이슈, 브랜치 생성

1. 이슈를 만든다. 라벨은 git-convention 커밋 타입 표의 이슈 라벨이다. 출력 URL에서 이슈 번호를 얻는다

   ```bash
   gh issue create --title "{type}: {제목}" --body-file {본문 파일} --label "{라벨}" --assignee @me
   ```

   - 라벨 지정이 실패하면 `gh label list`로 실제 이름을 확인한다
2. git-convention 브랜치 전략의 base(지금 `dev`)에서 `{type}/{번호}-{slug}` 브랜치를 만든다

   ```bash
   git fetch origin {base}
   git checkout -b {type}/{번호}-{slug} origin/{base}
   git push -u origin HEAD
   ```

   - hotfix처럼 base가 다를 수 있으면 사용자에게 base를 묻는다
   - `git checkout -b ... origin/{base}`는 upstream을 `origin/{base}`로 잡는다. `-u origin HEAD`로 같은 이름의 원격 브랜치를 upstream으로 바로잡는다

### Phase 4: 결과 보고

`template/output.md`를 Read해 채워 보고한다. 주석은 출력하지 않는다.
