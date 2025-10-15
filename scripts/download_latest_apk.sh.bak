#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

# === 설정 ===
REPO="kingjjy-debug/hello-android"
OUT_DIR="$HOME/android-apps/hello-android/apk"
DEST_DIR="/sdcard/Download/apk_list"

echo "[INFO] 최신 빌드된 APK 다운로드 준비..."

mkdir -p "$OUT_DIR"
rm -f "$OUT_DIR"/*.apk 2>/dev/null || true

# 최신 성공 빌드 아티팩트 다운로드
echo "[INFO] GitHub에서 APK 아티팩트 다운로드 중..."
gh run download --repo "$REPO" --name hello-android-apk --dir "$OUT_DIR"

# APK 찾기
APK_PATH="$(find "$OUT_DIR" -type f -name "*.apk" | head -n1 || true)"
if [[ -z "${APK_PATH}" || ! -f "${APK_PATH}" ]]; then
  echo "[ERROR] APK 다운로드 실패(파일을 찾을 수 없음)."
  exit 1
fi

# 타임스탬프 파일명
TS="$(date +%Y%m%d-%H%M)"
BASE="$(basename "$APK_PATH")"
EXT="${BASE##*.}"
NAME_NOEXT="${BASE%.*}"
DEST_NAME="${NAME_NOEXT}-${TS}.${EXT}"

# ===== 스토리지 접근 자동화 =====
echo "[INFO] 외부 저장소 접근 준비 중..."

# termux-setup-storage 백그라운드로 실행 (입력 대기 방지)
(termux-setup-storage >/dev/null 2>&1 &) || true

# /sdcard 접근 가능 여부를 최대 30초 동안 체크
for i in $(seq 1 30); do
  if [[ -d "/sdcard" && -w "/sdcard" ]]; then
    echo "[INFO] 외부 저장소 접근 가능 ✅"
    break
  fi
  sleep 1
done

# 그래도 안 되면 오류 종료
if [[ ! -w "/sdcard" ]]; then
  echo "[ERROR] /sdcard 접근 불가. Termux 저장소 권한을 수동으로 허용하세요."
  echo "설정 > 앱 > Termux > 권한 > 파일 및 미디어 → '허용'으로 변경 필요."
  exit 2
fi

# ===== 복사 수행 =====
mkdir -p "$DEST_DIR"

echo "[INFO] ${DEST_DIR} 으로 복사 중..."
cp -f "$APK_PATH" "${DEST_DIR}/${DEST_NAME}"

if [[ -f "${DEST_DIR}/${DEST_NAME}" ]]; then
  echo "[SUCCESS] APK 복사 완료:"
  echo "         ${DEST_DIR}/${DEST_NAME}"
  echo "📱 파일 관리자나 알림창에서 해당 APK를 눌러 설치하세요."
else
  echo "[ERROR] 복사 실패. 저장소 권한 또는 경로를 확인하세요."
  exit 3
fi
