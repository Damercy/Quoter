#!/usr/bin/env bash
set -euo pipefail

# Keep the recorder and cleanup in one shell; emulator-runner separates script lines.
diagnostics=app/build/reports/device-diagnostics
mkdir -p "$diagnostics"
adb shell wm size "$1"
adb shell wm density "$2"
adb logcat -c
adb logcat -v threadtime > "$diagnostics/logcat.txt" &
logcat_pid=$!

cleanup() {
  kill "$logcat_pid" 2>/dev/null || true
  wait "$logcat_pid" 2>/dev/null || true
  timeout 15s adb shell dumpsys activity activities > "$diagnostics/activities.txt" || true
  timeout 15s adb exec-out screencap -p > "$diagnostics/screen.png" || true
}
trap cleanup EXIT

status=0
timeout --signal=TERM --kill-after=30s 15m ./gradlew :app:connectedDebugAndroidTest --info || status=$?
exit "$status"
