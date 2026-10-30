# Engineering notes — location reminders

## Scope from the supplied README

The app must save a memo with a map-selected point and produce a notification containing the title, first 140 note characters and an icon when the phone comes within 200 metres. It must work when the UI is in the background or the process is not running. The assignment is in Kotlin and asks for practical code quality, a self-contained Android Studio project and third-party libraries isolated behind abstractions.

## Approach and trade-offs

**Why geofences instead of GPS polling?** Android's geofencing service handles location monitoring efficiently while the app is not running. A receiver processes `ENTER` transitions via a mutable `PendingIntent`; there is no fragile, always-on foreground loop. A device with Google Play services is required. Geofencing is best effort: platform latency and power-saving behavior can delay events.

**Why OpenStreetMap?** osmdroid provides an interactive map without needing a Google Maps key. The user taps any point, and the UI stores full-precision decimal latitude/longitude. `MapPicker` isolates the map framework. Tiles need an internet connection; memo and geofence data remain local.

**Persistence/migration:** The starter app stores obsolete `Long` latitude/longitude fields that cannot preserve fractional degrees. Rather than silently convert or drop historical data, migration 1→2 adds nullable `Double` coordinates and a nullable `notifiedAt` field. Legacy notes remain readable, but do not become geofences until they are explicitly given a location. The conditional `UPDATE` that sets `notifiedAt` prevents duplicate delivery across repeated callbacks. Memo completion uses a dedicated SQL `UPDATE isDone = 1` instead of replacing the entire record, preventing a concurrent completion from clearing a notification claim.

**Modern Android UI:** The app explicitly targets API 36, and its activities apply system-bar insets on Android 15+ so the toolbar and floating controls are not obscured by enforced edge-to-edge layouts.

**Permissions:** Foreground fine location is requested first. Android 10 supports a separate background-location permission prompt; Android 11+ asks users to choose “Allow all the time” under system settings. Android 13+ requires a notification permission. The app also checks the application-wide notification switch, even on older Android versions. Missing permissions never prevent saving the memo; the home screen explains how to activate monitoring. The geofence receiver does not silently claim a notification while notification permission is denied.

**Lifecycle:** New memos register after persistence. Pending geofences are re-synchronised when the app is foregrounded, and on boot. Marking a memo complete removes its registration. An Android force-stop blocks background broadcasts until the user explicitly relaunches the app.

**Reliability boundary:** The conditional database update establishes at-most-once *notification attempts*. A device crash between committing the notification state and presenting the system notification could lose an alert. An industrial-strength version would use a durable notification outbox and retry policy. Play services limits apps to 100 active geofences; handling thousands would require dynamic nearest-N registration.

## Manual verification plan

| Test | Steps | Expected result |
|---|---|---|
| Creation validation | Leave title, text or map location empty | Save remains on screen and shows the relevant error |
| Coordinate precision | Tap different map points / 0° | Precise decimal coordinates, not rounded integer degrees |
| Arrival | Save a memo and move emulator from >200m to <200m | Status-bar alert with title, icon and ≤140 Unicode characters |
| Background | Press Home / remove app from Recents; move simulator within 200m | Notification arrives without opening the Activity |
| Process restart | Stop running app process without user Force stop; re-enter area | Geofence remains registered |
| Device reboot | Reboot and wait for `BOOT_COMPLETED`, then cross the boundary | Pending geofence is restored |
| Duplicate callback | Enter the same geofence multiple times | One alert per memo |
| Completion race | Trigger notification and complete memo in quick succession | Stored notification timestamp is never reset |
| Completion | Check a memo done before reaching location | Geofence removed; no reminder |
| Permissions | Deny background or notification access | Memo saves, monitoring guidance visible; after granting, sync registrations |
| Migration | Install v1, save note; upgrade to v2 | Old note remains, no false geofence |
| Tap notification | Receive and tap a location alert | Existing memo detail screen opens |

## Verification performed in the isolated workspace

- Pure Kotlin `GeoRules` and `NotePreview` were compiled with a local Kotlin compiler and passed a standalone smoke test, including Unicode/truncation edge cases.
- Android resource XML files were parsed successfully. Wrapper pinned to the Android Gradle Plugin 8.12-supported Gradle 8.13.
- The full Gradle task was attempted but could not download Gradle 9.0.0 (`services.gradle.org` is inaccessible in this environment). No APK or emulator test was produced. This is a build verification blocker to resolve in Android Studio before submission.

## Extension points

- Replace `OpenStreetMapPicker` with any map provider by implementing `MapPicker`.
- Replace `PlayServicesGeofenceRegistrar` by implementing `GeofenceRegistrar`.
- Move receiver work to a durable WorkManager job if device-specific broadcast execution limits warrant it.
- Add Room migration/instrumentation tests and UI tests with a Play-enabled emulator, then include their results in the submission.

## Privacy and setup

All memo content and precise selected coordinates remain in the local Room database. OpenStreetMap tile requests reveal the map area being viewed to tile infrastructure; no account or map API token is necessary. Google Play services processes geofence registration using the permissions granted by the user. The app does not request location updates continuously. The assignment uses a Play-enabled device for geofencing; a device without compatible Play services is not claimed to be supported.
