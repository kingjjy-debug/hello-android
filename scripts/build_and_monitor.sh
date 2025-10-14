#!/usr/bin/env bash
set -euo pipefail

# 사용법:
#   ./scripts/build_and_monitor.sh [owner/repo] [--once|--quiet|--verbose] [--interval=초] [--no-empty-commit]
# 기본값:
#   - 폴링 출력: --once (한 번만 출력)
#   - 폴링 주기: 5초
#   - 변경이 없을 때도 새 런을 보장하려고 기본적으로 빈 커밋을 만들어 트리거합니다.
#     이를 끄려면 --no-empty-commit

REPO=""
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
ERROR_LOG="Error_Log.txt"
ONCE=1; QUIET=0; VERBOSE=0; INTERVAL=5
ALLOW_EMPTY=1

for arg in "$@"; do
  case "$arg" in
    */*) REPO="$arg" ;;
    --once) ONCE=1; QUIET=0; VERBOSE=0 ;;
    --quiet) QUIET=1; ONCE=0; VERBOSE=0 ;;
    --verbose) VERBOSE=1; ONCE=0; QUIET=0 ;;
    --interval=*) INTERVAL="${arg#*=}" ;;
    --no-empty-commit) ALLOW_EMPTY=0 ;;
    *) ;;
  esac
done

# REPO 자동 추론
if [[ -z "${REPO}" ]]; then
  OWNER="$(gh api user --jq '.login')"
  NAME="$(basename -s .git "$(git config --get remote.origin.url | sed 's#.*/##' )")"
  if [[ -z "$NAME" ]]; then
    FULL="$(gh repo view --json nameWithOwner --jq .nameWithOwner 2>/dev/null || true)"
    [[ -n "$FULL" ]] || { echo "원격 저장소 정보를 찾지 못했습니다. owner/repo 인자를 넘기세요." >&2; exit 1; }
    REPO="$FULL"
  else
    REPO="${OWNER}/${NAME}"
  fi
fi

echo "[INFO] Repository: $REPO"
echo "[INFO] Branch: $BRANCH"

# 변경사항 커밋(없으면 빈 커밋으로 강제 트리거)
if ! git diff --quiet || ! git diff --cached --quiet; then
  git add -A
  git commit -m "ci: trigger build at $(date -u +'%Y-%m-%dT%H:%M:%SZ')"
elif (( ALLOW_EMPTY )); then
  git commit --allow-empty -m "ci: trigger empty build at $(date -u +'%Y-%m-%dT%H:%M:%SZ')"
fi

HEAD_SHA="$(git rev-parse HEAD)"
echo "[INFO] Head SHA: $HEAD_SHA"

# 푸시 (실패 시 즉시 종료)
if ! git push origin "$BRANCH"; then
  {
    echo "==== Push Failed ===="
    echo "Repo: ${REPO}"
    echo "Branch: ${BRANCH}"
    echo "Commit: ${HEAD_SHA}"
    echo "Time(UTC): $(date -u +'%Y-%m-%dT%H:%M:%SZ')"
  } > "$ERROR_LOG"
  echo "❌ PUSH FAILED (세부내용은 ${ERROR_LOG})"
  exit 2
fi

echo "[INFO] GitHub Actions 런을 대기합니다 (head_sha 일치 + 가장 최신 created_at)."

# 지정 커밋(HEAD_SHA)의 "가장 최신" 런 ID를 찾는 함수
FIND_LATEST_RUN_ID() {
  gh api "repos/${REPO}/actions/runs?branch=${BRANCH}&per_page=50" \
    --jq "[.workflow_runs[] | select(.head_sha==\"${HEAD_SHA}\")] | sort_by(.created_at) | last | .id" 2>/dev/null
}

RUN_ID=""
# 런 생성 대기(최대 60초)
for _ in {1..20}; do
  RUN_ID="$(FIND_LATEST_RUN_ID || true)"
  if [[ "${RUN_ID}" != "null" && -n "${RUN_ID}" ]]; then
    break
  fi
  sleep 3
done

if [[ -z "$RUN_ID" || "$RUN_ID" == "null" ]]; then
  echo "[ERROR] 해당 커밋의 워크플로 런을 찾지 못했습니다." | tee "$ERROR_LOG"
  exit 3
fi

echo "[INFO] Run ID: $RUN_ID"

PRINTED=0
STATUS=""; CONCLUSION=""
while :; do
  # status: queued | in_progress | completed
  # conclusion: success | failure | cancelled | null(미완료)
  STATUS="$(gh api "repos/${REPO}/actions/runs/${RUN_ID}" --jq '.status')"
  CONCLUSION="$(gh api "repos/${REPO}/actions/runs/${RUN_ID}" --jq '.conclusion')"

  if (( VERBOSE )); then
    echo "[POLL] status=${STATUS} conclusion=${CONCLUSION}"
  elif (( ONCE )) && (( PRINTED == 0 )); then
    echo "[POLL] status=${STATUS} conclusion=${CONCLUSION}"
    PRINTED=1
  fi

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
  exit 4
fi
