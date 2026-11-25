#!/bin/bash
# PreToolUse(Bash) 훅: 위험한 명령 차단, 확인 요청
#
#   DENY : 되돌릴 수 없이 파괴적인 명령 -> stderr 사유 + exit 2
#   ASK  : 맥락상 위험하지만 정당한 사용도 있는 명령 -> permissionDecision "ask"
#   exit 0(출력 없음) = 허용
#
# 기준: backend-engineering-harness hooks/block-dangerous-commands.sh.
# 이 레포에는 하네스 CLI(judge-command, get-config)가 없어 재귀 삭제와 마이그레이션 경로를 고정 규칙으로 판정한다.
# settings.json의 deny 규칙은 훅의 판정과 무관하게 명령을 막는다(훅이 허용해도 뒤집히지 않는다).

command -v jq >/dev/null 2>&1 || { echo "차단됨: jq가 없어 안전 검사를 수행할 수 없습니다." >&2; exit 2; }

MIG_DIR="database/(migration|seed)"

INPUT=$(cat)
COMMAND=$(printf '%s' "$INPUT" | jq -r '.tool_input.command // empty')

[ -z "$COMMAND" ] && exit 0

deny() {
  echo "차단됨: $1" >&2
  exit 2
}

ask() {
  jq -n --arg reason "$1" \
    '{hookSpecificOutput: {hookEventName: "PreToolUse", permissionDecision: "ask", permissionDecisionReason: $reason}}'
  exit 0
}

# 공백을 정규화해 "DROP   DATABASE" 같은 다중 공백 우회를 막는다
NORM=$(printf '%s' "$COMMAND" | tr '[:upper:]' '[:lower:]' | tr -s '[:space:]' ' ')

# ── DENY ────────────────────────────────────────────────

printf '%s' "$NORM" | grep -qF ':(){ :|:& };:' && deny "포크밤으로 의심되는 명령입니다."

printf '%s' "$NORM" | grep -qE '(>|of=) *\/dev\/(sd[a-z]|nvme|r?disk)' \
  && deny "디스크 디바이스에 직접 쓰는 명령입니다."

printf '%s' "$NORM" | grep -qE 'mkfs(\.| )' && deny "파일시스템을 새로 생성(mkfs)하는 명령입니다."

printf '%s' "$NORM" | grep -qE 'rm +-[a-z]*r[a-z]* +(-[a-z]+ +)*(/|~/?|\$home/?)(\*|[[:space:]]|$)' \
  && deny "루트, 홈 디렉터리를 재귀 삭제하는 것으로 의심되는 명령입니다."

# force push. settings.json의 deny는 접두 매칭이라 원격 이름 뒤에 붙인 플래그를 놓친다. 여기서 토큰으로 잡는다
if printf '%s' "$NORM" | grep -qE '(^|[;&|] *)git( +-[a-z] +[^ ]+)* +push( |$)'; then
  printf '%s' "$NORM" | grep -qE ' (--force|-f)( |$)' \
    && deny "force push 명령입니다. 원격 이력을 덮어씁니다."
  printf '%s' "$NORM" | grep -qE ' --force-(with-lease|if-includes)' \
    && ask "force push(--force-with-lease) 명령입니다. 원격 이력을 덮어쓸 수 있습니다."
fi

# ── ASK ─────────────────────────────────────────────────

if printf '%s' "$NORM" | grep -qE '(psql|mysql|mariadb|docker exec)'; then
  printf '%s' "$NORM" | grep -qE 'drop +(database|table|schema)|truncate table' \
    && ask "DB 클라이언트 컨텍스트에서 DDL 파괴 명령(DROP, TRUNCATE)이 감지되었습니다."
fi

if printf '%s' "$NORM" | grep -q 'chmod' && printf '%s' "$NORM" | grep -q '777'; then
  ask "권한을 777로 변경하는 명령입니다."
fi

printf '%s' "$NORM" | grep -qE 'rm +-[a-z]*r[a-z]*( |$)|rm +--recursive' \
  && ask "재귀 삭제(rm -r 계열) 명령입니다. 대상 경로를 확인하세요."

printf '%s' "$NORM" | grep -qE 'find .*-delete|xargs .*rm' \
  && ask "find, xargs를 통한 일괄 삭제 명령입니다."

printf '%s' "$NORM" | grep -qE 'git push .*[[:space:]]\+[a-z0-9]' \
  && ask "refspec force-push(+) 패턴이 감지되었습니다."

printf '%s' "$NORM" | grep -qE 'git clean' \
  && ask "git clean은 추적되지 않는 파일과 디렉토리를 삭제합니다."

printf '%s' "$NORM" | grep -qE 'flyway ?(clean|repair)' \
  && ask "Flyway 파괴적 태스크(clean, repair)가 감지되었습니다."

if printf '%s' "$NORM" | grep -qE "$MIG_DIR"; then
  printf '%s' "$NORM" | grep -qE 'sed -i|mv |rm |cp |tee |>>|>' \
    && ask "적용된 마이그레이션은 불변입니다. 새 버전 파일로 추가하세요."
fi

printf '%s' "$COMMAND" | grep -qE '\.claude/(hooks|settings)' \
  && printf '%s' "$COMMAND" | grep -qE 'sed -i|tee|>>|>|mv |rm |chmod ' \
  && ask ".claude 훅, 설정 파일을 변경하는 명령입니다. 사용자 승인이 필요합니다."

exit 0
