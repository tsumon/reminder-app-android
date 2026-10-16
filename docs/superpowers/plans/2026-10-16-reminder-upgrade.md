# Reminder 2.8 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver the approved two-platform reminder upgrade while preserving the Android signing identity and user data.

**Architecture:** Keep two native apps and route actions through platform services. Add backward-compatible policy fields, schedule platform alarms from persisted state, and keep the shared JSON contract consistent. Implement and review each platform sequentially to avoid overlapping edits.

**Tech Stack:** Kotlin, Compose, Room, AlarmManager, WorkManager; Swift, SwiftUI, SwiftData, UserNotifications, AlarmKit; GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-10-16-reminder-upgrade.md`

## Global Constraints

- Android applicationId/namespace remain `com.reminderapp`; preserve the release signing configuration, secret names, key alias, keystore, and certificate. Never generate or rotate a release key. The new signed APK certificate SHA-256 must equal v2.7.2 before distribution.
- Keep Android minSdk 26, iOS deployment target 17, existing iOS bundle IDs and app-group IDs. Increment version to 2.8.0, build/versionCode 70. Use additive Room/SwiftData migrations; no destructive fallback, database reset, or user-data deletion.
- Keep all existing reminder kinds, lunar birthdays, holiday rules, AI/BYOK, WebDAV, local backups, nearby sharing, widgets, and languages. Preserve current defaults for existing reminders.
- Shared policy defaults and validation are exactly those in the spec. Any change to the contract must be reported to the controller before the other platform starts.

### Task 1: Android upgrade

**Files:** Work only in sibling repo `reminder-app-android`. Modify `app/src/main/java/com/reminderapp/{data,service,receiver,ui}`, manifest/resources, Gradle/wrapper/CI, and relevant docs/tests. Create focused policy/action/quick-create components instead of expanding the largest screens. Do not edit the iOS repo.

**Interfaces:** Consume existing ReminderEntity/DAOs/scheduler and the spec JSON fields. Produce an action service used by notifications/widget/app, a pure policy component usable in JVM tests, and schemaVersion 3 backups that iOS can consume. Define/record exact new API names in the report for integration.

- [ ] Add regression tests before implementation. Exercise engine behavior with deterministic `now`, including fixed vs afterCompletion, quiet boundaries, skip, retries, and old/new backup fixtures. Add an instrumentation migration test for the populated version-8 database and records. Keep original tests passing.

```kotlin
// Required assertions expressed against the chosen pure policy API:
// fixed: original daily anchor at 09:00, complete at 11:00 => tomorrow 09:00.
// afterCompletion: complete a daily reminder at 11:00 => tomorrow 11:00.
// gentle: first delivery => no additional alarm; completion remains possible.
// quiet 22:00-08:00: retry due 23:00 => next day 08:00.
// legacy JSON: recurrenceMode fixed, persistent retries, attempts 5.
```

- [ ] Add additive entity defaults and Room migration; implement scheduling/action/policy behavior from spec items 1-6. Do not schedule inside a DB transaction that can roll back. Compare expected occurrence/revision on asynchronous actions. Preserve native foreground/background behavior and existing channels.
- [ ] Wire real create/edit/home/detail/AI/notification/widget flows to the new behaviors; implement quick-create, editable summary, accessible undo/skip, compact home header, self-check and privacy/conflict screens. Reuse existing navigation and themes.
- [ ] Update compile/target SDK to 36 and a supported AGP/Gradle pair (8.10.1/8.11.1). Preserve signing block and secret names byte-for-byte where possible. Build versionCode 70/versionName 2.8.0. CI should test PRs without release secrets, and produce signed APK artifacts only on trusted dispatch/push. Do not publish a release.
- [ ] Run `./gradlew testDebugUnitTest assembleDebug lintDebug` with the installed SDK, capture failures and fix them; report unavailable device tests separately. Commit code and write full report with files, APIs, tests, migration evidence, constraints and remaining concerns.

### Task 2: iOS upgrade

**Files:** Work only in sibling repo `reminder-app-ios`: models, services, views, widget intents, project.yml, Info.plist, tests, workflow and docs. Read the approved shared spec from the Android repo and Task 1's resulting JSON fixture. Do not edit Android files.

**Interfaces:** Consume the exact schemaVersion 3 contract. Produce SwiftData defaults for old stores, policy/action services with equivalent behavior, the quick-create UI, guarded undo/skip and authorized AlarmKit integration. UserNotifications remains the old-OS fallback.

- [ ] Add pure Swift policy tests plus populated SwiftData upgrade/backup fixtures. Use the same fixed/afterCompletion/gentle/quiet/legacy cases listed in Task 1, and test queue allocation across many reminders so earliest primary requests beat auxiliary retries.

```swift
// Test original product code via macOS checks/XCTest, not copied implementations.
// Old stores retain UUIDs, titles, records, timestamps, enabled/status values.
// New fields absent from old backup => fixed + legacy persistent retry policy.
// Undo only restores the exact last completed revision; skip adds no confirmation.
```

- [ ] Implement spec items 2-9 with availability guards. New stored properties must have migration-safe defaults. Unify actions, record exactly once, replan all surviving reminders after sync, block destructive conflict resolution until selected, and preserve retry state.
- [ ] Implement AlarmKit using current Apple docs, supported authorization and stop/snooze App Intents. Add usage description, cancel corresponding alarms on edit/delete/confirm, observe state, and avoid duplicate main/retry alert paths. Configure important reminders from the UI; preserve iOS 17 compatibility.
- [ ] Add quick-create and editable summary, compact home card, skip/undo, policy controls, permission self-check and privacy text. Keep existing features and accessibility labels.
- [ ] Update CI/project to supported Xcode 26+ SDK, build 70/version 2.8.0 without changing bundle/app-group IDs or signing identity. Add all meaningful regression checks to PR CI. Compile application, widget, unit checks and UI tests when runners permit; report concrete failures rather than assuming success.
- [ ] Commit and report exact APIs, tests/results, platform limitations, and files for review.

### Task 3: Integration and release verification

**Files:** Test fixtures, CI validation scripts, release-readiness docs in both repos; no feature expansion.

**Interfaces:** Inputs are the two reviewed branches and their build artifacts. Outputs are tested draft PRs and a certificate comparison report.

- [ ] Compare golden fixtures field-by-field and round-trip both import/export implementations. Resolve behavioral differences with the responsible implementer.
- [ ] Run CI on each exact head; fix compilation/test/lint failures. Verify signed Android APK with `apksigner verify --print-certs` against downloaded v2.7.2 and inspect package/version with `aapt dump badging`.
- [ ] Run available emulator migration/upgrade/action tests. Preserve all unavailable physical-device checks in the report rather than labeling them passed.
- [ ] Generate complete diffs for independent task and final review; fix important findings, rerun affected checks, and attach created draft PRs to the chat. Do not merge or release.
