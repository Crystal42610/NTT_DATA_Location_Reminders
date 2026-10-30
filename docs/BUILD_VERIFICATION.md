# Final verification before sending to NTT DATA

This package includes source code, unit tests and an Android instrumentation test. It does **not** contain a compiled APK because Android SDK and Gradle distribution downloads are not available in the preparation environment. Do not describe the project as device-tested until you have run the following checks.

1. Open the root folder (contains `settings.gradle`) with Android Studio Narwhal Feature Drop or newer. Allow Gradle sync and install the SDK/Build Tools specified by `app/build.gradle`. Disable any NDK plugin if the IDE complains, as instructed by the starter project.
2. From Windows CMD at the project root run `scripts\verify_windows.bat` (build, JVM tests and lint), or use the Android Studio Gradle tasks. Select a Google Play-enabled emulator image running Android 13+ (or a compatible physical phone). Run `MemoDaoIntegrationTest` on the emulator.
3. Verify the three permission stages: precise location, background location, and notifications. Confirm the home-screen banner says monitoring is enabled.
4. Save a memo by tapping the map, ensure coordinates appear, and verify the new memo is listed.
5. Cross the selected 200-metre geofence using the emulator's Location controls. Allow several minutes, especially if the app is backgrounded; the notification must show the correct title and 140-character excerpt.
6. Tap the notification to open the memo. Verify re-entry does not duplicate the notification. Complete a different memo before visiting its location; it must not notify.
7. Reboot the emulator and cross the boundary for an unfinished reminder to check registration restoration.
8. Run the full build once more, confirm no source or dependency errors, and submit the project ZIP or the GitHub URL **before 2 November 2026**.

**Known issues to discuss:** Android geofencing is best-effort, Play services limits active geofences to 100, force-stop suppresses delivery, and the current at-most-once database claim can lose a delivery if the process dies between claiming and posting the notification. A durable outbox would improve that reliability trade-off.
