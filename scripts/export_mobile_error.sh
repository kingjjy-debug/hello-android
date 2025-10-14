#!/data/data/com.termux/files/usr/bin/bash
# 목적: Error_Log_mobile.txt 내용을 클립보드로 복사하고,
#       /sdcard/Download/hello_android_logs 로 파일도 내보내기(타임스탬프 부여)
# 사용: ./scripts/export_mobile_error.sh [경로]  # 기본값: ./Error_Log_mobile.txt

set -euo pipefail

SRC="${1:-./Error_Log_mobile.txt}"
DEST_DIR="/sdcard/Download/hello_android_logs"

if [[ ! -f "$SRC" ]]; then
  echo "[❌] 소스 파일을 찾을 수 없습니다: $SRC"
  exit 1
fi

# 1) termux-api 확인
if ! command -v termux-clipboard-set >/dev/null 2>&1; then
  echo "[❌] termux-api 패키지가 필요합니다. 설치: pkg install termux-api -y"
  exit 2
fi

# 2) 클립보드 복사
termux-clipboard-set "$(cat "$SRC")" || {
  echo "[❌] 클립보드 복사 실패"
  exit 3
}
echo "[✅] 클립보드에 복사 완료"

# 3) 외부 저장소 접근 준비 (입력 대기 방지용 백그라운드 실행)
(termux-setup-storage >/dev/null 2>&1 &) || true

# /sdcard 쓰기 가능해질 때까지 최대 30초 대기
for _ in $(seq 1 30); do
  if [[ -d "/sdcard" && -w "/sdcard" ]]; then
    break
  fi
  sleep 1
done
if [[ ! -w "/sdcard" ]]; then
  echo "[❌] /sdcard 접근 불가. 설정 > 앱 > Termux > 권한(파일/미디어) 허용 필요."
  exit 4
fi

# 4) 타임스탬프 붙여 내보내기
mkdir -p "$DEST_DIR"
TS="$(date +%Y%m%d-%H%M)"
BASE="$(basename "$SRC")"
EXT="${BASE##*.}"
NAME_NOEXT="${BASE%.*}"
OUT_PATH="${DEST_DIR}/${NAME_NOEXT}-${TS}.${EXT}"

cp -f "$SRC" "$OUT_PATH"
sync || true

if [[ -f "$OUT_PATH" ]]; then
  echo "[✅] 외부 저장소로 내보내기 완료: $OUT_PATH"
  echo "   → 파일 관리자나 PC 연결로 바로 전달 가능합니다."
else
  echo "[❌] 파일 복사 실패"
  exit 5
fi
