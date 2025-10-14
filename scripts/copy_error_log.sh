#!/usr/bin/env bash
set -e

LOG_FILE="./Error_Log.txt"

if [ ! -f "$LOG_FILE" ]; then
  echo "[❌] Error_Log.txt 파일이 현재 경로에 없습니다."
  echo "[INFO] 위치: $(pwd)"
  exit 1
fi

if ! command -v termux-clipboard-set >/dev/null 2>&1; then
  echo "[❌] termux-api 패키지가 설치되어 있지 않습니다."
  echo "[INFO] 설치 명령: pkg install termux-api -y"
  exit 2
fi

# 로그 파일을 복사
termux-clipboard-set "$(cat "$LOG_FILE")"

echo "[✅] Error_Log.txt 내용이 클립보드에 복사되었습니다."
echo "이제 ChatGPT 창에 바로 붙여넣기(Ctrl+V 또는 길게 눌러 붙여넣기) 해주세요."
