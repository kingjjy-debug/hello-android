#!/data/data/com.termux/files/usr/bin/bash
# 목적: 설치/실행 문제 상황의 관련 로그를 한 번에 수집해 Error_Log_mobile.txt 로 저장
# 사용: 문제 발생 직후 현재 디렉토리에서 실행 (앱 패키지: com.kingjjy.hello)

set -o pipefail

PKG="com.kingjjy.hello"
OUT="Error_Log_mobile.txt"
TS="$(date +%Y-%m-%dT%H:%M:%S%z)"

# 유틸 경로 확인 (logcat은 시스템에 기본 존재)
LOGCAT_BIN="$(command -v logcat || echo /system/bin/logcat)"

{
  echo "==== Mobile Error Log ===="
  echo "Time: ${TS}"
  echo "Device: $(getprop ro.product.manufacturer) $(getprop ro.product.model)"
  echo "Android: $(getprop ro.build.version.release) (SDK $(getprop ro.build.version.sdk))"
  echo "Package: ${PKG}"
  echo

  echo "---- Package Presence ----"
  pm list packages | grep -F "${PKG}" || echo "(not installed)"
  echo

  echo "---- dumpsys package (${PKG}) (short) ----"
  dumpsys package "${PKG}" 2>/dev/null | sed -n '1,120p' || echo "(no dumpsys package output)"
  echo

  echo "---- Last CRASH buffer (logcat -b crash) ----"
  "${LOGCAT_BIN}" -b crash -d -v time 2>/dev/null | tail -n 400 || echo "(no crash buffer or permission denied)"
  echo

  echo "---- Recent AndroidRuntime (FATAL) ----"
  "${LOGCAT_BIN}" -d -v time 2>/dev/null | grep -E "AndroidRuntime|FATAL EXCEPTION" | tail -n 400 || echo "(no AndroidRuntime lines)"
  echo

  echo "---- Package install/pm/activity related ----"
  "${LOGCAT_BIN}" -d -v time 2>/dev/null | grep -E "PackageManager|PackageInstaller|ActivityManager|ActivityTaskManager" | tail -n 600 || echo "(no PM/AM lines)"
  echo

  echo "---- App-specific lines (${PKG}) ----"
  "${LOGCAT_BIN}" -d -v time 2>/dev/null | grep -F "${PKG}" | tail -n 800 || echo "(no lines containing package name)"
  echo

  echo "---- Process/Activity snapshot ----"
  (echo "[dumpsys activity processes]"; dumpsys activity processes 2>/dev/null | sed -n '1,150p'
   echo
   echo "[dumpsys activity activities]"; dumpsys activity activities 2>/dev/null | sed -n '1,150p') || echo "(no dumpsys activity)"
  echo

  echo "---- Optional quick probe (won't fail script) ----"
  echo "\$ am start -W -n ${PKG}/.MainActivity  (timings only, may fail if crashing)"
  am start -W -n "${PKG}/.MainActivity" 2>&1 | sed -n '1,80p' || echo "(am start failed or app not installed)"
  echo

} > "${OUT}"

# 로그 덤프 후 원본 logcat은 유지(다른 앱 영향 방지). 필요 시 다음 줄의 주석을 해제해 클리어 가능
# ${LOGCAT_BIN} -c >/dev/null 2>&1 || true

echo "[✅] 수집 완료: ${OUT}"
echo "이 파일을 복사해 보내주세요. (필요하면: termux-clipboard-set \"\$(cat ${OUT})\")"
