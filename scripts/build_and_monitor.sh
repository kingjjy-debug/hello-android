#!/usr/bin/env bash
set -euo pipefail

# 사용법:
#   ./scripts/build_and_monitor.sh [owner/repo] [--once] [--quiet] [--verbose] [--interval=초]
# 기본값: --once (첫 폴링 한 번만 출력), interval=5
#  - --once: 첫 폴링 상태 한 줄만 출력하고 이후엔 결과만 출력
#  - --quiet: 폴링 중엔 아무 것도 출력하지 않고 마지막 결과만 출력
#  - --verbose: 매 폴링 상태를 모두 출력(이전 동작)
#  - --interval=초: 폴링 주기 변경(기본 5초)

REPO=""
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
HEAD_SHA="$(git rev-parse HEAD)"
ERROR_LOG="Error_Log.txt"

# 출력 모드
ONCE=1       # 기본 한 번만
QUIET=0
VERBOSE=0
INTERVAL=5

# 인자 파싱
for arg in "$@"; do
  case "$arg" in
    */*) REPO="$arg" ;;
    --once) ONCE=1; QUIET=0; VERBOSE=0 ;;
    --quiet) QUIET=1; ONCE=0; VERBOSE=0 ;;
    --verbose) VERBOSE=1; ONCE=0; QUIET=0 ;;
    --interval=*) INTERVAL="${arg#*=}" ;;
    *) ;;
  esac
done

# REPO 자동 추론
if [[ -z "$REPO" ]]; then
  OWNER="$(gh api user --jq '.login')"
  NAME="$(basename -s .git "$(git config --get remote.origin.url | sed 's#.*/##' )")"
  if [[ -z "$NAME" ]]; then
    FULL="$(gh repo view --json nameWithOwner --jq .nameWithOwner 2>/dev/null || true)"
    if [[ -n "$FULL" ]]; then
      REPO="$FULL"
    else
      echo "원격 저장소 정보를 찾을 수 없습니다. 인자로 owner/repo를 전달하세요." >&2
      exit 1
    fi
  else
    REPO="${OWNER}/${NAME}"
  fi
fi

echo "[INFO] Repository: $REPO"
echo "[INFO] Branch: $BRANCH"
echo "[INFO] Head SHA: $HEAD_SHA"

# 변경사항 커밋/푸시 (변경 없으면 스킵)
if ! git diff --quiet || ! git diff --cached --quiet; then
  git add -A
  git commit -m "ci: trigger build at $(date -u +'%Y-%m-%dT%H:%M:%SZ')"
fi
git push origin "$BRANCH" >/dev/null 2>&1 || true

# 최신 head_sha에 해당하는 워크플로 런 ID 찾기
FIND_RUN_ID() {
  gh api "repos/${REPO}/actions/runs?branch=${BRANCH}&per_page=30" \
    --jq ".workflow_runs[] | select(.head_sha==\"${HEAD_SHA}\") | .id" | head -n1
}

echo "[INFO] GitHub Actions 런을 대기합니다 (head_sha 일치하는 런 검색)."

RUN_ID=""
for _ in {1..30}; do
  RUN_ID="$(FIND_RUN_ID || true)"
  [[ -n "$RUN_ID" ]] && break
  sleep 3
done

if [[ -z "$RUN_ID" ]]; then
  echo "[ERROR] 해당 커밋의 워크플로 런을 찾지 못했습니다." | tee "$ERROR_LOG"
  exit 2
fi

echo "[INFO] Run ID: $RUN_ID"

PRINTED=0
STATUS=""
CONCLUSION=""

while :; do
  STATUS="$(gh api "repos/${REPO}/actions/runs/${RUN_ID}" --jq '.status')"
  CONCLUSION="$(gh api "repos/${REPO}/actions/runs/${RUN_ID}" --jq '.conclusion')"

  if (( VERBOSE )); then
    echo "[POLL] status=${STATUS} conclusion=${CONCLUSION}"
  elif (( ONCE )) && (( PRINTED == 0 )); then
    echo "[POLL] status=${STATUS} conclusion=${CONCLUSION}"
    PRINTED=1
  fi
  # QUIET 모드일 땐 폴링 출력 없음

  [[ "$STATUS" == "completed" ]] && break
  sleep "$INTERVAL"
done

if [[ "$CONCLUSION" == "success" ]]; then
  echo "✅ BUILD SUCCESS"
  echo "[INFO] 아티팩트 목록:"
  gh api "repos/${REPO}/actions/runs/${RUN_ID}/artifacts" --jq '.artifacts[].name'
  exit 0
else
  echo "❌ BUILD FAILED"
  {
    echo "==== Build Failed ===="
    echo "Repo: ${REPO}"
    echo "Branch: ${BRANCH}"
    echo "RunID: ${RUN_ID}"
    echo "Commit: ${HEAD_SHA}"
    echo "Time(UTC): $(date -u +'%Y-%m-%dT%H:%M:%SZ')"
    echo
    echo "---- Jobs & Conclusions ----"
    gh api "repos/${REPO}/actions/runs/${RUN_ID}/jobs" --jq '.jobs[] | "\(.name): \(.conclusion)"'
    echo
    echo "---- Tail of Logs (last 200 lines) ----"
  } > "$ERROR_LOG"

  if gh run view "${RUN_ID}" --repo "${REPO}" --log > ._tmp_logs 2>/dev/null; then
    tail -n 200 ._tmp_logs >> "$ERROR_LOG" || true
    rm -f ._tmp_logs
  else
    gh api "repos/${REPO}/actions/runs/${RUN_ID}/jobs" --jq '.jobs[].html_url' | head -n 5 >> "$ERROR_LOG" || true
  fi

  echo "[INFO] 실패 로그가 ${ERROR_LOG} 에 저장되었습니다."
  exit 3
fi
