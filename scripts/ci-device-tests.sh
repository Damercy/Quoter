#!/usr/bin/env bash
set -euo pipefail

if [[ "${CI:-}" != true || "${ANDROID_SERIAL:-}" != emulator-* ]]; then
  printf '%s\n' 'This workflow targets a disposable CI emulator.' >&2
  exit 2
fi

# Keep the recorder and cleanup in one shell; emulator-runner separates script lines.
diagnostics=app/build/reports/device-diagnostics
mkdir -p "$diagnostics"
grep -Fx "target=android-$3" "$ANDROID_AVD_HOME/test.ini" > "$diagnostics/avd-target.txt"
adb shell wm size "$1"
adb shell wm density "$2"
adb logcat -c
adb shell df -h /data > "$diagnostics/storage.txt"
adb logcat -v threadtime > "$diagnostics/logcat.txt" &
logcat_pid=$!

cleanup() {
  kill "$logcat_pid" 2>/dev/null || true
  wait "$logcat_pid" 2>/dev/null || true
  timeout 15s adb shell dumpsys activity activities > "$diagnostics/activities.txt" || true
  timeout 15s adb exec-out screencap -p > "$diagnostics/screen.png" || true
  timeout 15s adb pull /sdcard/Android/data/com.dayaonweb.quoter/files "$diagnostics/additional" || true
}
trap cleanup EXIT

status=0
timeout --signal=TERM --kill-after=30s 12m ./gradlew :app:assembleDebug :app:assembleDebugAndroidTest
timeout --signal=TERM --kill-after=15s 2m adb install -r -t --user 0 app/build/outputs/apk/debug/app-debug.apk
timeout --signal=TERM --kill-after=15s 2m adb install -r -t -g --user 0 app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
timeout --signal=TERM --kill-after=30s 8m adb shell am instrument -r -w com.dayaonweb.quoter.test/androidx.test.runner.AndroidJUnitRunner | tee "$diagnostics/instrumentation.txt" || status=$?
if ! python3 scripts/verify_device_tests.py --result "$diagnostics/instrumentation.txt" --output "$diagnostics/device-tests.xml"; then
  if [[ "$status" == 0 ]]; then status=1; fi
fi
exit "$status"
