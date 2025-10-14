#!/usr/bin/env bash
set -euo pipefail

REPO="${1:-}"   # 형식: owner/repo (예: kingjjy-debug/hello-android). 비우면 자동 추론
BRANCH="$(git rev-parse --abbrev-ref HEAD)"
HEAD_SHA="$(git rev-parse HEAD)"
ERROR_LOG="Error_Log.txt"

# REPO 자동 추론
if [[ -z "$REPO" ]]; then
  OWNER="$(gh api user --jq '.login')"
  NAME="$(basename -s .git "$(git config --get remote.origin.url | sed 's#.*/##' )")"
  if [[ "$NAME" == "" ]]; then
    # fallback: gh repo view 사용
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
git push origin "$BRANCH"

echo "[INFO] GitHub Actions 런을 대기합니다 (head_sha 일치하는 런 검색)."

# 최신 head_sha에 해당하는 워크플로 런 ID 찾기 (최대 30개 검색에서 head_sha 일치하는 것 선택)
FIND_RUN_ID() {
  gh api "repos/${REPO}/actions/runs?branch=${BRANCH}&per_page=30" \
    --jq ".workflow_runs[] | select(.head_sha==\"${HEAD_SHA}\") | .id" | head -n1
}

# 대기: 런이 생성될 때까지
RUN_ID=""
for i in {1..30}; do
  RUN_ID="$(FIND_RUN_ID || true)"
  if [[ -n "$RUN_ID" ]]; then
    break
  fi
  sleep 3
done

if [[ -z "$RUN_ID" ]]; then
  echo "[ERROR] 해당 커밋의 워크플로 런을 찾지 못했습니다." | tee "$ERROR_LOG"
  exit 2
fi

echo "[INFO] Run ID: $RUN_ID"

# 상태 폴링: completed 될 때까지
STATUS=""
CONCLUSION=""
while :; do
  STATUS="$(gh api "repos/${REPO}/actions/runs/${RUN_ID}" --jq '.status')"
  CONCLUSION="$(gh api "repos/${REPO}/actions/runs/${RUN_ID}" --jq '.conclusion')"
  echo "[POLL] status=${STATUS} conclusion=${CONCLUSION}"
  if [[ "$STATUS" == "completed" ]]; then
    break
  fi
  sleep 5
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

  # gh run view로 로그 출력(가능한 경우)
  if gh run view "${RUN_ID}" --repo "${REPO}" --log > ._tmp_logs 2>/dev/null; then
    tail -n 200 ._tmp_logs >> "$ERROR_LOG" || true
    rm -f ._tmp_logs
  else
    # 대체: 각 job 로그 URL에서 일부만 추출
    gh api "repos/${REPO}/actions/runs/${RUN_ID}/jobs" --jq '.jobs[].html_url' | head -n 5 >> "$ERROR_LOG" || true
  fi

  echo "[INFO] 실패 로그가 ${ERROR_LOG} 에 저장되었습니다."
  exit 3
fi
