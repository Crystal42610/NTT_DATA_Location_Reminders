<img width="200" height="400" alt="image" src="https://github.com/user-attachments/assets/c5f6609f-3055-424e-ba43-cd43a7e4eca4" />
<img width="200" height="400" alt="image" src="https://github.com/user-attachments/assets/2c1a94c3-7baa-4011-835d-c343707a0488" />
<img width="200" height="400" alt="image" src="https://github.com/user-attachments/assets/67562ff2-03e1-4763-966f-99f631cfebd7" />
<img width="200" height="400" alt="image" src="https://github.com/user-attachments/assets/32a34d3c-9bb9-4afe-8bc4-176c07099736" />

# Location-based Memo Reminders — NTT DATA coding challenge

A Kotlin implementation built on the supplied Android starter project. Users can create a memo, tap a point on an OpenStreetMap map, and receive a one-time notification upon entering a **200-metre** geofence, even when the Activity is closed. Existing memo list/detail screens and Room persistence have been retained.

**Original assignment:** [docs/ORIGINAL_CHALLENGE.md](docs/ORIGINAL_CHALLENGE.md). **Design decisions and verification:** [docs/ENGINEERING_NOTES.md](docs/ENGINEERING_NOTES.md) and [docs/BUILD_VERIFICATION.md](docs/BUILD_VERIFICATION.md).

## What works by design

- **Select a point on a map** when creating a memo. Coordinates retain full decimal precision; save validates title, description and location.
- **Persistent data:** Room stores the memo and precise coordinates. A Room 1 → 2 migration adds nullable latitude, longitude and notification timestamp without deleting existing notes.
- **On-device geofencing:** Google Play Services registers 200m `ENTER` geofences with a `PendingIntent` targeting a `BroadcastReceiver`. No foreground service or constantly running polling loop is needed.
- **Background delivery:** The receiver is invoked even when the Activity is not running. The app restores pending registrations after boot and resynchronises when the app returns to the foreground.
- **Notification content:** Memo title, its first **140 Unicode characters**, and a location icon. Tapping the notification opens its saved memo details.
- **One-shot delivery:** A conditional Room update stores `notifiedAt` so repeated geofence events do not create duplicate alerts. Completing a memo uses a dedicated SQL state transition that preserves `notifiedAt` and unregisters the geofence.
- **Permissions:** The home screen explains precise location, background location and notification access, including cases where notifications are disabled in Android settings. On Android 11+, the app opens Android settings to grant “Allow all the time.”

## How to open and run

1. Unzip the project. Open the **project root** (`settings.gradle` and `gradlew`) in **Android Studio Narwhal Feature Drop** or a newer compatible Android Studio. Let Gradle sync dependencies and install SDK 36 / build-tools 36 as prompted; the Gradle wrapper uses Gradle 8.13. **Disable an installed NDK plugin**, as specified by the starter README.
2. Use a device or emulator with **Google Play services** and Android 8.1+ (API 27 or higher). The OpenStreetMap map requires an internet connection, but **no map API key** is necessary. A Google Play-enabled emulator image is recommended.
3. Run the `app` configuration. Tap **Enable** on the home screen. Grant precise location, allow location access all the time (via system settings on Android 11+), and grant notifications on Android 13+. Confirm the status reads **Location reminders are enabled**.
4. Tap **+**, enter a memo title and description, pan/zoom the map and **tap a point**. Confirm that coordinates appear, then **Save**.
5. To test a trigger, use the emulator’s **Extended Controls → Location** to position the simulated device more than 200 metres from the point, then move it inside the selected radius. Geofence delivery is asynchronous, and background delivery can be delayed by several minutes. Check the status bar notification and tap it to open the memo.
6. Repeat after pressing Home, removing the app from Recents, or restarting the emulator. The boot receiver re-registers unfinished reminders once the device sends `BOOT_COMPLETED`. Android force-stop explicitly disables background component delivery until the user launches the app again.

## Run tests

In Android Studio, run **`GeoRulesTest`** and **`NotePreviewTest`** or execute from the project root:

```bash
./gradlew :app:testDebugUnitTest
```

On Windows CMD, run the all-in-one preflight script from the project root:

```cmd
scripts\verify_windows.bat
```

Or run individual checks:

```cmd
gradlew.bat :app:assembleDebug
gradlew.bat :app:testDebugUnitTest
gradlew.bat :app:lintDebug
```

For the emulator-only database tests, run `MemoDaoIntegrationTest` in Android Studio (or `gradlew.bat :app:connectedDebugAndroidTest` with a configured Google Play-enabled emulator).

The JVM tests verify the 200-metre radius and coordinate validation, stable geofence IDs, 140-character truncation and Unicode correctness. `MemoDaoIntegrationTest` must be run on an emulator or device to validate Room’s one-shot delivery and completion-race guards. See `docs/ENGINEERING_NOTES.md` for manual integration scenarios.

## Architecture

| Concern | Implementation / abstraction |
|---|---|
| Storage and migration | Room `Memo`, `MemoDao`, `Repository`, `Database.MIGRATION_1_2` |
| Map interaction | `MapPicker` interface → `OpenStreetMapPicker` (osmdroid) |
| Geofence registration | `GeofenceRegistrar` interface → `PlayServicesGeofenceRegistrar` |
| Synchronisation | `ReminderMonitoring`, `BootReceiver` |
| Delivery | `GeofenceReceiver`, `ReminderNotifier`, `NotePreview` |
| UI and permissions | `CreateMemo`, `Home`, `ViewMemo`, `LocationPermissionHelper` |

Third-party map and geofencing APIs are behind interfaces, allowing their implementations to be replaced and tested separately. No external backend or API keys are required.

## Operational boundaries

Android geofencing is **best-effort**, not a precisely timed GPS alarm. Actual triggering depends on location services, device power management, Google Play services and connectivity. Play services supports at most **100 active geofences per app**; exceeding that limit returns a visible monitoring error rather than silently dropping registrations. System-initiated process death is supported by the receiver; user **Force stop** is intentionally not bypassed.

The project was prepared and its pure Kotlin rules checked outside Android Studio; the Gradle wrapper has been aligned with Android Gradle Plugin 8.12.2 (Gradle 8.13). **The full Android Gradle build and device-level geofencing test could not be run in the isolated environment** because the Gradle distribution and Android SDK were unavailable. Run the stated Gradle tests and emulator scenarios before submitting and review the code to ensure you can explain the choices.
