# Android 2.9 runtime captures

Actual APK, Android API 36 / Pixel 7 emulator, 1080×2400. The test-only RuntimeCaptureTest creates and removes sample reminders through the real database/actions; production starts with no sample data. create.png is from an actual manually edited form. Source version 2.9.0 / 71; no concept image or HTML mockup is used here.

Home/calendar/statistics/settings: light mode. Additional dark and 1.5× font captures are linked from validation documentation. The final large-font capture waits for real Compose accessibility content after ActivityScenario startup; a transient blank startup frame was rejected and replaced. These images demonstrate rendering, not physical notification delivery or release-signed in-place upgrade.
