#!/usr/bin/env bash
set -euo pipefail

# 사용법:
#   ./scripts/download_latest_apk.sh [owner/repo] [--branch=브랜치] [--name=artifact_name] [--allow-empty]
# 기본 동작:
#   - 가장 최신의 "성공(SUCCESS)"한 워크플로 런 중에서
#   - 커밋 메시지가 "^ci: trigger empty build"로 시작하는 빈 커밋 런은 기본적으로 제외
#   - 아티팩트 이름 기본값: hello-android-apk
#   - 결과 APK를 /sdcard/Download/apk_list/hello-android_YYYY-MM-DD_HHMMSS.apk 로 저장

REPO=""
BRANCH="$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "")"
ARTIFACT_NAME="hello-android-apk"
ALLOW_EMPTY=0
MAX_PAGES=5
DEST_DIR="/sdcard/Download/apk_list"

for arg in "$@"; do
  case "$arg" in
    */*) REPO="$arg" ;;
    --branch=*) BRANCH="${arg#*=}" ;;
    --name=*) ARTIFACT_NAME="${arg#*=}" ;;
    --allow-empty) ALLOW_EMPTY=1 ;;
    *) ;;
  esac
done

# REPO 자동 추론
if [[ -z "${REPO}" ]]; then
  OWNER="$(gh api user --jq '.login')"
  NAME="$(basename -s .git "$(git config --get remote.origin.url | sed 's#.*/##' )" 2>/dev/null || true)"
  if [[ -z "$NAME" ]]; then
    FULL="$(gh repo view --json nameWithOwner --jq .nameWithOwner 2>/dev/null || true)"
    [[ -n "$FULL" ]] || { echo "원격 저장소 정보를 찾지 못했습니다. owner/repo 인자를 넘기세요." >&2; exit 1; }
    REPO="$FULL"
  else
    REPO="${OWNER}/${NAME}"
  fi
fi

# 브랜치 자동 추론 (비어있으면 기본 remote default branch 사용)
if [[ -z "$BRANCH" ]]; then
  BRANCH="$(gh api "repos/${REPO}" --jq '.default_branch')"
fi

echo "[INFO] Repository: $REPO"
echo "[INFO] Branch:     $BRANCH"
echo "[INFO] Artifact:    $ARTIFACT_NAME"
echo "[INFO] AllowEmpty:  $ALLOW_EMPTY"

# 최신 성공 런 중에서, (기본) 빈 커밋 런 제외하고 가장 최근 것 선택
RUN_ID=""
for PAGE in $(seq 1 $MAX_PAGES); do
  # 성공한 런들 페치
  JSON="$(gh api "repos/${REPO}/actions/runs?branch=${BRANCH}&status=success&per_page=50&page=${PAGE}")" || true

  if (( ALLOW_EMPTY )); then
    # 빈 커밋 포함: head_commit.message 무시
    RUN_ID="$(echo "$JSON" | jq -r '[.workflow_runs[]] | sort_by(.created_at) | reverse | .[0].id // empty')"
  else
    # 빈 커밋 제외: 메시지 ^ci: trigger empty build 로 시작하는 런은 거르기
    RUN_ID="$(echo "$JSON" | jq -r '[.workflow_runs[]
      | select(.head_commit != null)
      | select(.head_commit.message | test("^ci: trigger empty build") | not)
    ] | sort_by(.created_at) | reverse | .[0].id // empty')"
  fi

  [[ -n "$RUN_ID" ]] && break
done

if [[ -z "$RUN_ID" ]]; then
  echo "❌ 적절한 성공 런을 찾지 못했습니다. (빈 커밋 제외 모드). 필요 시 --allow-empty 를 사용해보세요."
  exit 3
fi

echo "[INFO] Selected Run ID: $RUN_ID"

# 런의 아티팩트 목록에서 대상 이름 찾기
ARTIFACT_ID="$(gh api "repos/${REPO}/actions/runs/${RUN_ID}/artifacts" \
  --jq ".artifacts[] | select(.name==\"${ARTIFACT_NAME}\") | .id" 2>/dev/null || true)"

# 없으면 첫 번째 아티팩트라도 받기 (폴백)
if [[ -z "$ARTIFACT_ID" ]]; then
  echo "[WARN] '${ARTIFACT_NAME}' 아티팩트를 못 찾았습니다. 첫 번째 아티팩트로 대체합니다."
  ARTIFACT_ID="$(gh api "repos/${REPO}/actions/runs/${RUN_ID}/artifacts" --jq '.artifacts[0].id' 2>/dev/null || true)"
fi

if [[ -z "$ARTIFACT_ID" ]]; then
  echo "❌ 아티팩트가 존재하지 않습니다."
  exit 4
fi

echo "[INFO] Artifact ID: $ARTIFACT_ID"

WORKDIR="$(mktemp -d)"
ZIPFILE="${WORKDIR}/artifact.zip"

# 아티팩트 다운로드 (gh run download 사용 - Termux 호환)
TMPDIR="${WORKDIR}/dl"
mkdir -p "$TMPDIR"
gh run download "$RUN_ID" --repo "$REPO" -n "$ARTIFACT_NAME" --dir "$TMPDIR"

# APK 찾기
APK_PATH="$(find "$TMPDIR" -type f -name '*.apk' | head -n1 || true)"
if [[ -z "$APK_PATH" ]]; then
  # zip으로 직접 받은 경우(이전 행동) 대비: 워크디렉토리도 탐색
  APK_PATH="$(find "$WORKDIR" -type f -name '*.apk' | head -n1 || true)"
fi
if [[ -z "$APK_PATH" ]]; then
  echo "❌ 아티팩트에서 APK 파일을 찾지 못했습니다."
  exit 5
fi
APK_PATH="$(find "$WORKDIR" -type f -name '*.apk' | head -n1 || true)"
if [[ -z "$APK_PATH" ]]; then
  echo "❌ ZIP 안에서 APK 파일을 찾지 못했습니다."
  exit 5
fi

# 저장 경로 준비
mkdir -p "$DEST_DIR" 2>/dev/null || true
TS="$(date +'%Y-%m-%d_%H%M%S')"
DEST_APK="${DEST_DIR}/hello-android_${TS}.apk"

cp -f "$APK_PATH" "$DEST_APK"

echo "✅ APK 저장 완료: $DEST_APK"
echo "   (파일 앱에서 열거나, 다음으로 설치 가능)"
echo "   am start -a android.intent.action.VIEW -d 'file://${DEST_APK}' -t 'application/vnd.android.package-archive'"

# 정리
rm -rf "$WORKDIR"
