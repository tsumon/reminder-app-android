#!/usr/bin/env bash
set -uo pipefail

# Preserve instrumentation's real status independently of post-test visual evidence.
test_status=0
./gradlew connectedDebugAndroidTest --no-daemon || test_status=$?

smoke_dir=app/build/reports/androidTests/smoke
mkdir -p "$smoke_dir"
rm -f "$smoke_dir/home.png" "$smoke_dir/home.pending.png"
capture_smoke() {
  # Instrumentation may uninstall the target APK during cleanup. Install the exact built debug APK.
  adb install -r app/build/outputs/apk/debug/app-debug.apk > "$smoke_dir/install.txt" 2>&1 || return 10
  grep -q '^Success' "$smoke_dir/install.txt" || return 11
  adb shell input keyevent KEYCODE_WAKEUP || return 12
  adb shell wm dismiss-keyguard || return 13
  adb shell am start -W -n com.reminderapp/.MainActivity > "$smoke_dir/launch.txt" 2>&1 || return 14
  if grep -Eiq 'Error|Exception|does not exist|unable to resolve' "$smoke_dir/launch.txt"; then return 15; fi
  grep -Eq '^Status: ok' "$smoke_dir/launch.txt" || return 16
  grep -Eq '^Activity: com\.reminderapp/(\.MainActivity|com\.reminderapp\.MainActivity)' "$smoke_dir/launch.txt" || return 17
  sleep 3
  adb shell dumpsys activity activities > "$smoke_dir/foreground.txt" 2>&1 || return 18
  grep -E 'mResumedActivity|topResumedActivity' "$smoke_dir/foreground.txt" | grep -Eq 'com\.reminderapp/(\.MainActivity|com\.reminderapp\.MainActivity)' || return 19
  adb exec-out screencap -p > "$smoke_dir/home.pending.png" || return 20
  test -s "$smoke_dir/home.pending.png" || return 21
  mv "$smoke_dir/home.pending.png" "$smoke_dir/home.png"
}
smoke_status=0
capture_smoke || smoke_status=$?
if [ "$smoke_status" -ne 0 ]; then
  rm -f "$smoke_dir/home.png" "$smoke_dir/home.pending.png"
  echo "::warning::App screenshot capture failed ($smoke_status); see install, launch and foreground logs."
fi
printf 'connectedDebugAndroidTest=%s\nscreenshot=%s\n' "$test_status" "$smoke_status" > "$smoke_dir/status.txt"
exit "$test_status"
