#!/usr/bin/env bash
set -uo pipefail

# Keep the product test result even when collecting post-test visual evidence.
test_status=0
./gradlew connectedDebugAndroidTest --no-daemon || test_status=$?

smoke_dir=app/build/reports/androidTests/smoke
mkdir -p "$smoke_dir"
capture_smoke() {
  adb shell input keyevent KEYCODE_WAKEUP &&
    adb shell wm dismiss-keyguard &&
    adb shell am start -W -n com.reminderapp/.MainActivity > "$smoke_dir/launch.txt" &&
    sleep 3 &&
    adb exec-out screencap -p > "$smoke_dir/home.png" &&
    test -s "$smoke_dir/home.png"
}
smoke_status=0
capture_smoke || smoke_status=$?
if [ "$smoke_status" -ne 0 ]; then
  rm -f "$smoke_dir/home.png"
  echo "::warning::App screenshot capture failed ($smoke_status); see launch log and device tests."
fi
printf 'connectedDebugAndroidTest=%s\nscreenshot=%s\n' "$test_status" "$smoke_status" > "$smoke_dir/status.txt"
exit "$test_status"
