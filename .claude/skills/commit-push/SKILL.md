---
name: commit-push
description: |
  변경 사항을 작업 성격별 커밋으로 묶어 컨벤션에 맞는 메시지로 커밋하고 push한다.
  Trigger: "커밋해줘", "커밋하고 푸시해줘", "커밋 푸시해줘", "푸시해줘", "커밋 올려줘", "커밋까지만 해줘"
  Do NOT use for: 이슈, 브랜치 생성(→ open-issue), PR 생성(→ open-pr), 구현(→ implement)
  Boundary: 커밋과 push까지만 한다.
argument-hint: "[커밋까지만 | 사용자 지시]"
allowed-tools: Read, AskUserQuestion, Bash(git *)
model: sonnet
effort: medium
---

## 커밋, 푸시

### 시작 조건

- 현재 브랜치가 `main`이나 `dev`면 "main, dev에는 직접 커밋하지 않는다"를 알리고 멈춘다
- `git status --short`가 비어 있으면 커밋할 변경이 없다고 알리고 멈춘다
- 브랜치명 `{type}/{번호}-{slug}`에서 이슈 번호를 얻는다. 없으면 묻고, 없다고 하면 메시지의 `(#번호)` 없이 진행한다

### Phase 1: 변경 파악과 커밋 계획

1. `git status --short`(추적 안 하는 파일 포함), `git diff`, `git diff --staged`로 실제 변경을 본다. 계획서와 대화 내용보다 diff가 기준이다
2. 변경을 작업 성격별로 묶는다. 성격이 하나면 커밋 하나, 여러 갈래(예: 기능과 테스트)면 `.claude/spec/git-convention.md` 커밋 타입별로 나눈다
3. 커밋마다 메시지 `{type}: {내용}(#{번호})`를 정한다. 내용은 무엇을 했는지 명사형 한 줄이고 git-convention 제목 규칙, 표기 규칙을 따른다
4. 커밋하지 않을 변경을 따로 적는다: 비밀값이 든 파일(`.env`, 키 파일, `tokens.json`), 사용자가 뺀 파일, 이번 작업과 관계없는 변경
5. push 여부: $ARGUMENTS에 "커밋까지만", "푸시 안 함"이 있으면 push하지 않는다

### Phase 2: 사용자 확인

커밋별 메시지와 파일 목록, 커밋하지 않을 변경, push 대상(브랜치, 첫 push 여부)을 보여주고 승인을 받는다. 수정 요청이 있으면 반영해 다시 확인한다. 승인 전에는 커밋하지 않는다.

> 다음 Phase 조건: 사용자가 승인했을 때

### Phase 3: 커밋, 푸시

1. 커밋마다 그 파일만 스테이징하고 커밋한다. `git add -A`, `git add .`는 쓰지 않는다

   ```bash
   git add {파일들}
   git commit -m "{메시지}"
   ```

   - `.gitignore`에 걸린 경로라도 이미 추적 중인 파일은 `git add -u {파일}`로 올린다
2. push한다. `git rev-parse --abbrev-ref --symbolic-full-name @{u}`가 비었거나 `origin/{현재 브랜치}`가 아니면 `git push -u origin HEAD`, 같으면 `git push`
3. 실패하면 이미 만든 커밋과 실패 내용을 보고하고 멈춘다

### Phase 4: 결과 보고

`template/output.md`를 Read해 채워 보고한다. 주석은 출력하지 않는다.
